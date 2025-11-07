package com.taingy.eventmanagementsystem.dto;

public class AuthRequests {

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String email, String firstName, String lastName, String password) {}
    public record AuthResponse(String token, UserResponseDTO user) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}
    public record ResetPasswordRequest(String newPassword) {}

}
