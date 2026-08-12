package com.tellinbox.tellinbox_api.trustscore.service;

import com.tellinbox.tellinbox_api.feedback.dto.TrustScoreDto;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.feedback.repository.FeedbackRepository;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service implementation for Trust Score calculation using Apache Commons Math
 * 
 * Trust Score Formula:
 * - Count Factor (20%): Logarithmic scaling of feedback count
 * - Diversity Factor (20%): Unique authors / Total feedbacks
 * - Credibility Factor (25%): Average author credibility score
 * - Recency Factor (15%): Weighted average based on feedback age
 * - Rating Factor (20%): Normalized average rating with standard deviation penalty
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrustScoreServiceImpl implements TrustScoreService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    // Weights for each component (must sum to 1.0)
    private static final double WEIGHT_COUNT = 0.20;
    private static final double WEIGHT_DIVERSITY = 0.20;
    private static final double WEIGHT_CREDIBILITY = 0.25;
    private static final double WEIGHT_RECENCY = 0.15;
    private static final double WEIGHT_RATING = 0.20;

    // Maximum scores for normalization
    private static final double MAX_FEEDBACK_COUNT_FOR_MAX_SCORE = 100.0;

    @Override
    public TrustScoreDto calculateTrustScore(Long userId) {
        return calculateTrustScoreWithRecency(userId, 12); // Default: last 12 months
    }

    @Override
    public TrustScoreDto calculateTrustScoreWithRecency(Long userId, Integer monthsBack) {
        log.info("Calculating trust score for user {} with {} months recency", userId, monthsBack);

        // Verify user exists
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Get feedbacks for the user within the date range
        Instant startDate = LocalDate.now().minusMonths(monthsBack).atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<FeedbackModel> feedbacks = feedbackRepository.findByReceiverIdAndCreatedAtAfter(userId, startDate);

        if (feedbacks.isEmpty()) {
            log.debug("No feedbacks found for user {}", userId);
            return createEmptyTrustScore();
        }

        // Calculate components
        double countFactor = calculateCountFactor(feedbacks.size());
        double diversityFactor = calculateDiversityFactor(feedbacks);
        double credibilityFactor = calculateCredibilityFactor(feedbacks);
        double recencyFactor = calculateRecencyFactor(feedbacks);
        double ratingFactor = calculateRatingFactor(feedbacks);

        // Calculate weighted trust score using Apache Commons Math
        double[] scores = {countFactor, diversityFactor, credibilityFactor, recencyFactor, ratingFactor};
        double[] weights = {WEIGHT_COUNT, WEIGHT_DIVERSITY, WEIGHT_CREDIBILITY, WEIGHT_RECENCY, WEIGHT_RATING};
        
        // Weighted mean calculation
        double trustScore = 0.0;
        for (int i = 0; i < scores.length; i++) {
            trustScore += scores[i] * weights[i];
        }

        // Normalize to 0-100 scale
        trustScore = Math.min(100.0, Math.max(0.0, trustScore));

        // Determine level
        String level = determineScoreLevel(trustScore);

        // Build response DTO
        return TrustScoreDto.builder()
                .score(Math.round(trustScore * 100.0) / 100.0)
                .level(level)
                .feedbackCount((long) feedbacks.size())
                .uniqueAuthorsCount(countUniqueAuthors(feedbacks))
                .averageAuthorCredibility(Math.round(credibilityFactor * 100.0) / 100.0)
                .recencyFactor(Math.round(recencyFactor * 100.0) / 100.0)
                .averageRating(calculateAverageRating(feedbacks))
                .components(TrustScoreDto.TrustScoreComponents.builder()
                        .countComponent(Math.round(countFactor * 100.0) / 100.0)
                        .diversityComponent(Math.round(diversityFactor * 100.0) / 100.0)
                        .credibilityComponent(Math.round(credibilityFactor * 100.0) / 100.0)
                        .recencyComponent(Math.round(recencyFactor * 100.0) / 100.0)
                        .ratingComponent(Math.round(ratingFactor * 100.0) / 100.0)
                        .build())
                .build();
    }

    /**
     * Count Factor: Uses logarithmic scaling to prevent gaming by excessive feedbacks
     * Formula: min(100, ln(count + 1) / ln(maxCount + 1) * 100)
     */
    private double calculateCountFactor(long feedbackCount) {
        if (feedbackCount == 0) return 0.0;
        
        // Using natural logarithm for smooth scaling
        double maxLog = Math.log(MAX_FEEDBACK_COUNT_FOR_MAX_SCORE + 1);
        double actualLog = Math.log(feedbackCount + 1);
        
        return Math.min(100.0, (actualLog / maxLog) * 100.0);
    }

    /**
     * Diversity Factor: Ratio of unique authors to total feedbacks
     * Higher diversity indicates more trustworthy feedback
     */
    private double calculateDiversityFactor(List<FeedbackModel> feedbacks) {
        if (feedbacks.isEmpty()) return 0.0;
        
        long uniqueAuthors = countUniqueAuthors(feedbacks);
        double diversityRatio = (double) uniqueAuthors / feedbacks.size();
        
        // Use Apache Commons Math for statistical calculations if needed
        DescriptiveStatistics stats = new DescriptiveStatistics();
        stats.addValue(diversityRatio);
        
        return diversityRatio * 100.0;
    }

    /**
     * Credibility Factor: Average credibility of feedback authors
     * Authors with higher trust scores contribute more
     */
    private double calculateCredibilityFactor(List<FeedbackModel> feedbacks) {
        if (feedbacks.isEmpty()) return 0.0;
        
        double[] credibilityScores = feedbacks.stream()
                .map(FeedbackModel::getAuthor)
                .filter(Objects::nonNull)
                .mapToDouble(author -> {
                    // Simple credibility based on user's own feedback count and profile completeness
                    int authorFeedbackCount = feedbackRepository.countByAuthorId(author.getId());
                    double profileCompleteness = calculateProfileCompleteness(author);
                    return (Math.min(authorFeedbackCount, 50) / 50.0 * 0.6 + profileCompleteness * 0.4) * 100.0;
                })
                .toArray();
        
        if (credibilityScores.length == 0) return 50.0; // Default middle score
        
        // Use Apache Commons Math for mean calculation
        return StatUtils.mean(credibilityScores);
    }

    /**
     * Recency Factor: Weighted average based on feedback age
     * Recent feedbacks have higher weight
     */
    private double calculateRecencyFactor(List<FeedbackModel> feedbacks) {
        if (feedbacks.isEmpty()) return 0.0;
        
        LocalDate now = LocalDate.now();
        double[] recencyWeights = new double[feedbacks.size()];
        
        for (int i = 0; i < feedbacks.size(); i++) {
            FeedbackModel feedback = feedbacks.get(i);
            LocalDate feedbackDate = feedback.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysAgo = ChronoUnit.DAYS.between(feedbackDate, now);
            
            // Exponential decay: weight = e^(-days/90)
            // Feedback older than 90 days gets significantly less weight
            double weight = Math.exp(-daysAgo / 90.0);
            recencyWeights[i] = weight;
        }
        
        // Calculate weighted average recency
        double totalWeight = StatUtils.sum(recencyWeights);
        if (totalWeight == 0) return 0.0;
        
        double averageRecency = totalWeight / recencyWeights.length;
        return Math.min(100.0, averageRecency * 100.0);
    }

    /**
     * Rating Factor: Average rating with standard deviation penalty
     * Consistent ratings are more trustworthy than highly variable ones
     */
    private double calculateRatingFactor(List<FeedbackModel> feedbacks) {
        if (feedbacks.isEmpty()) return 0.0;
        
        double[] ratings = feedbacks.stream()
                .filter(f -> f.getOverallRating() != null)
                .mapToDouble(FeedbackModel::getOverallRating)
                .toArray();
        
        if (ratings.length == 0) return 50.0;
        
        // Calculate mean rating (normalized to 0-100 scale, assuming 1-5 star system)
        double meanRating = StatUtils.mean(ratings);
        double normalizedRating = ((meanRating - 1.0) / 4.0) * 100.0;
        
        // Calculate standard deviation penalty
        double stdDev = StatUtils.variance(ratings);
        double penalty = Math.min(20.0, stdDev * 5.0); // Max 20 point penalty
        
        return Math.max(0.0, normalizedRating - penalty);
    }

    /**
     * Calculate simple average rating from feedbacks
     */
    private double calculateAverageRating(List<FeedbackModel> feedbacks) {
        if (feedbacks.isEmpty()) return 0.0;
        
        return feedbacks.stream()
                .filter(f -> f.getOverallRating() != null)
                .mapToDouble(FeedbackModel::getOverallRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Count unique authors in feedback list
     */
    private long countUniqueAuthors(List<FeedbackModel> feedbacks) {
        return feedbacks.stream()
                .map(FeedbackModel::getAuthor)
                .filter(Objects::nonNull)
                .map(UserModel::getId)
                .distinct()
                .count();
    }

    /**
     * Simple profile completeness calculation (0.0 to 1.0)
     */
    private double calculateProfileCompleteness(UserModel user) {
        double completeness = 0.0;
        
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) completeness += 0.15;
        if (user.getLastName() != null && !user.getLastName().isBlank()) completeness += 0.15;
        if (user.getEmail() != null && !user.getEmail().isBlank()) completeness += 0.20;
        if (user.getMobile() != null && !user.getMobile().isBlank()) completeness += 0.15;
        if (user.getBio() != null && !user.getBio().isBlank()) completeness += 0.15;
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) completeness += 0.10;
        if (user.getTitle() != null && !user.getTitle().isBlank()) completeness += 0.10;
        
        return completeness;
    }

    /**
     * Determine trust score level based on score value
     */
    private String determineScoreLevel(double score) {
        if (score >= 80.0) return "VERY_HIGH";
        if (score >= 60.0) return "HIGH";
        if (score >= 40.0) return "MEDIUM";
        if (score >= 20.0) return "LOW";
        return "VERY_LOW";
    }

    /**
     * Create empty trust score when no feedbacks exist
     */
    private TrustScoreDto createEmptyTrustScore() {
        return TrustScoreDto.builder()
                .score(0.0)
                .level("NO_DATA")
                .feedbackCount(0L)
                .uniqueAuthorsCount(0L)
                .averageAuthorCredibility(0.0)
                .recencyFactor(0.0)
                .averageRating(0.0)
                .components(TrustScoreDto.TrustScoreComponents.builder()
                        .countComponent(0.0)
                        .diversityComponent(0.0)
                        .credibilityComponent(0.0)
                        .recencyComponent(0.0)
                        .ratingComponent(0.0)
                        .build())
                .build();
    }
}
