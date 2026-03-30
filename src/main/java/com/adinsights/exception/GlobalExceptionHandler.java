package com.adinsights.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest().body(buildResponse(ex));
    }

    @ExceptionHandler(DependencyException.class)
    public ResponseEntity<?> handleDependency(DependencyException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildResponse(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "INTERNAL_SERVER_ERROR",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now()
                ));
    }

    private Map<String, Object> buildResponse(AdInsightsException ex) {
        return Map.of(
                "errorCode", ex.getErrorCode(),
                "message", ex.getMessage(),
                "timestamp", Instant.now()
        );
    }
}