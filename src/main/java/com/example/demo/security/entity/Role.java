package com.example.demo.security.entity;

import java.time.OffsetDateTime;

import org.jspecify.annotations.NullUnmarked;

import lombok.Data;

@NullUnmarked
@Data
public class Role {
  private Long id;
  private String name;
  private OffsetDateTime createdAt;
}
