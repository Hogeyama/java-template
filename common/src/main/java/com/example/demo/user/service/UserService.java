package com.example.demo.user.service;

import com.example.demo.user.entity.Role;
import com.example.demo.user.entity.User;
import com.example.demo.user.repository.UserRepository;
import com.example.demo.user.repository.UserRepository.InsertResult;
import com.example.demo.utils.Either;
import com.fasterxml.uuid.Generators;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  // ----------------------------------------------------------------------------------------------
  // Create User

  public sealed interface CreateUserResult
      permits CreateUserResult.Success,
          CreateUserResult.AlreadyExists,
          CreateUserResult.InvalidInput {
    record Success(User user) implements CreateUserResult {}

    record AlreadyExists() implements CreateUserResult {}

    record InvalidInput(String msg) implements CreateUserResult {}
  }

  public CreateUserResult createUser(String username, String password, @Nullable String role) {
    var now = OffsetDateTime.now(ZoneId.of("Asia/Tokyo"));
    var roles = new Role(generateUUID(), "READ", now);
    var mUser = User.of(username, password, "tekito@example.com", now, roles);

    switch (mUser) {
      case Either.Right(User user):
        // FIXME ユーザー名がコンフリクトしたのか、メルアドがコンフリクトしたのかを区別する必要がある
        // 事前のSELECTが必要
        var result = userRepository.insert(user);
        switch (result) {
          case InsertResult.Success():
            return new CreateUserResult.Success(user);
          case UserRepository.InsertResult.AlreadyExists():
            return new CreateUserResult.AlreadyExists();
        }
      case Either.Left(var msg):
        return new CreateUserResult.InvalidInput(msg);
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
    if (!user.getPasswordHash().matches(request.password())) {
      log.info("Wrong password for user: {}", request.username());
      return new AuthResult.WrongPassword();
    }

    log.info("User authenticated: {}", request.username());
    return new AuthResult.Success(user);
  }

  // ----------------------------------------------------------------------------------------------
  // Change Password

  public Optional<User> changePassword(User user, String newPassword) {
    switch (user.changePassword(newPassword)) {
      case Either.Left(var msg):
        return Optional.empty();
      case Either.Right(User newUser):
        userRepository.changePassword(newUser.getId(), newUser.getPasswordHash());
        return Optional.of(newUser);
    }
  }
}
