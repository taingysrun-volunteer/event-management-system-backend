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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final EmailService emailService;
    private final SendGridEmailService sendGridEmailService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                      UserMapper userMapper, OtpService otpService, EmailService emailService,
                      SendGridEmailService sendGridEmailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.otpService = otpService;
        this.emailService = emailService;
        this.sendGridEmailService = sendGridEmailService;
    }

    @Transactional
    public AuthRequests.RegisterResponse register(AuthRequests.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameExistException("Username is already in use");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already in use");
        }

        // Create user account but keep it unverified
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(Role.USER);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);
        userRepository.save(user);

        // Generate and send OTP
        String otpCode = otpService.generateOtp(request.email());

        // Use SendGrid if available, otherwise fall back to JavaMailSender
        if (sendGridEmailService != null) {
            sendGridEmailService.sendOtpEmail(request.email(), otpCode, request.firstName());
        } else {
            emailService.sendOtpEmail(request.email(), otpCode, request.firstName());
        }

        return new AuthRequests.RegisterResponse(
                "Registration successful. Please check your email for the verification code.",
                request.email()
        );
    }

    @Transactional
    public AuthRequests.AuthResponse verifyOtp(AuthRequests.VerifyOtpRequest request) {
        // Verify the OTP
        boolean isValid = otpService.verifyOtp(request.email(), request.otpCode());
        if (!isValid) {
            throw new BadRequestException("Invalid or expired OTP code");
        }

        // Update user's email verification status
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate token and return
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthRequests.AuthResponse(token, userMapper.toResponseDTO(user));
    }

    @Transactional
    public AuthRequests.RegisterResponse resendOtp(AuthRequests.ResendOtpRequest request) {
        // Check if user exists
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Check if already verified
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Generate and send new OTP
        String otpCode = otpService.generateOtp(request.email());

        // Use SendGrid if available, otherwise fall back to JavaMailSender
        if (sendGridEmailService != null) {
            sendGridEmailService.sendOtpEmail(request.email(), otpCode, user.getFirstName());
        } else {
            emailService.sendOtpEmail(request.email(), otpCode, user.getFirstName());
        }

        return new AuthRequests.RegisterResponse(
                "A new verification code has been sent to your email.",
                request.email()
        );
    }

    public AuthRequests.AuthResponse login(AuthRequests.LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new CustomAuthException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new CustomAuthException("Invalid username or password");
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new CustomAuthException("Please verify your email before logging in. Check your email for the verification code.");
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
