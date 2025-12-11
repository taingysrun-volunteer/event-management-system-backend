package com.taingy.eventmanagementsystem.repository;

import com.taingy.eventmanagementsystem.model.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
    Optional<EmailOtp> findByEmailAndOtpCodeAndVerifiedFalseAndExpiresAtAfter(
            String email, String otpCode, LocalDateTime now);

    List<EmailOtp> findByEmailAndVerifiedFalse(String email);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
