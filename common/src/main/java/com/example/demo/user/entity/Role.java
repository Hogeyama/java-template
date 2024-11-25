package com.example.demo.user.entity;

import java.time.OffsetDateTime;

public record Role(Long id, String name, OffsetDateTime createdAt) {}
