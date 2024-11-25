package com.example.demo.user.entity;

import java.time.OffsetDateTime;
import java.util.Set;

public record User(
    Long id,
    String username,
    String password,
    String email,
    boolean enabled,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Set<Role> roles) {

  public User {
    if (id == null) {
      throw new IllegalArgumentException("id is null");
    }
  }
}
