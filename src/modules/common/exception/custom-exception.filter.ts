import { ArgumentsHost, Catch, ExceptionFilter, HttpStatus } from '@nestjs/common';
import { Response } from 'express';
import { CustomException } from './custom-exceptions';

@Catch(CustomException)
export class CustomExceptionFilter implements ExceptionFilter {
  catch(exception: CustomException, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest();

    const errorResponse = {
      statusCode: exception.statusCode,
      timestamp: new Date().toISOString(),
      path: request.url,
      message: exception.message,
      code: exception.code,
      ...(exception instanceof (exception.constructor as any) && 
          'errors' in exception && 
          Array.isArray((exception as any).errors) 
          ? { errors: (exception as any).errors } 
          : {}),
    };

    response.status(exception.statusCode).json(errorResponse);
  }
}
