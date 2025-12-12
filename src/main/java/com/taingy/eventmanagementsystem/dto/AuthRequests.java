package com.taingy.eventmanagementsystem.dto;

public class AuthRequests {

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String email, String firstName, String lastName, String password) {}
    public record AuthResponse(String token, UserResponseDTO user) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}
    public record ResetPasswordRequest(String newPassword) {}
    public record VerifyOtpRequest(String email, String otpCode) {}
    public record ResendOtpRequest(String email) {}
    public record RegisterResponse(String message, String email) {}
    public record ForgotPasswordRequest(String email) {}
    public record ResetPasswordWithOtpRequest(String email, String otpCode, String newPassword) {}

}
