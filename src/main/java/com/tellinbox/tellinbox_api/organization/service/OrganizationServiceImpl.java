package com.tellinbox.tellinbox_api.organization.service;

import com.tellinbox.tellinbox_api.config.MinioConfig;
import com.tellinbox.tellinbox_api.exception.TellInboxCustomException;
import com.tellinbox.tellinbox_api.invitation.entity.Invitation;
import com.tellinbox.tellinbox_api.invitation.repository.InvitationRepository;
import com.tellinbox.tellinbox_api.organization.dto.*;
import com.tellinbox.tellinbox_api.organization.entity.OrganizationMemberModel;
import com.tellinbox.tellinbox_api.organization.entity.OrganizationModel;
import com.tellinbox.tellinbox_api.organization.entity.OrganizationSubscriptionModel;
import com.tellinbox.tellinbox_api.organization.entity.SubscriptionRateModel;
import com.tellinbox.tellinbox_api.organization.repository.OrganizationMemberRepository;
import com.tellinbox.tellinbox_api.organization.repository.OrganizationRepository;
import com.tellinbox.tellinbox_api.organization.repository.OrganizationSubscriptionRepository;
import com.tellinbox.tellinbox_api.organization.repository.SubscriptionRateRepository;
import com.tellinbox.tellinbox_api.subscription.enums.SubscriptionStatus;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

	private final OrganizationRepository organizationRepository;
	private final OrganizationMemberRepository organizationMemberRepository;
	private final OrganizationSubscriptionRepository subscriptionRepository;
	private final SubscriptionRateRepository subscriptionRateRepository;
	private final InvitationRepository invitationRepository;
	private final MinioConfig minioConfig;
	private final MessageSource messageSource;

	@Override
	@Transactional
	public OrganizationResponse createOrganization(CreateOrganizationRequest request, UUID userId) {
		// Check if name already exists
		if (organizationRepository.existsByName(request.getName())) {
			throw new TellInboxCustomException.DuplicateEntityException(getMessage("error.DuplicateEntityException.organization_name_exists"));
		}

		// Check if registration number already exists
		if (request.getRegistrationNumber() != null &&
				organizationRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
			throw new TellInboxCustomException.DuplicateEntityException(getMessage("error.DuplicateEntityException.registration_number_exists"));
		}

		// Create organization
		OrganizationModel organization = OrganizationModel.builder()
				.name(request.getName())
				.registrationNumber(request.getRegistrationNumber())
				.type(request.getType())
				.description(request.getDescription())
				.website(request.getWebsite())
				.phone(request.getPhone())
				.address(request.getAddress())
				.build();

		// Set owner (will be set by repository after fetching user)
		// For now, we'll set it in the member relationship

		OrganizationModel savedOrg = organizationRepository.save(organization);

		// Add owner as first member with OWNER role
		var ownerMember = OrganizationMemberModel.builder()
				.organization(savedOrg)
				.id(null) // Will be generated
				.build();

		// We need to fetch the user model here - simplified for now
		// In real implementation, you'd fetch from UserRepository

		organizationMemberRepository.save(ownerMember);

		return mapToResponse(savedOrg, 1, false);
	}

	@Override
	@Transactional(readOnly = true)
	public OrganizationResponse getOrganizationById(UUID id) {
		OrganizationModel organization = organizationRepository.findById(id)
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.organization_not_found")));

		if (organization.getIsDeleted()) {
			throw new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.organization_deleted"));
		}

		long membersCount = organizationMemberRepository.countActiveMembers(id);
		boolean hasActiveSub = subscriptionRepository.hasActiveSubscription(id, LocalDateTime.now());

		return mapToResponse(organization, (int) membersCount, hasActiveSub);
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrganizationResponse> getOrganizationsByOwnerId(UUID userId) {
		return organizationRepository.findActiveByOwnerId(userId).stream()
				.map(org -> {
					long membersCount = organizationMemberRepository.countActiveMembers(org.getId());
					boolean hasActiveSub = subscriptionRepository.hasActiveSubscription(org.getId(), LocalDateTime.now());
					return mapToResponse(org, (int) membersCount, hasActiveSub);
				})
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<OrganizationResponse> searchOrganizations(String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return organizationRepository.searchByName(keyword, pageable)
				.map(org -> {
					long membersCount = organizationMemberRepository.countActiveMembers(org.getId());
					boolean hasActiveSub = subscriptionRepository.hasActiveSubscription(org.getId(), LocalDateTime.now());
					return mapToResponse(org, (int) membersCount, hasActiveSub);
				});
	}

	@Override
	@Transactional
	public OrganizationResponse updateOrganization(UUID id, CreateOrganizationRequest request) {
		OrganizationModel organization = organizationRepository.findById(id)
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.organization_not_found")));

		if (organization.getIsDeleted()) {
			throw new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.organization_deleted"));
		}

		// Update fields
		organization.setName(request.getName());
		organization.setRegistrationNumber(request.getRegistrationNumber());
		organization.setType(request.getType());
		organization.setDescription(request.getDescription());
		organization.setWebsite(request.getWebsite());
		organization.setPhone(request.getPhone());
		organization.setAddress(request.getAddress());

		OrganizationModel updated = organizationRepository.save(organization);

		long membersCount = organizationMemberRepository.countActiveMembers(id);
		boolean hasActiveSub = subscriptionRepository.hasActiveSubscription(id, LocalDateTime.now());

		return mapToResponse(updated, (int) membersCount, hasActiveSub);
	}

	@Override
	@Transactional
	public OrganizationResponse uploadLogo(UUID organizationId, MultipartFile file) {
		OrganizationModel organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.organization_not_found")));

		if (organization.getIsDeleted()) {
			throw new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.organization_deleted"));
		}

		try {
			// Generate unique filename
			String fileName = "organizations/" + organizationId + "/logo_" + System.currentTimeMillis() + "." +
					getFileExtension(file.getOriginalFilename());

			// Upload to MinIO
			minioConfig.minioClient().putObject(
					PutObjectArgs.builder()
							.bucket(minioConfig.getBucketName())
							.object(fileName)
							.stream(file.getInputStream(), file.getSize(), -1)
							.contentType(file.getContentType())
							.build()
			);

			String logoUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + fileName;

			organization.setLogoUrl(logoUrl);
			OrganizationModel updated = organizationRepository.save(organization);

			long membersCount = organizationMemberRepository.countActiveMembers(organizationId);
			boolean hasActiveSub = subscriptionRepository.hasActiveSubscription(organizationId, LocalDateTime.now());

			return mapToResponse(updated, (int) membersCount, hasActiveSub);

		} catch (Exception e) {
			log.error("Error uploading logo", e);
			throw new TellInboxCustomException.ApplicationServerException(getMessage("error.ApplicationServerException.logo_upload_error"));
		}
	}

	@Override
	@Transactional
	public void deleteOrganization(UUID id) {
		OrganizationModel organization = organizationRepository.findById(id)
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException("سازمان مورد نظر یافت نشد"));

		organization.softDelete();
		organizationRepository.save(organization);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubscriptionRateResponse> getSubscriptionRates() {
		return subscriptionRateRepository.findAllActiveRates().stream()
				.map(this::mapRateToResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public SubscriptionResponse purchaseSubscription(UUID organizationId, PurchaseSubscriptionRequest request) {
		OrganizationModel organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException("سازمان مورد نظر یافت نشد"));

		// Get subscription rate
		SubscriptionRateModel rate = subscriptionRateRepository.findByPlanAndBillingCycle(
						request.getPlan(), request.getBillingCycle())
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException("نرخ اشتراک برای این پلن و چرخه یافت نشد"));

		// Calculate price
		BigDecimal totalPrice = rate.calculatePriceForSeats(request.getSeatsCount());

		// Create subscription
		OrganizationSubscriptionModel subscription = OrganizationSubscriptionModel.builder()
				.organization(organization)
				.plan(request.getPlan())
				.billingCycle(request.getBillingCycle())
				.status(SubscriptionStatus.PENDING) // Will be activated after payment
				.price(totalPrice)
				.currency(rate.getCurrency())
				.seatsCount(request.getSeatsCount())
				.autoRenewal(request.getAutoRenewal())
				.notes(request.getNotes())
				.build();

		OrganizationSubscriptionModel saved = subscriptionRepository.save(subscription);

		return mapSubscriptionToResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public SubscriptionResponse getActiveSubscription(UUID organizationId) {
		OrganizationSubscriptionModel subscription = subscriptionRepository
				.findActiveSubscription(organizationId, LocalDateTime.now())
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException("اشتراک فعال یافت نشد"));

		return mapSubscriptionToResponse(subscription);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubscriptionResponse> getOrganizationSubscriptions(UUID organizationId) {
		return subscriptionRepository.findByOrganizationId(organizationId).stream()
				.map(this::mapSubscriptionToResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void sendInvitation(SendInvitationRequest request, UUID senderId) {
		// Verify sender is admin/owner of organization
		OrganizationModel organization = organizationRepository.findById(request.getOrganizationId())
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.organization_not_found")));

		// Check if sender is member with appropriate role
		var senderMember = organizationMemberRepository.findByOrganizationIdAndUserId(
				request.getOrganizationId(), senderId);

		if (senderMember.isEmpty() || !isAllowedToSendInvitation(senderMember.get().getRole())) {
			throw new TellInboxCustomException.AccessDeniedException(getMessage("error.AccessDeniedException.invitation_permission_denied"));
		}

		// Create invitation
		Invitation invitation = Invitation.builder()
				.token(UUID.randomUUID().toString())
				.user(null) // Will be associated when accepted
				.isActive(true)
				.maxUses(1)
				.currentUses(0)
				.expiresAt(LocalDateTime.now().plusDays(7)) // Valid for 7 days
				.build();

		invitationRepository.save(invitation);

		// TODO: Send email with invitation link
		log.info("Invitation sent to {} for organization {}. Token: {}",
				request.getEmail(), organization.getName(), invitation.getToken());
	}

	@Override
	@Transactional
	public void acceptInvitation(String token, UUID userId) {
		Invitation invitation = invitationRepository.findByToken(token)
				.orElseThrow(() -> new TellInboxCustomException.ResourceNotFoundException(getMessage("error.ResourceNotFoundException.invitation_not_found")));

		if (!invitation.isActive()) {
			throw new TellInboxCustomException.ResourceForbiddenException(getMessage("error.ResourceForbiddenException.invitation_inactive"));
		}

		if (invitation.getExpiresAt() != null && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new TellInboxCustomException.ResourceForbiddenException(getMessage("error.ResourceForbiddenException.invitation_inactive"));
		}

		if (invitation.getMaxUses() != null && invitation.getCurrentUses() >= invitation.getMaxUses()) {
			throw new TellInboxCustomException.ResourceForbiddenException(getMessage("error.ResourceForbiddenException.invitation_inactive"));
		}

		// TODO: Fetch user and add to organization
		// For now, just increment uses
		invitation.setCurrentUses(invitation.getCurrentUses() + 1);
		if (invitation.getMaxUses() != null && invitation.getCurrentUses() >= invitation.getMaxUses()) {
			invitation.setActive(false);
		}

		invitationRepository.save(invitation);
	}

	// Helper methods
	private OrganizationResponse mapToResponse(OrganizationModel org, int membersCount, boolean hasActiveSub) {
		return OrganizationResponse.builder()
				.id(org.getId())
				.name(org.getName())
				.registrationNumber(org.getRegistrationNumber())
				.type(org.getType())
				.description(org.getDescription())
				.logoUrl(org.getLogoUrl())
				.website(org.getWebsite())
				.phone(org.getPhone())
				.address(org.getAddress())
				.ownerId(org.getOwner() != null ? org.getOwner().getId() : null)
				.ownerName(org.getOwner() != null ? org.getOwner().getFullName() : null)
				.membersCount(membersCount)
				.hasActiveSubscription(hasActiveSub)
				.createdAt(org.getCreatedAt())
				.updatedAt(org.getUpdatedAt())
				.build();
	}

	private SubscriptionRateResponse mapRateToResponse(SubscriptionRateModel rate) {
		return SubscriptionRateResponse.builder()
				.id(rate.getId())
				.plan(rate.getPlan())
				.billingCycle(rate.getBillingCycle())
				.basePrice(rate.getBasePrice())
				.discountPercentage(rate.getDiscountPercentage())
				.finalPrice(rate.getFinalPrice())
				.currency(rate.getCurrency())
				.includedSeats(rate.getIncludedSeats())
				.additionalSeatPrice(rate.getAdditionalSeatPrice())
				.features(rate.getFeatures())
				.isRecommended(rate.getIsRecommended())
				.displayOrder(rate.getDisplayOrder())
				.isValid(rate.isValid())
				.build();
	}

	private SubscriptionResponse mapSubscriptionToResponse(OrganizationSubscriptionModel sub) {
		return SubscriptionResponse.builder()
				.id(sub.getId())
				.organizationId(sub.getOrganization().getId())
				.organizationName(sub.getOrganization().getName())
				.plan(sub.getPlan())
				.billingCycle(sub.getBillingCycle())
				.status(sub.getStatus())
				.price(sub.getPrice())
				.currency(sub.getCurrency())
				.startDate(sub.getStartDate())
				.endDate(sub.getEndDate())
				.seatsCount(sub.getSeatsCount())
				.autoRenewal(sub.getAutoRenewal())
				.paymentReference(sub.getPaymentReference())
				.createdAt(sub.getCreatedAt())
				.isActive(sub.isActive())
				.build();
	}

	private boolean isAllowedToSendInvitation(OrganizationMemberModel.MemberRole role) {
		return role == OrganizationMemberModel.MemberRole.OWNER ||
				role == OrganizationMemberModel.MemberRole.ADMIN ||
				role == OrganizationMemberModel.MemberRole.MANAGER;
	}

	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "png";
		}
		return filename.substring(filename.lastIndexOf(".") + 1);
	}

	protected String getMessage(String key, Object... args) {
		return messageSource.getMessage(key, args, java.util.Locale.forLanguageTag("fa"));
	}
}
