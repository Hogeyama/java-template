package com.example.demo.security.entity;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class Role {
    private Long id;
    private String name;
    private OffsetDateTime createdAt;
}
