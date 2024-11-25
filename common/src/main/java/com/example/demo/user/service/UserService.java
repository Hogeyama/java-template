package com.example.demo.user.service;

import com.example.demo.user.db.UserRepository;
import com.example.demo.user.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // ----------------------------------------------------------------------------------------------
  // Create User

  public sealed interface CreateUserResult
      permits CreateUserResult.Success, CreateUserResult.AlreadyExists {
    record Success(User user) implements CreateUserResult {}

    record AlreadyExists() implements CreateUserResult {}
  }

  public User createUser(User user) {
    userRepository.insert(user);
    return user;
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
    if (!passwordEncoder.matches(request.password(), user.password())) {
      log.info("Wrong password for user: {}", request.username());
      return new AuthResult.WrongPassword();
    }

    log.info("User authenticated: {}", request.username());
    return new AuthResult.Success(user);
  }
}
