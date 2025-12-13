package com.taingy.eventmanagementsystem.integration;

import com.taingy.eventmanagementsystem.dto.AuthRequests;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.model.EmailOtp;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.repository.EmailOtpRepository;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Password Reset functionality
 * Tests the full stack: Controller -> Service -> Repository -> Database
 */
@Transactional
class PasswordResetIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String testEmail = "testuser@test.com";
    private String originalPassword = "password123";

    @BeforeEach
    void setUp() {
        // Clean up
        emailOtpRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user with verified email
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail(testEmail);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPasswordHash(passwordEncoder.encode(originalPassword));
        testUser.setRole(Role.USER);
        testUser.setEmailVerified(true);
        testUser.setEmailVerifiedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithAnonymousUser
    void forgotPassword_Success() throws Exception {
        AuthRequests.ForgotPasswordRequest request = new AuthRequests.ForgotPasswordRequest(testEmail);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset code has been sent to your email."))
                .andExpect(jsonPath("$.email").value(testEmail));

        // Verify OTP was created in database
        var otps = emailOtpRepository.findByEmailAndVerifiedFalse(testEmail);
        assertEquals(1, otps.size());
        EmailOtp otp = otps.get(0);
        assertNotNull(otp.getOtpCode());
        assertEquals(6, otp.getOtpCode().length());
        assertFalse(otp.isVerified());
        assertTrue(otp.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @WithAnonymousUser
    void forgotPassword_UserNotFound() throws Exception {
        AuthRequests.ForgotPasswordRequest request = new AuthRequests.ForgotPasswordRequest("nonexistent@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No account found with this email address"));
    }

    @Test
    @WithAnonymousUser
    void resetPassword_Success() throws Exception {
        // First, create an OTP for password reset
        EmailOtp otp = new EmailOtp();
        otp.setEmail(testEmail);
        otp.setOtpCode("123456");
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setVerified(false);
        emailOtpRepository.save(otp);

        String newPassword = "newSecurePassword123";
        AuthRequests.ResetPasswordWithOtpRequest request = new AuthRequests.ResetPasswordWithOtpRequest(
                testEmail,
                "123456",
                newPassword
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully. You can now log in with your new password."))
                .andExpect(jsonPath("$.email").value(testEmail));

        // Verify OTP was marked as verified
        EmailOtp updatedOtp = emailOtpRepository.findById(otp.getId()).orElseThrow();
        assertTrue(updatedOtp.isVerified());
        assertNotNull(updatedOtp.getVerifiedAt());

        // Verify password was actually changed
        User updatedUser = userRepository.findByEmail(testEmail).orElseThrow();
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPasswordHash()));
        assertFalse(passwordEncoder.matches(originalPassword, updatedUser.getPasswordHash()));
    }

    @Test
    @WithAnonymousUser
    void resetPassword_InvalidOtp() throws Exception {
        String newPassword = "newSecurePassword123";
        AuthRequests.ResetPasswordWithOtpRequest request = new AuthRequests.ResetPasswordWithOtpRequest(
                testEmail,
                "999999", // Invalid OTP
                newPassword
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP code"));

        // Verify password was NOT changed
        User user = userRepository.findByEmail(testEmail).orElseThrow();
        assertTrue(passwordEncoder.matches(originalPassword, user.getPasswordHash()));
    }

    @Test
    @WithAnonymousUser
    void resetPassword_ExpiredOtp() throws Exception {
        // Create an expired OTP
        EmailOtp expiredOtp = new EmailOtp();
        expiredOtp.setEmail(testEmail);
        expiredOtp.setOtpCode("123456");
        expiredOtp.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired
        expiredOtp.setVerified(false);
        emailOtpRepository.save(expiredOtp);

        String newPassword = "newSecurePassword123";
        AuthRequests.ResetPasswordWithOtpRequest request = new AuthRequests.ResetPasswordWithOtpRequest(
                testEmail,
                "123456",
                newPassword
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP code"));

        // Verify password was NOT changed
        User user = userRepository.findByEmail(testEmail).orElseThrow();
        assertTrue(passwordEncoder.matches(originalPassword, user.getPasswordHash()));
    }

    @Test
    @WithAnonymousUser
    void resetPassword_AlreadyUsedOtp() throws Exception {
        // Create an already verified OTP
        EmailOtp usedOtp = new EmailOtp();
        usedOtp.setEmail(testEmail);
        usedOtp.setOtpCode("123456");
        usedOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        usedOtp.setVerified(true); // Already used
        usedOtp.setVerifiedAt(LocalDateTime.now().minusMinutes(1));
        emailOtpRepository.save(usedOtp);

        String newPassword = "newSecurePassword123";
        AuthRequests.ResetPasswordWithOtpRequest request = new AuthRequests.ResetPasswordWithOtpRequest(
                testEmail,
                "123456",
                newPassword
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP code"));

        // Verify password was NOT changed
        User user = userRepository.findByEmail(testEmail).orElseThrow();
        assertTrue(passwordEncoder.matches(originalPassword, user.getPasswordHash()));
    }

    @Test
    @WithAnonymousUser
    void resetPassword_UserNotFound() throws Exception {
        // Create OTP for a non-existent user
        EmailOtp otp = new EmailOtp();
        otp.setEmail("nonexistent@test.com");
        otp.setOtpCode("123456");
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setVerified(false);
        emailOtpRepository.save(otp);

        String newPassword = "newSecurePassword123";
        AuthRequests.ResetPasswordWithOtpRequest request = new AuthRequests.ResetPasswordWithOtpRequest(
                "nonexistent@test.com",
                "123456",
                newPassword
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithAnonymousUser
    void completePasswordResetFlow_Success() throws Exception {
        // Step 1: Request password reset
        AuthRequests.ForgotPasswordRequest forgotRequest = new AuthRequests.ForgotPasswordRequest(testEmail);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk());

        // Step 2: Get the OTP from database (simulating user receiving email)
        var otps = emailOtpRepository.findByEmailAndVerifiedFalse(testEmail);
        assertEquals(1, otps.size());
        String otpCode = otps.get(0).getOtpCode();

        // Step 3: Reset password with OTP
        String newPassword = "brandNewPassword456";
        AuthRequests.ResetPasswordWithOtpRequest resetRequest = new AuthRequests.ResetPasswordWithOtpRequest(
                testEmail,
                otpCode,
                newPassword
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully. You can now log in with your new password."));

        // Step 4: Verify user can login with new password
        User updatedUser = userRepository.findByEmail(testEmail).orElseThrow();
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPasswordHash()));
        assertFalse(passwordEncoder.matches(originalPassword, updatedUser.getPasswordHash()));

        // Step 5: Verify OTP is now marked as verified and cannot be reused
        EmailOtp verifiedOtp = emailOtpRepository.findById(otps.get(0).getId()).orElseThrow();
        assertTrue(verifiedOtp.isVerified());
    }
}
