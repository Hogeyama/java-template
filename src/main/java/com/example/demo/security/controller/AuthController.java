package com.example.demo.security.controller;

import com.example.demo.security.entity.RevokedToken;
import com.example.demo.security.entity.User;
import com.example.demo.security.mapper.RevokedTokenMapper;
import com.example.demo.security.mapper.UserMapper;
import com.example.demo.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "認証関連のAPI")
public class AuthController {

    private final UserMapper userMapper;
    private final RevokedTokenMapper revokedTokenMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Data
    @Schema(description = "ログインリクエスト")
    public static class LoginRequest {
        @Schema(description = "ユーザー名", example = "user1")
        private String username;
        @Schema(description = "パスワード", example = "password123")
        private String password;
    }

    @Data
    @Schema(description = "パスワード変更リクエスト")
    public static class ChangePasswordRequest {
        @Schema(description = "現在のパスワード", example = "oldPassword123")
        private String oldPassword;
        @Schema(description = "新しいパスワード", example = "newPassword123")
        private String newPassword;
    }

    @Operation(summary = "ログイン", description = "ユーザー名とパスワードでログインし、JWTトークンを取得します")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ログイン成功"),
        @ApiResponse(responseCode = "400", description = "無効なユーザー名またはパスワード", 
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        
        Optional<User> userOpt = userMapper.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS環境では必須
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24時間
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "パスワード変更", description = "現在のパスワードを確認した上で、新しいパスワードに変更します")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "パスワード変更成功"),
        @ApiResponse(responseCode = "400", description = "無効なパスワードまたはユーザーが見つかりません", 
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest,
            @Parameter(description = "JWT認証トークン", required = true)
            @CookieValue("jwt") String token) {

        String username = jwtService.getUsername(token);
        Optional<User> userOpt = userMapper.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid old password");
        }

        // パスワードを更新
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updatePassword(user.getId(), user.getPassword());

        // 現在のトークンを無効化
        String jti = jwtService.getJti(token);
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setJti(jti);
        revokedToken.setReason("Password changed");
        revokedToken.setCreatedByUserId(user.getId());
        revokedTokenMapper.insert(revokedToken);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "ログアウト", description = "現在のセッションからログアウトし、JWTトークンを無効化します")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ログアウト成功")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletResponse response,
            @Parameter(description = "JWT認証トークン", required = true)
            @CookieValue("jwt") String token) {

        // トークンを無効化
        String jti = jwtService.getJti(token);
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setJti(jti);
        revokedToken.setReason("Logout");
        revokedTokenMapper.insert(revokedToken);

        // Cookieを削除
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}
