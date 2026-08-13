package com.tellinbox.tellinbox_api.organization.service;

import com.tellinbox.tellinbox_api.organization.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for organization operations.
 */
public interface OrganizationService {

    /**
     * Create a new organization
     */
    OrganizationResponse createOrganization(CreateOrganizationRequest request, UUID userId);

    /**
     * Get organization by ID
     */
    OrganizationResponse getOrganizationById(UUID id);

    /**
     * Get all organizations owned by a user
     */
    List<OrganizationResponse> getOrganizationsByOwnerId(UUID userId);

    /**
     * Search organizations by name
     */
    Page<OrganizationResponse> searchOrganizations(String keyword, int page, int size);

    /**
     * Update organization details
     */
    OrganizationResponse updateOrganization(UUID id, CreateOrganizationRequest request);

    /**
     * Upload organization logo
     */
    OrganizationResponse uploadLogo(UUID organizationId, MultipartFile file);

    /**
     * Delete organization (soft delete)
     */
    void deleteOrganization(UUID id);

    /**
     * Get subscription rates
     */
    List<SubscriptionRateResponse> getSubscriptionRates();

    /**
     * Purchase subscription for organization
     */
    SubscriptionResponse purchaseSubscription(UUID organizationId, PurchaseSubscriptionRequest request);

    /**
     * Get active subscription for organization
     */
    SubscriptionResponse getActiveSubscription(UUID organizationId);

    /**
     * Get all subscriptions for organization
     */
    List<SubscriptionResponse> getOrganizationSubscriptions(UUID organizationId);

    /**
     * Send invitation to join organization
     */
    void sendInvitation(SendInvitationRequest request, UUID senderId);

    /**
     * Accept invitation and join organization
     */
    void acceptInvitation(String token, UUID userId);
}
