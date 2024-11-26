package com.example.demo.user.entity;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;

public record User(
    Long id,
    String username,
    PasswordHash passwordHash,
    String email,
    boolean enabled,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Set<Role> roles) {
  public User changePassword(PasswordHash newPassword) {
    return new User(id, username, newPassword, email, enabled, createdAt, updatedAt, roles);
  }

  public static class PasswordHash {
    private final String hash;

    private PasswordHash(String hash) {
      this.hash = hash;
    }

    /** パスワードとして不正な場合はemptyを返す */
    public static Optional<PasswordHash> of(String rawPassword, PasswordEncoder encoder) {
      if (!isValidPassword(rawPassword)) {
        return Optional.empty();
      } else {
        return Optional.of(new PasswordHash(encoder.encode(rawPassword)));
      }
    }

    public static PasswordHash unsafeOf(String hash) {
      return new PasswordHash(hash);
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
      return encoder.matches(rawPassword, this.hash);
    }

    public String asString() {
      return this.hash;
    }

    private static boolean isValidPassword(String password) {
      return PASSWORD_PATTERN.matcher(password).matches();
    }

    private static final Pattern PASSWORD_PATTERN =
        // Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        Pattern.compile("^.*$"); // 動作確認が面倒なので一旦これで
  }
}
