package com.tellinbox.tellinbox_api.payment.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("در انتظار پرداخت"),
    SUCCESS("موفق"),
    FAILED("ناموفق"),
    CANCELLED("لغو شده"),
    REFUNDED("بازگشت داده شده"),
    PARTIALLY_REFUNDED("بازگشت جزئی");

    private final String persianName;

    PaymentStatus(String persianName) {
        this.persianName = persianName;
    }

    public boolean isCompleted() {
        return this == SUCCESS;
    }
}
