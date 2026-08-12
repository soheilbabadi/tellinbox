package com.tellinbox.tellinbox_api.trustscore.controller;

import com.tellinbox.tellinbox_api.feedback.dto.TrustScoreDto;
import com.tellinbox.tellinbox_api.trustscore.service.TrustScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Trust Score management
 */
@RestController
@RequestMapping("/api/v1/trust-score")
@RequiredArgsConstructor
public class TrustScoreController {

    private final TrustScoreService trustScoreService;

    /**
     * Get trust score for a specific user
     * 
     * @param userId the ID of the user to get trust score for
     * @return TrustScoreDto with calculated score and breakdown
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TrustScoreDto> getTrustScore(@PathVariable Long userId) {
        TrustScoreDto trustScore = trustScoreService.calculateTrustScore(userId);
        return ResponseEntity.ok(trustScore);
    }

    /**
     * Get trust score for a specific user with custom recency period
     * 
     * @param userId the ID of the user
     * @param monthsBack number of months to consider (default: 12)
     * @return TrustScoreDto with calculated score and breakdown
     */
    @GetMapping("/{userId}/custom")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TrustScoreDto> getTrustScoreWithRecency(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "12") Integer monthsBack) {
        TrustScoreDto trustScore = trustScoreService.calculateTrustScoreWithRecency(userId, monthsBack);
        return ResponseEntity.ok(trustScore);
    }

    /**
     * Get trust score for the currently authenticated user
     * 
     * @param userId the ID of the authenticated user (from security context)
     * @return TrustScoreDto with calculated score and breakdown
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TrustScoreDto> getMyTrustScore(
            @RequestAttribute("userId") Long userId) {
        TrustScoreDto trustScore = trustScoreService.calculateTrustScore(userId);
        return ResponseEntity.ok(trustScore);
    }
}
