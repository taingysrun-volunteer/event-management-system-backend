package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.AuthRequests;
import com.taingy.eventmanagementsystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthRequests.RegisterResponse> register(@RequestBody AuthRequests.RegisterRequest request) {
        AuthRequests.RegisterResponse registerResponse = authService.register(request);
        return ResponseEntity.ok(registerResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthRequests.AuthResponse> verifyOtp(@RequestBody AuthRequests.VerifyOtpRequest request) {
        AuthRequests.AuthResponse authResponse = authService.verifyOtp(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthRequests.RegisterResponse> resendOtp(@RequestBody AuthRequests.ResendOtpRequest request) {
        AuthRequests.RegisterResponse response = authService.resendOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthRequests.AuthResponse> login(@RequestBody AuthRequests.LoginRequest request) {
        AuthRequests.AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody AuthRequests.ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
