package com.example.demo.user.service;

import com.example.demo.user.entity.Role;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;
import com.example.demo.user.repository.UserRepository.InsertResult;
import com.fasterxml.uuid.Generators;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // ----------------------------------------------------------------------------------------------
  // Create User

  public sealed interface CreateUserResult
      permits CreateUserResult.Success,
          CreateUserResult.AlreadyExists,
          CreateUserResult.InvalidPassword {
    record Success(User user) implements CreateUserResult {}

    record AlreadyExists() implements CreateUserResult {}

    record InvalidPassword() implements CreateUserResult {}
  }

  public CreateUserResult createUser(String username, String password, @Nullable String role) {
    var mHash = User.PasswordHash.of(password, passwordEncoder);
    if (mHash.isEmpty()) {
      return new CreateUserResult.InvalidPassword();
    }

    var hash = mHash.get();
    var now = OffsetDateTime.now(ZoneId.of("Asia/Tokyo"));

    var roleId = generateUUID();
    var roles = Set.of(new Role(roleId, "READ", now)); // TODO リポジトリから取る

    var userId = generateUUID();
    var user = new User(userId, username, hash, "tekito@example.com", true, now, now, roles);

    switch (userRepository.insert(user)) {
      case InsertResult.Success():
        log.info("User created: {}", user.username());
        return new CreateUserResult.Success(user);
      case UserRepository.InsertResult.AlreadyExists():
        log.info("User already exists: {}", user.username());
        return new CreateUserResult.AlreadyExists();
    }
  }

  private UUID generateUUID() {
    return Generators.timeBasedEpochRandomGenerator().generate();
  }

  // ----------------------------------------------------------------------------------------------
  // Authenticate

  public record AuthChallange(String username, String password) {}

  public sealed interface AuthResult
      permits AuthResult.Success, AuthResult.UserNotFound, AuthResult.WrongPassword {

    public record Success(User user) implements AuthResult {}

    public record UserNotFound() implements AuthResult {}

    public record WrongPassword() implements AuthResult {}
  }

  public AuthResult authenticate(AuthChallange request) {
    log.info("Authenticate request: {}", request);

    Optional<User> userOpt = userRepository.findByUsername(request.username());
    if (userOpt.isEmpty()) {
      log.info("User not found: {}", request.username());
      return new AuthResult.UserNotFound();
    }

    User user = userOpt.get();
    if (!user.passwordHash().matches(request.password(), passwordEncoder)) {
      log.info("Wrong password for user: {}", request.username());
      return new AuthResult.WrongPassword();
    }

    log.info("User authenticated: {}", request.username());
    return new AuthResult.Success(user);
  }

  // ----------------------------------------------------------------------------------------------
  // Change Password

  public Optional<User> changePassword(User user, String newPassword) {
    var mHash = User.PasswordHash.of(newPassword, passwordEncoder);
    if (mHash.isEmpty()) {
      return Optional.empty();
    }

    var newHash = mHash.get();
    var newUser = user.changePassword(newHash);
    userRepository.changePassword(newUser.id(), newHash);

    return Optional.of(newUser);
  }
}
