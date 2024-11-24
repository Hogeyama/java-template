package com.example.demo.security.infra;

import java.util.Optional;
import java.util.Set;

import org.apache.ibatis.annotations.*;

import com.example.demo.security.domain.Role;
import com.example.demo.security.domain.User;

@Mapper
public interface UserMapper {
  @Select("SELECT * FROM users WHERE username = #{username}")
  @Results({@Result(property = "id", column = "id"), @Result(property = "roles", column = "id",
      javaType = Set.class, many = @Many(select = "findRolesByUserId"))})
  Optional<User> findByUsername(String username);

  @Select("SELECT r.* FROM roles r " + "INNER JOIN user_roles ur ON r.id = ur.role_id "
      + "WHERE ur.user_id = #{userId}")
  Set<Role> findRolesByUserId(Long userId);

  @Insert("INSERT INTO users (username, password, email, enabled) "
      + "VALUES (#{username}, #{password}, #{email}, #{enabled})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(User user);

  @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
  void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

  @Update("UPDATE users SET password = #{password}, updated_at = CURRENT_TIMESTAMP "
      + "WHERE id = #{id}")
  void updatePassword(@Param("id") Long id, @Param("password") String password);
}
