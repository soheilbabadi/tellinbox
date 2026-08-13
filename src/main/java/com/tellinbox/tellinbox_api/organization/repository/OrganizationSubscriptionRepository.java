package com.tellinbox.tellinbox_api.organization.repository;

import com.tellinbox.tellinbox_api.organization.entity.OrganizationSubscriptionModel;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for OrganizationSubscription entity operations.
 */
@Repository
public interface OrganizationSubscriptionRepository extends JpaRepository<OrganizationSubscriptionModel, UUID> {

    /**
     * Find subscriptions by organization
     */
    List<OrganizationSubscriptionModel> findByOrganizationId(UUID organizationId);

    /**
     * Find active subscription for an organization
     */
    @Query("SELECT s FROM OrganizationSubscriptionModel s WHERE s.organization.id = :organizationId AND s.status = 'ACTIVE' AND s.endDate > :now")
    Optional<OrganizationSubscriptionModel> findActiveSubscription(@Param("organizationId") UUID organizationId, @Param("now") LocalDateTime now);

    /**
     * Find all active subscriptions
     */
    @Query("SELECT s FROM OrganizationSubscriptionModel s WHERE s.status = 'ACTIVE' AND s.endDate > :now")
    List<OrganizationSubscriptionModel> findAllActiveSubscriptions(@Param("now") LocalDateTime now);

    /**
     * Count active subscriptions by plan
     */
    @Query("SELECT COUNT(s) FROM OrganizationSubscriptionModel s WHERE s.plan = :plan AND s.status = 'ACTIVE'")
    long countByPlan(@Param("plan") com.tellinbox.tellinbox_api.subscription.enums.SubscriptionPlan plan);

    /**
     * Find subscriptions expiring soon
     */
    @Query("SELECT s FROM OrganizationSubscriptionModel s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :now AND :threshold")
    List<OrganizationSubscriptionModel> findExpiringSoon(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);

    /**
     * Find subscriptions by status
     */
    List<OrganizationSubscriptionModel> findByStatus(SubscriptionStatus status);

    /**
     * Check if organization has active subscription
     */
    @Query("SELECT COUNT(s) > 0 FROM OrganizationSubscriptionModel s WHERE s.organization.id = :organizationId AND s.status = 'ACTIVE' AND s.endDate > :now")
    boolean hasActiveSubscription(@Param("organizationId") UUID organizationId, @Param("now") LocalDateTime now);
}
