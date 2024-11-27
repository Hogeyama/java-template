package com.example.demo.user.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Role(UUID id, String name, OffsetDateTime createdAt) {}
