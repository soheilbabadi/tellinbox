package com.tellinbox.tellinbox_api.user.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegistrationRequest {

    @NotBlank(message = "شماره موبایل الزامی است")
    @Pattern(regexp = "^09[0-9]{9}$", message = "شماره موبایل نامعتبر است")
    private String mobile;

    @Email(message = "ایمیل نامعتبر است")
    @Size(max = 100, message = "ایمیل نباید بیشتر از ۱۰۰ کاراکتر باشد")
    private String email;

    @NotBlank(message = "نام کامل الزامی است")
    @Size(min = 2, max = 100, message = "نام کامل باید بین ۲ تا ۱۰۰ کاراکتر باشد")
    private String fullName;

    @NotBlank(message = "نام کاربری الزامی است")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,50}$", message = "نام کاربری باید ۳ تا ۵۰ کاراکتر و شامل حروف، اعداد و زیرخط باشد")
    private String username;

    @Size(max = 500, message = "بیو نباید بیشتر از ۵۰۰ کاراکتر باشد")
    private String bio;

    @Size(max = 500, message = "URL عکس پروفایل نباید بیشتر از ۵۰۰ کاراکتر باشد")
    private String profilePictureUrl;

    @Pattern(regexp = "^[a-zA-Z]*$", message = "زبان باید به انگلیسی باشد")
    private String preferredLanguage = "fa";
}