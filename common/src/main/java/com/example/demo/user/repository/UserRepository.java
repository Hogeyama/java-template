package com.example.demo.user.repository;

import static com.example.demo.jooq.tables.Roles.ROLES;
import static com.example.demo.jooq.tables.UserRoles.USER_ROLES;
import static com.example.demo.jooq.tables.Users.USERS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.demo.jooq.tables.records.RolesRecord;
import com.example.demo.jooq.tables.records.UserRolesRecord;
import com.example.demo.jooq.tables.records.UsersRecord;
import com.example.demo.user.entity.Role;
import com.example.demo.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class UserRepository {

  private final DSLContext dsl;

  public Optional<User> findByUsername(String username) {
    var query =
        dsl.select(
                USERS.asterisk(),
                multiset(
                        select(ROLES.asterisk())
                            .from(ROLES.join(USER_ROLES).on(ROLES.ID.eq(USER_ROLES.ROLE_ID)))
                            .where(USER_ROLES.USER_ID.eq(USERS.ID)))
                    .as("roles")
                    .convertFrom(r -> r.into(ROLES)))
            .from(USERS)
            .where(USERS.USERNAME.eq(username));
    var record = query.fetchOne();

    var userR = record.into(USERS);
    @SuppressWarnings("unchecked")
    var roleRs = (List<RolesRecord>) record.get("roles");

    if (userR != null) {
      var user = fromRecord(userR, new TreeSet<>(roleRs));
      return Optional.of(user);
    } else {
      return Optional.empty();
    }
  }

  public sealed interface InsertResult permits InsertResult.Success, InsertResult.AlreadyExists {
    public record Success() implements InsertResult {}

    public record AlreadyExists() implements InsertResult {}
  }

  public InsertResult insert(User user) {
    try {
      // Unique violationのときはここまでrollbackされる
      dsl.transaction(
          tx -> {
            var userR = toRecord(user);
            var userRolesR =
                user.roles().stream()
                    .map(role -> new UserRolesRecord(user.id(), role.id(), role.createdAt()))
                    .collect(Collectors.toSet());
            tx.dsl().insertInto(USERS).set(userR).execute();
            tx.dsl().batchInsert(userRolesR).execute();
          });
      return new InsertResult.Success();
    } catch (DataAccessException e) {
      if (e.sqlState().equals("23505")) { // Unique violation
        return new InsertResult.AlreadyExists();
      } else {
        throw e;
      }
    }
  }

  public void changePassword(Long id, User.PasswordHash passwordHash) {}

  // --------------------------------------------------------------------------------------------
  // Helper

  private static User fromRecord(UsersRecord r, Set<RolesRecord> roles) {
    return new User(
        r.getId(),
        r.getUsername(),
        User.PasswordHash.unsafeOf(r.getPassword()),
        r.getEmail(),
        r.getEnabled(),
        r.getCreatedAt(),
        r.getUpdatedAt(),
        roles.stream().map(UserRepository::fromRecord).collect(Collectors.toSet()));
  }

  private static Role fromRecord(RolesRecord r) {
    return new Role(r.getId(), r.getName(), r.getCreatedAt());
  }

  private static UsersRecord toRecord(User user) {
    return new UsersRecord(
        user.id(),
        user.username(),
        user.passwordHash().asString(),
        user.email(),
        user.enabled(),
        user.createdAt(),
        user.updatedAt());
  }
}
