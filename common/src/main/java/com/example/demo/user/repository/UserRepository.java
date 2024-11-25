package com.example.demo.user.repository;

import com.example.demo.user.entity.User;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
  // ----------------------------------------------------------------------------------------------
  // Find User

  Optional<User> findByUsername(String username);

  // ----------------------------------------------------------------------------------------------
  // Insert User

  public enum InsertResult {
    SUCCESS,
    ALREADY_EXISTS
  }

  InsertResult insert(User user);

  // ----------------------------------------------------------------------------------------------
  // Update User

  void updatePassword(Long id, String password);
}
