package com.bractus.userservice.exception;

/**
 * Thrown when a login attempt fails due to wrong username or password.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
