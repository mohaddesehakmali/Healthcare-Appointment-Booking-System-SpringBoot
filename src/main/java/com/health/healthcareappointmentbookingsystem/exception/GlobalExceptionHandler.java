package com.health.healthcareappointmentbookingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Map<String,Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now().toString(),
                "status", status.value(), "message", message));
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String,Object>> denied(AccessDeniedException e) {
        return error(HttpStatus.FORBIDDEN, "Access denied");
    }
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Map<String,Object>> credentials(BadCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,Object>> badArgument(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,Object>> state(IllegalStateException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>> invalid(MethodArgumentNotValidException e) {
        return error(HttpStatus.BAD_REQUEST, "Invalid fields: " +
                e.getBindingResult().getFieldErrors().stream().map(f -> f.getField()).distinct().toList());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String,Object>> unexpected(Exception e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }
}
