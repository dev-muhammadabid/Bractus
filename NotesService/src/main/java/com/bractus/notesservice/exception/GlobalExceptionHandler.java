package com.bractus.notesservice.exception;

import com.bractus.notesservice.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler catches exceptions thrown anywhere in the Notes service
 * and converts them into clean JSON error responses.
 *
 * This keeps error handling out of the controllers and in one central place.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles the case where a note ID doesn't exist in the database.
     * Returns HTTP 404 Not Found.
     */
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoteNotFound(NoteNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    /**
     * Handles the case where a user tries to modify a note they don't own.
     * Returns HTTP 403 Forbidden.
     */
    @ExceptionHandler(NoteOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleOwnership(NoteOwnershipException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, ex.getMessage()));
    }

    /**
     * Handles validation errors from @Valid annotations on request bodies.
     * Returns HTTP 400 Bad Request with the first validation message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // Extract the first field error message
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, message));
    }

    /**
     * Catch-all for any unexpected exceptions.
     * Returns HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "An unexpected error occurred."));
    }
}
