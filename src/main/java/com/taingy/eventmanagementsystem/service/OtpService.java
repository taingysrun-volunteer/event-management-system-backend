package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.model.EmailOtp;
import com.taingy.eventmanagementsystem.repository.EmailOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom random = new SecureRandom();

    private final EmailOtpRepository emailOtpRepository;

    @Autowired
    public OtpService(EmailOtpRepository emailOtpRepository) {
        this.emailOtpRepository = emailOtpRepository;
    }

    public String generateOtp(String email) {
        List<EmailOtp> existingOtps = emailOtpRepository.findByEmailAndVerifiedFalse(email);
        if (!existingOtps.isEmpty()) {
            logger.info("Invalidating {} existing OTP(s) for email: {}", existingOtps.size(), email);
        }

        // Generate a 6-digit OTP
        String otpCode = String.format("%06d", random.nextInt(1000000));

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtpCode(otpCode);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        emailOtp.setVerified(false);

        emailOtpRepository.save(emailOtp);
        logger.info("Generated OTP for email: {}", email);

        return otpCode;
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        Optional<EmailOtp> otpOpt = emailOtpRepository.findByEmailAndOtpCodeAndVerifiedFalseAndExpiresAtAfter(
                email, otpCode, LocalDateTime.now());

        if (otpOpt.isEmpty()) {
            logger.warn("Invalid or expired OTP attempt for email: {}", email);
            return false;
        }

        EmailOtp emailOtp = otpOpt.get();
        emailOtp.setVerified(true);
        emailOtp.setVerifiedAt(LocalDateTime.now());
        emailOtpRepository.save(emailOtp);

        logger.info("OTP verified successfully for email: {}", email);
        return true;
    }

    public boolean hasVerifiedOtp(String email) {
        List<EmailOtp> otps = emailOtpRepository.findByEmailAndVerifiedFalse(email);
        return otps.stream().anyMatch(EmailOtp::isVerified);
    }


    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredOtps() {
        try {
            emailOtpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
            logger.info("Cleaned up expired OTPs");
        } catch (Exception e) {
            logger.error("Error cleaning up expired OTPs", e);
        }
    }
}
