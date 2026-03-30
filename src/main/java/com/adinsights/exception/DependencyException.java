package com.adinsights.exception;

public class DependencyException extends AdInsightsException {

    public DependencyException(String message, Throwable cause) {
        super(message, "DEPENDENCY_FAILURE");
        initCause(cause);
    }
}