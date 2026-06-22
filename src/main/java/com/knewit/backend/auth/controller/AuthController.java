package com.knewit.backend.auth.controller;

import com.knewit.backend.auth.dto.*;
import com.knewit.backend.auth.service.AuthService;
import com.knewit.backend.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtService jwtService;

    @PostMapping("/signup/email")
    public ResponseEntity<EmailSignUpResponse> signup(@Valid @RequestBody EmailSignUpRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ResendVerificationResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok(authService.resendVerification(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Map<?,?> response = authService.logoutUser(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<ProfileCompletionResponse> completeProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProfileCompletionRequest profileCompletionRequest) {
        Long userId = jwtService.extractUserId(authHeader);
        return ResponseEntity.ok(authService.completeProfile(userId, profileCompletionRequest));
    }
}
