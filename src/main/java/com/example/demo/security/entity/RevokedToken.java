package com.example.demo.security.entity;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class RevokedToken {
    private Long id;
    private String jti;
    private OffsetDateTime revokedAt;
    private String reason;
    private Long createdByUserId;
}
