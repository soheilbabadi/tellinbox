package com.tellinbox.tellinbox_api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for user login.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
public class LoginRequest {

    @NotBlank(message = "نام کاربری یا شماره موبایل الزامی است")
    private String usernameOrMobile;

    @NotBlank(message = "رمز عبور الزامی است")
    @Size(min = 6, max = 100, message = "رمز عبور باید بین ۶ تا ۱۰۰ کاراکتر باشد")
    private String password;
}
