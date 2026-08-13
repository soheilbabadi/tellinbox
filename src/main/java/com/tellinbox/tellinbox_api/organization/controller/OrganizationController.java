package com.tellinbox.tellinbox_api.organization.controller;

import com.tellinbox.tellinbox_api.organization.dto.*;
import com.tellinbox.tellinbox_api.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controller for organization management operations.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Create a new organization
     */
    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal(expression = "userId") UUID userId) {
        return ResponseEntity.ok(organizationService.createOrganization(request, userId));
    }

    /**
     * Get organization by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    /**
     * Get all organizations owned by current user
     */
    @GetMapping("/my-organizations")
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations(
            @AuthenticationPrincipal(expression = "id") UUID userId) {
        return ResponseEntity.ok(organizationService.getOrganizationsByOwnerId(userId));
    }

    /**
     * Search organizations
     */
    @GetMapping("/search")
    public ResponseEntity<Page<OrganizationResponse>> searchOrganizations(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(organizationService.searchOrganizations(keyword, page, size));
    }

    /**
     * Update organization
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.updateOrganization(id, request));
    }

    /**
     * Upload organization logo
     */
    @PostMapping("/{id}/logo")
    public ResponseEntity<OrganizationResponse> uploadLogo(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(organizationService.uploadLogo(id, file));
    }

    /**
     * Delete organization
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get subscription rates
     */
    @GetMapping("/subscription-rates")
    public ResponseEntity<List<SubscriptionRateResponse>> getSubscriptionRates() {
        return ResponseEntity.ok(organizationService.getSubscriptionRates());
    }

    /**
     * Purchase subscription
     */
    @PostMapping("/{id}/subscriptions")
    public ResponseEntity<SubscriptionResponse> purchaseSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseSubscriptionRequest request) {
        return ResponseEntity.ok(organizationService.purchaseSubscription(id, request));
    }

    /**
     * Get active subscription
     */
    @GetMapping("/{id}/subscriptions/active")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getActiveSubscription(id));
    }

    /**
     * Get all subscriptions for organization
     */
    @GetMapping("/{id}/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getOrganizationSubscriptions(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationSubscriptions(id));
    }

    /**
     * Send invitation to join organization
     */
    @PostMapping("/invitations")
    public ResponseEntity<Void> sendInvitation(
            @Valid @RequestBody SendInvitationRequest request,
            @AuthenticationPrincipal(expression = "id") UUID senderId) {
        organizationService.sendInvitation(request, senderId);
        return ResponseEntity.accepted().build();
    }

    /**
     * Accept invitation
     */
    @PostMapping("/invitations/accept")
    public ResponseEntity<Void> acceptInvitation(
            @RequestParam String token,
            @AuthenticationPrincipal(expression = "id") UUID userId) {
        organizationService.acceptInvitation(token, userId);
        return ResponseEntity.ok().build();
    }
}
