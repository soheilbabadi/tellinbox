package com.tellinbox.tellinbox_api.organization.repository;

import com.tellinbox.tellinbox_api.organization.entity.OrganizationModel;
import com.tellinbox.tellinbox_api.organization.enums.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Organization entity operations.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationModel, UUID> {

    /**
     * Find organization by name
     */
    Optional<OrganizationModel> findByName(String name);

    /**
     * Find organization by registration number
     */
    Optional<OrganizationModel> findByRegistrationNumber(String registrationNumber);

    /**
     * Find organizations owned by a user
     */
    List<OrganizationModel> findByOwnerId(UUID ownerId);

    /**
     * Find organizations by type
     */
    List<OrganizationModel> findByType(OrganizationType type);

    /**
     * Search organizations by name (case-insensitive)
     */
    @Query("SELECT o FROM OrganizationModel o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND o.isDeleted = false")
    Page<OrganizationModel> searchByName(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Count active organizations
     */
    @Query("SELECT COUNT(o) FROM OrganizationModel o WHERE o.isDeleted = false")
    long countActiveOrganizations();

    /**
     * Find organizations by owner ID and not deleted
     */
    @Query("SELECT o FROM OrganizationModel o WHERE o.owner.id = :ownerId AND o.isDeleted = false")
    List<OrganizationModel> findActiveByOwnerId(@Param("ownerId") UUID ownerId);

    /**
     * Check if organization name exists
     */
    boolean existsByName(String name);

    /**
     * Check if registration number exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);
}
