package com.tellinbox.tellinbox_api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for updating user profile.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Data
@Builder
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "نام کامل باید بین ۲ تا ۱۰۰ کاراکتر باشد")
    private String fullName;

    @Size(min = 3, max = 50, message = "نام کاربری باید بین ۳ تا ۵۰ کاراکتر باشد")
    private String username;

    @Email(message = "ایمیل نامعتبر است")
    @Size(max = 100, message = "ایمیل نباید بیشتر از ۱۰۰ کاراکتر باشد")
    private String email;

    @Size(max = 4000, message = "بیو نباید بیشتر از ۴۰۰۰ کاراکتر باشد")
    private String bio;

    @Size(max = 500, message = "URL عکس پروفایل نباید بیشتر از ۵۰۰ کاراکتر باشد")
    private String profilePictureUrl;

    private String gender;

    private String birthDate;

    @Size(min = 2, max = 5, message = "کد زبان باید ۲ تا ۵ کاراکتر باشد")
    private String preferredLanguage;

    @Size(max = 50, message = "منطقه زمانی نباید بیشتر از ۵۰ کاراکتر باشد")
    private String timezone;
}
