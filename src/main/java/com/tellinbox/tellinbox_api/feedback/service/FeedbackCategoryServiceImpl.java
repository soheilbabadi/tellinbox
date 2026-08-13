package com.tellinbox.tellinbox_api.feedback.service;

import com.tellinbox.tellinbox_api.common.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.feedback.dto.CategoryScoreDto;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackCategoryModel;
import com.tellinbox.tellinbox_api.feedback.repository.FeedbackCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for feedback category operations.
 * Provides business logic for feedback category management.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackCategoryServiceImpl implements FeedbackCategoryService {

    private final MessageSource messageSource;
    private final FeedbackCategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackCategoryModel> getAllActiveCategories() {
        log.debug("Finding all active categories");
        return categoryRepository.findByIsActiveTrueOrderBySortOrder();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackCategoryModel> getDefaultCategories() {
        log.debug("Finding all default categories");
        return categoryRepository.findByIsDefaultTrueAndIsActiveTrueOrderBySortOrder();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FeedbackCategoryModel> getCategoryById(UUID id) {
        log.debug("Finding category by ID: {}", id);
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryScoreDto> getCategoryScoresWithAverages(UUID receiverId) {
        log.debug("Getting category scores with averages for receiver: {}", receiverId);
        
        List<FeedbackCategoryModel> categories = getAllActiveCategories();
        
        return categories.stream()
            .map(category -> {
                Double avgScore = categoryRepository.findCategoriesWithAverageScores(receiverId)
                    .stream()
                    .filter(result -> result[0].equals(category.getId()))
                    .map(result -> (Double) result[3])
                    .findFirst()
                    .orElse(0.0);
                
                return CategoryScoreDto.builder()
                    .categoryId(category.getId())
                    .categoryTitle(category.getTitle())
                    .categoryTitleEn(category.getTitleEn())
                    .score(avgScore != null ? avgScore.intValue() : 0)
                    .minScore(category.getMinScore())
                    .maxScore(category.getMaxScore())
                    .icon(category.getIcon())
                    .color(category.getColor())
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeedbackCategoryModel createCategory(FeedbackCategoryModel category) {
        log.info("Creating new category: {}", category.getTitle());
        
        if (categoryRepository.existsByTitle(category.getTitle())) {
            throw new ResourceAlreadyExistsException(getMessage("error.ResourceAlreadyExistsException.عنوان_دسته_بندی_تکراری_است"));
        }
        
        category.setCreatedAt(LocalDateTime.now());
        category.setIsDeleted(false);
        FeedbackCategoryModel savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return savedCategory;
    }

    @Override
    @Transactional
    public FeedbackCategoryModel updateCategory(UUID categoryId, FeedbackCategoryModel updatedCategory) {
        log.info("Updating category: {}", categoryId);
        
        FeedbackCategoryModel existingCategory = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.دسته_بندی_یافت_نشد")));
        
        if (updatedCategory.getTitle() != null) {
            existingCategory.setTitle(updatedCategory.getTitle());
        }
        if (updatedCategory.getTitleEn() != null) {
            existingCategory.setTitleEn(updatedCategory.getTitleEn());
        }
        if (updatedCategory.getDescription() != null) {
            existingCategory.setDescription(updatedCategory.getDescription());
        }
        if (updatedCategory.getIcon() != null) {
            existingCategory.setIcon(updatedCategory.getIcon());
        }
        if (updatedCategory.getColor() != null) {
            existingCategory.setColor(updatedCategory.getColor());
        }
        if (updatedCategory.getSortOrder() != null) {
            existingCategory.setSortOrder(updatedCategory.getSortOrder());
        }
        if (updatedCategory.getIsActive() != null) {
            existingCategory.setIsActive(updatedCategory.getIsActive());
        }
        if (updatedCategory.getMinScore() != null) {
            existingCategory.setMinScore(updatedCategory.getMinScore());
        }
        if (updatedCategory.getMaxScore() != null) {
            existingCategory.setMaxScore(updatedCategory.getMaxScore());
        }
        
        existingCategory.setUpdatedAt(LocalDateTime.now());
        FeedbackCategoryModel savedCategory = categoryRepository.save(existingCategory);
        log.info("Category updated successfully: {}", categoryId);
        
        return savedCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        log.info("Soft deleting category: {}", categoryId);
        
        FeedbackCategoryModel category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.دسته_بندی_یافت_نشد")));
        
        category.setIsDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
        
        log.info("Category soft deleted successfully: {}", categoryId);
    }

    /**
     * Get localized message from messages.properties
     * @param key Message key
     * @param args Optional arguments for message formatting
     * @return Localized message
     */
    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, java.util.Locale.forLanguageTag("fa"));
    }

    }
