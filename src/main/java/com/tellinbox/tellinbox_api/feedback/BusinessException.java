package com.tellinbox.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }


    public class NotFoundException extends BusinessException {
        public NotFoundException(String entityName, Object id) {
            super(entityName + " not found with id: " + id, "NOT_FOUND");
        }
    }

    public class ValidationException extends BusinessException {
        public ValidationException(String message) {
            super(message, "VALIDATION_ERROR");
        }
    }

    public class UnauthorizedException extends BusinessException {
        public UnauthorizedException(String message) {
            super(message, "UNAUTHORIZED");
        }
    }
}
