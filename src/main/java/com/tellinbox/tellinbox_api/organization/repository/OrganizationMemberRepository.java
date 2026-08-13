package com.tellinbox.tellinbox_api.organization.repository;

import com.tellinbox.tellinbox_api.organization.entity.OrganizationMemberModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for OrganizationMember entity operations.
 */
@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMemberModel, UUID> {

    /**
     * Find member by organization and user
     */
    Optional<OrganizationMemberModel> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    /**
     * Check if user is a member of an organization
     */
    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    /**
     * Find all members of an organization
     */
    List<OrganizationMemberModel> findByOrganizationId(UUID organizationId);

    /**
     * Find all organizations a user belongs to
     */
    List<OrganizationMemberModel> findByUserId(UUID userId);

    /**
     * Count active members in an organization
     */
    @Query("SELECT COUNT(m) FROM OrganizationMemberModel m WHERE m.organization.id = :organizationId AND m.isActive = true")
    long countActiveMembers(@Param("organizationId") UUID organizationId);

    /**
     * Find active members by organization
     */
    @Query("SELECT m FROM OrganizationMemberModel m WHERE m.organization.id = :organizationId AND m.isActive = true")
    List<OrganizationMemberModel> findActiveMembersByOrganizationId(@Param("organizationId") UUID organizationId);

    /**
     * Find members by role
     */
    List<OrganizationMemberModel> findByOrganizationIdAndRole(UUID organizationId, OrganizationMemberModel.MemberRole role);

    /**
     * Delete member by organization and user
     */
    void deleteByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}
