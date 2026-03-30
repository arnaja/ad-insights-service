package com.adinsights.exception;

import com.adinsights.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return getErrorResponseResponseEntity(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(DependencyException.class)
    public ResponseEntity<ErrorResponse> handleDependency(DependencyException ex) {
        return getErrorResponseResponseEntity(HttpStatus.SERVICE_UNAVAILABLE, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(AdInsightsException.class)
    public ResponseEntity<ErrorResponse> handleDomain(AdInsightsException ex) {
        return getErrorResponseResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return getErrorResponseResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected failure");
    }

    @NonNull
    private static ResponseEntity<ErrorResponse> getErrorResponseResponseEntity(HttpStatus httpStatus, String errorCode, String message) {
        return ResponseEntity.status(httpStatus).body(
                ErrorResponse.builder()
                        .errorCode(errorCode)
                        .message(message)
                        .timestamp(Instant.now())
                        .build()
        );
    }
}