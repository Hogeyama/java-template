package com.example.demo.security.controller;

import com.example.demo.security.entity.RevokedToken;
import com.example.demo.security.entity.User;
import com.example.demo.security.mapper.RevokedTokenMapper;
import com.example.demo.security.mapper.UserMapper;
import com.example.demo.security.service.JwtService;
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
public class AuthController {

    private final UserMapper userMapper;
    private final RevokedTokenMapper revokedTokenMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }

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

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest,
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletResponse response,
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
