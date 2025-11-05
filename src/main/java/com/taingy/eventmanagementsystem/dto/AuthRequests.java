package com.taingy.eventmanagementsystem.dto;

public class AuthRequests {

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String firstName, String lastName, String password) {}
    public record AuthResponse(String token, UserResponseDTO user) {}

}
