package com.tellinbox.tellinbox_api.feedback.mapper;

import com.tellinbox.tellinbox_api.feedback.model.FeedbackModel;
import com.tellinbox.tellinbox_api.feedback.model.FeedbackReportModel;
import com.tellinbox.tellinbox_api.user.model.UserModel;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between FeedbackReportModel and DTOs.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Component
public class FeedbackReportMapper {

    /**
     * Converts feedback, reporter and reason to a FeedbackReportModel entity.
     * 
     * @param feedback the feedback being reported
     * @param reporter the user reporting the feedback
     * @param reason the reason for reporting
     * @return the created FeedbackReportModel entity
     */
    public FeedbackReportModel toEntity(FeedbackModel feedback, UserModel reporter, String reason) {
        return FeedbackReportModel.builder()
                .feedback(feedback)
                .reporter(reporter)
                .reason(reason)
                .status("PENDING")
                .build();
    }

    /**
     * Converts a FeedbackReportModel entity to a DTO.
     * For now, returns the entity itself as there's no dedicated Report DTO.
     * Can be extended when a ReportDto is created.
     * 
     * @param report the report entity
     * @return the report entity (or DTO when created), or null if report is null
     */
    public FeedbackReportModel toDto(FeedbackReportModel report) {
        // Currently returning entity as-is since there's no dedicated Report DTO
        // This can be updated when a FeedbackReportDto is created
        return report;
    }
}
