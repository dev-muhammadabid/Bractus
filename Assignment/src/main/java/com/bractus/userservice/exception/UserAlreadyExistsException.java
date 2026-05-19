package com.bractus.userservice.exception;

/**
 * Thrown when a signup attempt uses a username that already exists in the database.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
