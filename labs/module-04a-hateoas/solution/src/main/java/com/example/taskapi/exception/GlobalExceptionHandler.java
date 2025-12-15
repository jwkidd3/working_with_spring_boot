package com.example.taskapi.exception;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Returns RFC 7807 Problem Details for consistent error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle TaskNotFoundException.
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Problem> handleTaskNotFoundException(TaskNotFoundException ex) {
        Problem problem = Problem.create()
                .withTitle("Task Not Found")
                .withStatus(HttpStatus.NOT_FOUND)
                .withDetail(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)
                .body(problem);
    }

    /**
     * Handle IllegalStateException (invalid state transitions).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Problem> handleIllegalStateException(IllegalStateException ex) {
        Problem problem = Problem.create()
                .withTitle("Invalid State Transition")
                .withStatus(HttpStatus.CONFLICT)
                .withDetail(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)
                .body(problem);
    }

    /**
     * Handle validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Problem problem = Problem.create()
                .withTitle("Validation Failed")
                .withStatus(HttpStatus.BAD_REQUEST)
                .withDetail("Invalid request parameters")
                .withProperties(map -> map.put("errors", errors));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)
                .body(problem);
    }

    /**
     * Handle generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleGenericException(Exception ex) {
        Problem problem = Problem.create()
                .withTitle("Internal Server Error")
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .withDetail("An unexpected error occurred: " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaTypes.HTTP_PROBLEM_DETAILS_JSON)
                .body(problem);
    }
}
