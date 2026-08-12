package com.tellinbox.tellinbox_api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for sending OTP.
 */
@Data
@Builder
public class OtpSendRequest {

    @NotBlank(message = "شماره موبایل یا ایمیل الزامی است")
    private String identifier;

    @Pattern(regexp = "^(MOBILE|EMAIL)$", message = "نوع شناسه باید موبایل یا ایمیل باشد")
    @Builder.Default
    private String identifierType = "MOBILE";

    @Pattern(regexp = "^(LOGIN|REGISTRATION|PASSWORD_RESET)$", message = "نوع عملیات نامعتبر است")
    @Builder.Default
    private String otpType = "LOGIN";
}
