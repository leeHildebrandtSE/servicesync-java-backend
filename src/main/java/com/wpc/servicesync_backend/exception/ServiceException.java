package com.wpc.servicesync_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus status;

    public ServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ServiceException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    // Factory methods for common exceptions
    public static ServiceException notFound(String message) {
        return new ServiceException(message, HttpStatus.NOT_FOUND);
    }

    public static ServiceException badRequest(String message) {
        return new ServiceException(message, HttpStatus.BAD_REQUEST);
    }

    public static ServiceException unauthorized(String message) {
        return new ServiceException(message, HttpStatus.UNAUTHORIZED);
    }

    public static ServiceException forbidden(String message) {
        return new ServiceException(message, HttpStatus.FORBIDDEN);
    }

    public static ServiceException conflict(String message) {
        return new ServiceException(message, HttpStatus.CONFLICT);
    }

    public static ServiceException internalError(String message) {
        return new ServiceException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}