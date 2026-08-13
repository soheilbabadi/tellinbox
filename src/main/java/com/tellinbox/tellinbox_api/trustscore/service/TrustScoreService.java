package com.tellinbox.tellinbox_api.trustscore.service;

import com.tellinbox.tellinbox_api.feedback.dto.TrustScoreDto;

import java.util.UUID;

/**
 * Service interface for Trust Score calculation
 */
public interface TrustScoreService {

    /**
     * Calculate trust score for a user based on their feedbacks
     * 
     * @param userId the ID of the user to calculate trust score for
     * @return TrustScoreDto containing the calculated score and breakdown
     */
    TrustScoreDto calculateTrustScore(UUID userId);

    /**
     * Calculate trust score with custom date range
     * 
     * @param userId the ID of the user
     * @param monthsBack number of months to consider for recency calculation
     * @return TrustScoreDto containing the calculated score and breakdown
     */
    TrustScoreDto calculateTrustScoreWithRecency(UUID userId, Integer monthsBack);
}
