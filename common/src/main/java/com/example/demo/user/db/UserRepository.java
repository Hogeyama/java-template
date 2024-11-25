package com.example.demo.user.db;

import static com.example.demo.jooq.tables.Roles.ROLES;
import static com.example.demo.jooq.tables.UserRoles.USER_ROLES;
import static com.example.demo.jooq.tables.Users.USERS;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

import com.example.demo.jooq.tables.records.RolesRecord;
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

  public enum InsertResult {
    SUCCESS,
    ALREADY_EXISTS
  }

  public InsertResult insert(User user) {
    // TODO
    return InsertResult.SUCCESS;
  }

  public void updatePassword(Long id, String password) {
    // TODO
  }

  // --------------------------------------------------------------------------------------------
  // Helper

  private static User fromRecord(UsersRecord r, Set<RolesRecord> roles) {
    return new User(
        r.getId(),
        r.getUsername(),
        r.getPassword(),
        r.getEmail(),
        r.getEnabled(),
        r.getCreatedAt(),
        r.getUpdatedAt(),
        roles.stream().map(UserRepository::fromRecord).collect(Collectors.toSet()));
  }

  private static Role fromRecord(RolesRecord r) {
    return new Role(r.getId(), r.getName(), r.getCreatedAt());
  }
}
