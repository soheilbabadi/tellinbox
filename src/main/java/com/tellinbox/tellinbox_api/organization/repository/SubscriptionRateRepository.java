package com.tellinbox.tellinbox_api.organization.repository;

import com.tellinbox.tellinbox_api.organization.entity.SubscriptionRateModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SubscriptionRate entity operations.
 */
@Repository
public interface SubscriptionRateRepository extends JpaRepository<SubscriptionRateModel, UUID> {

    /**
     * Find rate by plan and billing cycle
     */
    Optional<SubscriptionRateModel> findByPlanAndBillingCycle(
        com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan plan,
        com.tellinbox.tellinbox_api.organization.enums.BillingCycle billingCycle
    );

    /**
     * Find all rates for a specific plan
     */
    List<SubscriptionRateModel> findByPlan(com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan plan);

    /**
     * Find all rates for a specific billing cycle
     */
    List<SubscriptionRateModel> findByBillingCycle(com.tellinbox.tellinbox_api.organization.enums.BillingCycle billingCycle);

    /**
     * Find all active rates
     */
    @Query("SELECT sr FROM SubscriptionRateModel sr WHERE sr.isActive = true ORDER BY sr.displayOrder ASC")
    List<SubscriptionRateModel> findAllActiveRates();

    /**
     * Find active rates for a plan
     */
    @Query("SELECT sr FROM SubscriptionRateModel sr WHERE sr.plan = :plan AND sr.isActive = true ORDER BY sr.displayOrder ASC")
    List<SubscriptionRateModel> findActiveRatesByPlan(@Param("plan") com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan plan);

    /**
     * Find recommended rates
     */
    @Query("SELECT sr FROM SubscriptionRateModel sr WHERE sr.isRecommended = true AND sr.isActive = true")
    List<SubscriptionRateModel> findRecommendedRates();

    /**
     * Check if rate exists for plan and billing cycle
     */
    boolean existsByPlanAndBillingCycle(
        com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan plan,
        com.tellinbox.tellinbox_api.organization.enums.BillingCycle billingCycle
    );

    /**
     * Find valid rates at current time
     */
    @Query("SELECT sr FROM SubscriptionRateModel sr WHERE sr.isActive = true AND (sr.validFrom IS NULL OR sr.validFrom <= :now) AND (sr.validUntil IS NULL OR sr.validUntil >= :now)")
    List<SubscriptionRateModel> findValidRates(@Param("now") LocalDateTime now);
}
