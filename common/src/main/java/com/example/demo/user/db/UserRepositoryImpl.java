package com.example.demo.user.db;

import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {

  private final UserMapper userMapper;

  @Override
  public Optional<User> findByUsername(String username) {
    return userMapper.findByUsername(username);
  }

  @Override
  public InsertResult insert(User user) {
    // TODO savepoint & rollbackは要らない？
    try {
      userMapper.insert(user);
      return InsertResult.SUCCESS;
    } catch (DuplicateKeyException e) {
      return InsertResult.ALREADY_EXISTS;
    }
  }

  @Override
  public void updatePassword(Long id, String password) {
    userMapper.updatePassword(id, password);
  }
}
