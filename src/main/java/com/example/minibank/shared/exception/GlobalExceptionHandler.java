package com.example.minibank.shared.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED,
                "Email ou senha incorretos");
    }

    // 404
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado");
    }

    // 401
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(
            ExpiredJwtException ex) {
        return build(HttpStatus.UNAUTHORIZED, "JWT expirado");
    }

    // Mantém o status original do ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex) {

        return build(
                HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason()
        );
    }

    // 409
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor");
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String message) {

        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(error);
    }
}