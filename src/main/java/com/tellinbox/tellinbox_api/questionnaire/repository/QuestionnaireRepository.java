package com.tellinbox.tellinbox_api.questionnaire.repository;

import com.tellinbox.tellinbox_api.organization.entity.OrganizationModel;
import com.tellinbox.tellinbox_api.questionnaire.entity.QuestionnaireModel;
import com.tellinbox.tellinbox_api.questionnaire.enums.QuestionnaireStatus;
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
 * Repository interface for QuestionnaireModel.
 */
@Repository
public interface QuestionnaireRepository extends JpaRepository<QuestionnaireModel, UUID> {

    /**
     * Find questionnaires by organization
     */
    Page<QuestionnaireModel> findByOrganizationId(UUID organizationId, Pageable pageable);

    /**
     * Find questionnaires by organization and status
     */
    Page<QuestionnaireModel> findByOrganizationIdAndStatus(UUID organizationId, QuestionnaireStatus status, Pageable pageable);

    /**
     * Find questionnaires by owner
     */
    List<QuestionnaireModel> findByOwnerId(UUID ownerId);

    /**
     * Find active questionnaires by organization
     */
    @Query("SELECT q FROM QuestionnaireModel q WHERE q.organization.id = :organizationId AND q.isActive = true AND q.status = :status")
    Page<QuestionnaireModel> findActiveByOrganizationId(@Param("organizationId") UUID organizationId, 
                                                         @Param("status") QuestionnaireStatus status, 
                                                         Pageable pageable);

    /**
     * Find questionnaire by ID and organization
     */
    Optional<QuestionnaireModel> findByIdAndOrganizationId(UUID id, UUID organizationId);

    /**
     * Find questionnaire by ID and owner
     */
    Optional<QuestionnaireModel> findByIdAndOwnerId(UUID id, UUID ownerId);

    /**
     * Count questionnaires by organization
     */
    long countByOrganizationId(UUID organizationId);

    /**
     * Count active questionnaires by organization
     */
    long countByOrganizationIdAndIsActiveTrue(UUID organizationId);

    /**
     * Find published questionnaires by organization
     */
    @Query("SELECT q FROM QuestionnaireModel q WHERE q.organization.id = :organizationId AND q.status = 'PUBLISHED' AND q.isActive = true ORDER BY q.createdAt DESC")
    List<QuestionnaireModel> findPublishedByOrganizationId(@Param("organizationId") UUID organizationId);

    /**
     * Search questionnaires by title
     */
    @Query("SELECT q FROM QuestionnaireModel q WHERE q.organization.id = :organizationId AND LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY q.createdAt DESC")
    Page<QuestionnaireModel> searchByTitle(@Param("organizationId") UUID organizationId, 
                                           @Param("keyword") String keyword, 
                                           Pageable pageable);

    /**
     * Find questionnaires that are accepting responses
     */
    @Query("SELECT q FROM QuestionnaireModel q WHERE q.isActive = true AND q.status = 'PUBLISHED' AND (q.startDate IS NULL OR q.startDate <= CURRENT_TIMESTAMP) AND (q.endDate IS NULL OR q.endDate > CURRENT_TIMESTAMP)")
    List<QuestionnaireModel> findAcceptingResponses();

    /**
     * Find questionnaires that are accepting responses by organization
     */
    @Query("SELECT q FROM QuestionnaireModel q WHERE q.organization.id = :organizationId AND q.isActive = true AND q.status = 'PUBLISHED' AND (q.startDate IS NULL OR q.startDate <= CURRENT_TIMESTAMP) AND (q.endDate IS NULL OR q.endDate > CURRENT_TIMESTAMP)")
    List<QuestionnaireModel> findAcceptingResponsesByOrganizationId(@Param("organizationId") UUID organizationId);
}
