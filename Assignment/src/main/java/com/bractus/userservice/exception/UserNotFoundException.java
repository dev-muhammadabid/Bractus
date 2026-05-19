package com.bractus.userservice.exception;

/**
 * Thrown when a user lookup by ID or username returns no result.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
