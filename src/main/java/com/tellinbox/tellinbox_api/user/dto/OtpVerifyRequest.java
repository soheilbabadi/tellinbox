package com.tellinbox.tellinbox_api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for verifying OTP.
 */
@Data
@Builder
public class OtpVerifyRequest {

    @NotBlank(message = "شماره موبایل یا ایمیل الزامی است")
    private String identifier;

    @NotBlank(message = "کد تایید الزامی است")
    @Pattern(regexp = "^\\d{6}$", message = "کد تایید باید ۶ رقم باشد")
    private String code;

    @Pattern(regexp = "^(LOGIN|REGISTRATION|PASSWORD_RESET)$", message = "نوع عملیات نامعتبر است")
    @Builder.Default
    private String otpType = "LOGIN";
}
