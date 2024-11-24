package com.example.demo.security.controller;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.domain.User;
import com.example.demo.security.infra.UserMapper; // TODO AuthUseCaseを経由する

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "認証関連のAPI")
public class AuthController {
  public AuthController(UserMapper userMapper, PasswordEncoder passwordEncoder,
      FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.sessionRepository = sessionRepository;
  }

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

  // --------------------------------------------------------------------------------------------
  // ログイン

  @Schema(description = "ログインリクエスト")
  public static record LoginRequest(//
      @NotNull(message = "Username is required") //
      @Schema(description = "ユーザー名", example = "user1") //
      String username, //

      @NotNull(message = "Password is required") //
      @Schema(description = "パスワード", example = "password123") //
      String password) {}

  @Operation(summary = "ログイン", description = "ユーザー名とパスワードでログインし、セッションを開始します")
  @ApiResponses(value = { //
      @ApiResponse(responseCode = "200", //
          description = "ログイン成功", //
          content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(responseCode = "400", //
          description = "無効なユーザー名またはパスワード", //
          content = @Content(schema = @Schema(implementation = String.class)))})
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {

    log.info("login request", kv("username", request.username()));

    Optional<User> userOpt = userMapper.findByUsername(request.username());
    if (userOpt.isEmpty()
        || !passwordEncoder.matches(request.password(), userOpt.get().getPassword())) {
      return ResponseEntity.badRequest().body("Invalid username or password");
    }

    User user = userOpt.get();

    // ユーザーのロールをSpring SecurityのGrantedAuthorityに変換
    var authorities =
        user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toSet());

    // セッションを作成し、認証情報を保存
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = //
        new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
    securityContext.setAuthentication(authentication);

    HttpSession session = httpRequest.getSession(true);
    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        securityContext);

    return ResponseEntity.ok().build();
  }

  // --------------------------------------------------------------------------------------------
  // ログアウト

  @Operation(summary = "ログアウト", description = "現在のセッションからログアウトします")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "ログアウト成功")})
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();
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

  @Operation(summary = "パスワード変更", description = "現在のパスワードを確認した上で、新しいパスワードに変更します")
  @ApiResponses(value = {//
      @ApiResponse(responseCode = "200", //
          description = "パスワード変更成功",
          content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(responseCode = "400", //
          description = "無効なパスワードまたはユーザーが見つかりません", //
          content = @Content(schema = @Schema(implementation = String.class)))})
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
      HttpServletRequest httpRequest) {

    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    Optional<User> userOpt = userMapper.findByUsername(username);
    if (userOpt.isEmpty()) {
      return ResponseEntity.badRequest().body("User not found");
    }

    User user = userOpt.get();
    if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
      return ResponseEntity.badRequest().body("Invalid old password");
    }

    // パスワードを更新
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userMapper.updatePassword(user.getId(), user.getPassword());

    // ユーザーの全セッションを削除
    sessionRepository.findByPrincipalName(username)
        .forEach((id, session) -> sessionRepository.deleteById(id));

    return ResponseEntity.ok().build();
  }
}