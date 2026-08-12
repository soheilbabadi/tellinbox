package com.tellinbox.tellinbox_api.feedback.repository;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackScoreModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for FeedbackScore operations.
 * Provides database access methods for feedback scores.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Repository
public interface FeedbackScoreRepository extends JpaRepository<FeedbackScoreModel, UUID> {

    /**
     * Find all scores for a specific feedback
     */
    List<FeedbackScoreModel> findByFeedbackId(UUID feedbackId);

    /**
     * Find score for a specific feedback and category
     */
    Optional<FeedbackScoreModel> findByFeedbackIdAndCategoryId(UUID feedbackId, UUID categoryId);

    /**
     * Delete all scores for a feedback
     */
    void deleteByFeedbackId(UUID feedbackId);

    /**
     * Get average score by category for a receiver
     */
    @Query("SELECT AVG(fs.score) FROM FeedbackScoreModel fs WHERE fs.receiverId = :receiverId AND fs.category.id = :categoryId")
    Double getAverageScoreByCategory(UUID receiverId, UUID categoryId);

    /**
     * Get all scores by receiver and category
     */
    List<FeedbackScoreModel> findByReceiverIdAndCategoryId(UUID receiverId, UUID categoryId);

    /**
     * Get score distribution for a category
     */
    @Query("SELECT fs.score, COUNT(fs) FROM FeedbackScoreModel fs " +
           "WHERE fs.receiverId = :receiverId AND fs.category.id = :categoryId " +
           "GROUP BY fs.score ORDER BY fs.score")
    List<Object[]> getScoreDistribution(UUID receiverId, UUID categoryId);
}
