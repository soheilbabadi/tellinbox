package com.tellinbox.tellinbox_api.feedback.service;

import com.tellinbox.tellinbox_api.feedback.dto.CategoryScoreDto;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackCategoryModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for feedback category operations.
 * Defines the contract for feedback category management functionality.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
public interface FeedbackCategoryService {

    /**
     * Get all active categories
     */
    List<FeedbackCategoryModel> getAllActiveCategories();

    /**
     * Get all default categories
     */
    List<FeedbackCategoryModel> getDefaultCategories();

    /**
     * Get category by ID
     */
    Optional<FeedbackCategoryModel> getCategoryById(UUID id);

    /**
     * Get category scores with averages for a receiver
     */
    List<CategoryScoreDto> getCategoryScoresWithAverages(UUID receiverId);

    /**
     * Create a new category
     */
    FeedbackCategoryModel createCategory(FeedbackCategoryModel category);

    /**
     * Update an existing category
     */
    FeedbackCategoryModel updateCategory(UUID categoryId, FeedbackCategoryModel category);

    /**
     * Delete a category (soft delete)
     */
    void deleteCategory(UUID categoryId);
}
