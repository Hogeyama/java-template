package com.example.demo.user.entity;

import java.time.OffsetDateTime;
import java.util.Set;

// @Data
// public class User {
//   private Long id;
//   private String username;
//   private String password;
//   private String email;
//   private boolean enabled;
//   private OffsetDateTime createdAt;
//   private OffsetDateTime updatedAt;
//   private Set<Role> roles;
//
//   public Long id() {
//     return id;
//   }
//
//   public String username() {
//     return username;
//   }
//
//   public String password() {
//     return password;
//   }
//
//   public String email() {
//     return email;
//   }
//
//   public boolean enabled() {
//     return enabled;
//   }
//
//   public OffsetDateTime createdAt() {
//     return createdAt;
//   }
//
//   public OffsetDateTime updatedAt() {
//     return updatedAt;
//   }
//
//   public Set<Role> roles() {
//     return roles;
//   }
// }

public record User(
    Long id,
    String username,
    String password,
    String email,
    boolean enabled,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Set<Role> roles) {}
