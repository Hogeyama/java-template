package com.example.demo.security.controller;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.example.demo.auth.infra.SessionManager;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.UserService.AuthResult;
import com.example.demo.user.service.UserService.CreateUserResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "認証関連のAPI")
public class AuthController {
  private final UserService userService;
  private final SessionManager sessionManager;

  // --------------------------------------------------------------------------------------------
  // サインアップ

  @Schema(description = "サインアップリクエスト")
  public static record SignupRequest(
      @NotNull(message = "Username is required") @Schema(description = "ユーザー名", example = "user1")
          String username,
      @NotNull(message = "Password is required")
          @Schema(description = "パスワード", example = "password123")
          String password,
      @Nullable @Schema(description = "ロール", example = "USER") String role) {}

  @Operation(summary = "サインアップ", description = "新しいユーザーを登録します")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "サインアップ成功",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "400",
            description = "ユーザー名が既に存在する",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {

    log.info("signup request", kv("username", request.username()));

    var result = userService.createUser(request.username(), request.password(), request.role());

    return switch (result) {
      case CreateUserResult.Success(var user) -> {
        log.info("User created: {}", user.getUsername());
        yield ResponseEntity.ok().build();
      }

      case CreateUserResult.AlreadyExists() -> {
        yield ResponseEntity.badRequest().body("Username already exists");
      }

      case CreateUserResult.InvalidInput(var e) -> {
        yield ResponseEntity.badRequest().body("Invalid password");
      }
    };
  }

  // --------------------------------------------------------------------------------------------
  // ログイン

  @Schema(description = "ログインリクエスト")
  public static record LoginRequest(
      @NotNull(message = "Username is required") @Schema(description = "ユーザー名", example = "user1")
          String username,
      @NotNull(message = "Password is required")
          @Schema(description = "パスワード", example = "password123")
          String password) {}

  @Operation(summary = "ログイン", description = "ユーザー名とパスワードでログインし、セッションを開始します")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "ログイン成功",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "400",
            description = "無効なユーザー名またはパスワード",
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  @PostMapping("/login")
  public ResponseEntity<?> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

    log.info("login request", kv("username", request.username()));

    var result =
        userService.authenticate(
            new UserService.AuthChallange(request.username(), request.password()));

    return switch (result) {
      case AuthResult.Success(var user) -> {
        sessionManager.createSession(httpRequest, user);
        yield ResponseEntity.ok().build();
      }

      case AuthResult.UserNotFound() -> {
        yield ResponseEntity.badRequest().body("Invalid username or password");
      }

      case AuthResult.WrongPassword() -> {
        yield ResponseEntity.badRequest().body("Invalid username or password");
      }
    };
  }

  // --------------------------------------------------------------------------------------------
  // ログアウト

  @Operation(summary = "ログアウト", description = "現在のセッションからログアウトします")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "ログアウト成功")})
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    sessionManager.invalidateSession(request);
    return ResponseEntity.ok().build();
  }

  // --------------------------------------------------------------------------------------------
  // パスワード変更

  @Schema(description = "パスワード変更リクエスト")
  public static record ChangePasswordRequest( //
      @NotNull(message = "Old password is required") //
          @Schema(description = "現在のパスワード", example = "oldPassword123") //
          String oldPassword, //
      @NotNull(message = "New password is required") //
          @Schema(description = "新しいパスワード", example = "newPassword123") //
          String newPassword) {}

  @Operation(summary = "パスワード変更", description = "パスワードを変更し、既存のセッションを無効にします")
  @ApiResponses(
      value = { //
        @ApiResponse(
            responseCode = "200", //
            description = "パスワード変更成功",
            content = @Content(schema = @Schema(implementation = String.class))),
        @ApiResponse(
            responseCode = "400", //
            description = "無効なパスワードまたはユーザーが見つかりません", //
            content = @Content(schema = @Schema(implementation = String.class)))
      })
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(
      @Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {

    String username = sessionManager.getUsername();

    var result =
        userService.authenticate(new UserService.AuthChallange(username, request.oldPassword()));

    switch (result) {
      case AuthResult.Success(var user) -> {
        userService.changePassword(user, request.newPassword());
        sessionManager.invalidateAllSessionsForCurrentUser();
        return ResponseEntity.ok().build();
      }

      case AuthResult.UserNotFound() -> {
        return ResponseEntity.badRequest().body("Invalid username or password");
      }

      case AuthResult.WrongPassword() -> {
        return ResponseEntity.badRequest().body("Invalid username or password");
      }
    }
  }
}
