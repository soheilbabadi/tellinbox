package com.tellinbox.tellinbox_api.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for uploading organization logo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationLogoRequest {

    /**
     * Logo file content (base64 encoded) or MultipartFile will be handled in controller
     */
    private byte[] logoData;

    /**
     * Content type of the image
     */
    private String contentType;

    /**
     * File name
     */
    private String fileName;
}
