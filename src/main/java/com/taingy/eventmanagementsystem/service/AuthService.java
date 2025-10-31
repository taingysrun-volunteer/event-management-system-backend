package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.dto.AuthRequests;
import com.taingy.eventmanagementsystem.exception.CustomAuthException;
import com.taingy.eventmanagementsystem.exception.UsernameExistException;
import com.taingy.eventmanagementsystem.model.Role;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import com.taingy.eventmanagementsystem.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthRequests.AuthResponse register(AuthRequests.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameExistException("Username is already in use");
        }

        User user = new User(request.username(),
                request.firstName(),
                request.lastName(),
                Role.USER,
                passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthRequests.AuthResponse(token);
    }

    public AuthRequests.AuthResponse login(AuthRequests.LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new CustomAuthException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new CustomAuthException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthRequests.AuthResponse(token);
    }
}
