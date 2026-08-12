package com.tellinbox.tellinbox_api.feedback.repository;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackCategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for FeedbackCategory operations.
 * Provides database access methods for feedback categories.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Repository
public interface FeedbackCategoryRepository extends JpaRepository<FeedbackCategoryModel, UUID> {

    /**
     * Find all active categories ordered by sort order
     */
    List<FeedbackCategoryModel> findByIsActiveTrueOrderBySortOrder();

    /**
     * Find all default categories
     */
    List<FeedbackCategoryModel> findByIsDefaultTrueAndIsActiveTrueOrderBySortOrder();

    /**
     * Find category by title
     */
    Optional<FeedbackCategoryModel> findByTitle(String title);

    /**
     * Find category by English title
     */
    Optional<FeedbackCategoryModel> findByTitleEn(String titleEn);

    /**
     * Check if category title exists
     */
    boolean existsByTitle(String title);

    /**
     * Get all categories with their average scores for a receiver
     */
    @Query("SELECT c.id, c.title, c.titleEn, AVG(fs.score) as avgScore " +
           "FROM FeedbackCategoryModel c " +
           "LEFT JOIN FeedbackScoreModel fs ON fs.category.id = c.id AND fs.receiverId = :receiverId " +
           "WHERE c.isActive = true " +
           "GROUP BY c.id, c.title, c.titleEn " +
           "ORDER BY c.sortOrder")
    List<Object[]> findCategoriesWithAverageScores(UUID receiverId);
}
