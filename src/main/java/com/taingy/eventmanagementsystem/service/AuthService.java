package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.dto.AuthRequests;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.CustomAuthException;
import com.taingy.eventmanagementsystem.exception.UnauthorizedException;
import com.taingy.eventmanagementsystem.exception.UsernameExistException;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.mapper.UserMapper;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import com.taingy.eventmanagementsystem.security.JwtUtil;
import com.taingy.eventmanagementsystem.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    public AuthRequests.AuthResponse register(AuthRequests.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameExistException("Username is already in use");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(Role.USER);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthRequests.AuthResponse(token, userMapper.toResponseDTO(user));
    }

    public AuthRequests.AuthResponse login(AuthRequests.LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new CustomAuthException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new CustomAuthException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthRequests.AuthResponse(token, userMapper.toResponseDTO(user));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public void changePassword(AuthRequests.ChangePasswordRequest request) {
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new BadRequestException("Current password is required");
        }

        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new BadRequestException("New password is required");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
