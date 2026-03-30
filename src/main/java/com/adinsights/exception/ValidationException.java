package com.adinsights.exception;

public class ValidationException extends AdInsightsException {

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
}