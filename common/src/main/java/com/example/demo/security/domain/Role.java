package com.example.demo.security.domain;

import java.time.OffsetDateTime;
import lombok.Data;
import org.jspecify.annotations.NullUnmarked;

@NullUnmarked
@Data
public class Role {
  private Long id;
  private String name;
  private OffsetDateTime createdAt;
}
