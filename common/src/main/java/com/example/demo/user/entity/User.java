package com.example.demo.user.entity;

import com.example.demo.utils.Either;
import com.fasterxml.uuid.Generators;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class User {
  @With private UUID id;
  @With private String username;
  @With private PasswordHash passwordHash;
  @With private String email;
  @With private boolean enabled;
  @With private OffsetDateTime createdAt;
  @With private OffsetDateTime updatedAt;
  @With private Set<Role> roles;

  // ----------------------------------------------------------------------------------------------
  // Factory

  public static Either<String, User> of(
      String username, String rawPasword, String email, OffsetDateTime now, Role role) {
    var id = generateUUID();
    var enabled = true;
    var createdAt = now;
    var updatedAt = now;
    var roles = Set.of(role);

    if (!isValidUsername(username)) {
      return Either.left("Invalid username");
    }

    if (!isValidPassword(rawPasword)) {
      return Either.left("Invalid password");
    }
    var passwordHash = PasswordHash.of(rawPasword);

    if (!isValidEmail(email)) {
      return Either.left("Invalid email");
    }

    return Either.right(
        new User(id, username, passwordHash, email, enabled, createdAt, updatedAt, roles));
  }

  /** Repositoryからの復元またはテスト用途以外で使わないこと */
  public static User unsafeOf(
      UUID id,
      String username,
      PasswordHash passwordHash,
      String email,
      boolean enabled,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      Set<Role> roles) {
    return new User(id, username, passwordHash, email, enabled, createdAt, updatedAt, roles);
  }

  // ----------------------------------------------------------------------------------------------
  // Mutations

  public Either<String, User> changePassword(String newPassword) {
    if (!isValidPassword(newPassword)) {
      return Either.left("Invalid password");
    }
    var passwordHash = PasswordHash.of(newPassword);
    return Either.right(this.withPasswordHash(passwordHash));
  }

  // ----------------------------------------------------------------------------------------------
  // Validation

  // 一旦これで
  private static final Pattern ANY_PATTERN = Pattern.compile("^.*$");
  private static final Pattern USERNAME_PATTERN = ANY_PATTERN;
  private static final Pattern PASSWORD_PATTERN = ANY_PATTERN;
  private static final Pattern EMAIL_PATTERN = ANY_PATTERN;

  public static boolean isValidUsername(String username) {
    return USERNAME_PATTERN.matcher(username).matches();
  }

  public static boolean isValidPassword(String password) {
    return PASSWORD_PATTERN.matcher(password).matches();
  }

  public static boolean isValidEmail(String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }

  // ----------------------------------------------------------------------------------------------
  // Internal

  private static UUID generateUUID() {
    return Generators.timeBasedEpochRandomGenerator().generate();
  }

  // ----------------------------------------------------------------------------------------------
  // PasswordHash: 平文と取り違えやすいので固有の型にする

  public static class PasswordHash {
    private final String hash;

    // 直接依存しちゃっても問題ない
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private PasswordHash(String hash) {
      this.hash = hash;
    }

    private static PasswordHash of(String rawPassword) {
      return new PasswordHash(encoder.encode(rawPassword));
    }

    public static PasswordHash unsafeOf(String hash) {
      return new PasswordHash(hash);
    }

    public boolean matches(String rawPassword) {
      return encoder.matches(rawPassword, this.hash);
    }

    public String asString() {
      return this.hash;
    }
  }
}
