import { HttpStatus } from '@nestjs/common';

export class CustomException extends Error {
  constructor(
    public readonly message: string,
    public readonly statusCode: HttpStatus,
    public readonly code?: string,
  ) {
    super(message);
    this.name = this.constructor.name;
    Error.captureStackTrace(this, this.constructor);
  }
}

export class ResourceNotFoundException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.NOT_FOUND, 'RESOURCE_NOT_FOUND');
  }
}

export class ResourceUnauthorizedException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.UNAUTHORIZED, 'RESOURCE_UNAUTHORIZED');
  }
}

export class AccessDeniedException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.FORBIDDEN, 'ACCESS_DENIED');
  }
}

export class ResourceForbiddenException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.FORBIDDEN, 'RESOURCE_FORBIDDEN');
  }
}

export class DuplicateEntityException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.CONFLICT, 'DUPLICATE_ENTITY');
  }
}

export class ApplicationServerException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.INTERNAL_SERVER_ERROR, 'APPLICATION_SERVER_ERROR');
  }
}

export class ValidationException extends CustomException {
  constructor(message: string, public readonly errors?: string[]) {
    const errorMessage = errors && errors.length > 0 
      ? `${message}: ${errors.join(', ')}` 
      : message;
    super(errorMessage, HttpStatus.NOT_ACCEPTABLE, 'VALIDATION_ERROR');
  }
}

export class BadRequestException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.BAD_REQUEST, 'BAD_REQUEST');
  }
}

export class NotFoundException extends CustomException {
  constructor(message: string) {
    super(message, HttpStatus.NOT_FOUND, 'NOT_FOUND');
  }
}
