package com.tellinbox.tellinbox_api.organization.dto;

import com.tellinbox.tellinbox_api.organization.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new organization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "نام سازمان الزامی است")
    @Size(max = 200, message = "نام سازمان نباید بیشتر از ۲۰۰ کاراکتر باشد")
    private String name;

    @Size(max = 50, message = "شماره ثبت نباید بیشتر از ۵۰ کاراکتر باشد")
    private String registrationNumber;

    private OrganizationType type;

    @Size(max = 2000, message = "توضیحات نباید بیشتر از ۲۰۰۰ کاراکتر باشد")
    private String description;

    @Size(max = 255, message = "وب‌سایت نباید بیشتر از ۲۵۵ کاراکتر باشد")
    private String website;

    @Size(max = 20, message = "تلفن نباید بیشتر از ۲۰ کاراکتر باشد")
    private String phone;

    @Size(max = 500, message = "آدرس نباید بیشتر از ۵۰۰ کاراکتر باشد")
    private String address;
}
