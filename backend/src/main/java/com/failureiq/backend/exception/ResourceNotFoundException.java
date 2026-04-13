package com.failureiq.backend.exception;

// This custom exception makes missing records easier to handle cleanly.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
