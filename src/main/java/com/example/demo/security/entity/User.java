package com.example.demo.security.entity;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private boolean enabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<Role> roles;
}
