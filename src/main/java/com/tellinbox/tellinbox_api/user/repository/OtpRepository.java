package com.tellinbox.tellinbox_api.user.repository;

import com.tellinbox.tellinbox_api.user.model.OtpModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpModel, UUID> {

    /**
     * Find the most recent unused OTP for an identifier
     */
    @Query("SELECT o FROM OtpModel o WHERE o.identifier = :identifier AND o.isUsed = false ORDER BY o.createdAt DESC")
    Optional<OtpModel> findLatestUnusedByIdentifier(@Param("identifier") String identifier);

    /**
     * Find all unused OTPs for an identifier
     */
    List<OtpModel> findByIdentifierAndIsUsedFalse(String identifier);

    /**
     * Delete expired OTPs
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpModel o WHERE o.expiresAt < :now")
    int deleteExpiredOtps(@Param("now") LocalDateTime now);

    /**
     * Count recent OTP requests from an IP (for rate limiting)
     */
    @Query("SELECT COUNT(o) FROM OtpModel o WHERE o.requestedIp = :ip AND o.createdAt > :since")
    long countRecentRequestsByIp(@Param("ip") String ip, @Param("since") LocalDateTime since);

    /**
     * Find OTP by code and identifier (for verification)
     */
    @Query("SELECT o FROM OtpModel o WHERE o.code = :code AND o.identifier = :identifier AND o.isUsed = false")
    Optional<OtpModel> findByCodeAndIdentifier(@Param("code") String code, @Param("identifier") String identifier);
}
