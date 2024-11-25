package com.example.demo.user.db;

import com.example.demo.user.entity.Role;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final UserMapper userMapper;

  @Override
  public Optional<User> findByUsername(String username) {
    // TODO これ全部つけるのか？
    try {
      return userMapper.findByUsername(username).map(RawUser::fromRaw);
    } catch (DataAccessException e) {
      log.error("Failed to find user by username: {}", username, e);
      return Optional.empty();
    }
  }

  @Override
  public InsertResult insert(User user) {
    // TODO savepoint & rollbackは要らない？
    try {
      userMapper.insert(RawUser.toRaw(user));
      return InsertResult.SUCCESS;
    } catch (DuplicateKeyException e) {
      return InsertResult.ALREADY_EXISTS;
    }
  }

  @Override
  public void updatePassword(Long id, String password) {
    userMapper.updatePassword(id, password);
  }

  // --------------------------------------------------------------------------------------------
  // Query

  @Mapper
  public interface UserMapper {
    @Select(
        """
        SELECT *
        FROM users
        WHERE username = #{username}
        """)
    @Results({
      @Result(property = "id", column = "id"), // これなんで要るんだ？
      @Result(
          property = "roles",
          column = "id",
          javaType = Set.class,
          many = @Many(select = "findRolesByUserId"))
    })
    Optional<RawUser> findByUsername(String username);

    @Select(
        """
        SELECT r.* FROM roles r
        INNER JOIN user_roles ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId}
        """)
    Set<RawRole> findRolesByUserId(Long userId);

    @Insert(
        """
        INSERT INTO users (username, password, email, enabled)
        VALUES (#{username}, #{password}, #{email}, #{enabled})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RawUser user);

    @Insert(
        """
        INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})
        """)
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Update(
        """
        UPDATE users
        SET password = #{password}
          , updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
        """)
    void updatePassword(@Param("id") Long id, @Param("password") String password);
  }

  @Data
  @AllArgsConstructor
  public static class RawUser {
    private Long id;
    private String username;
    private String password;
    private String email;
    private boolean enabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<RawRole> roles;

    User fromRaw() {
      return new User(
          id,
          username,
          password,
          email,
          enabled,
          createdAt,
          updatedAt,
          roles.stream().map(RawRole::fromRaw).collect(Collectors.toSet()));
    }

    static RawUser toRaw(User user) {
      return new RawUser(
          user.id(),
          user.username(),
          user.password(),
          user.email(),
          user.enabled(),
          user.createdAt(),
          user.updatedAt(),
          user.roles().stream().map(RawRole::toRaw).collect(Collectors.toSet()));
    }
  }

  @Data
  @AllArgsConstructor
  public static class RawRole {
    private Long id;
    private String name;
    private OffsetDateTime createdAt;

    Role fromRaw() {
      return new Role(id, name, createdAt);
    }

    static RawRole toRaw(Role role) {
      return new RawRole(role.id(), role.name(), role.createdAt());
    }
  }
}
