package com.mulberry.WebChat.controller;

import com.mulberry.WebChat.common.R;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionController {
    @ExceptionHandler(JwtException.class)
    public R<Void> handleJwtException(JwtException e) {
        log.warn("JWT identity verification failed: {}", e.getMessage());
        return R.error(401, "Invalid or expired identity");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = Optional.of(e.getBindingResult())
                .flatMap(result -> Optional.of(result.getFieldError()))
                .map(FieldError::getDefaultMessage)
                .orElse("Request param format error");
        log.warn("Invalid request param: {}", message);
        return R.error(400, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return R.error(400, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Insufficient permission: {}", e.getMessage());
        return R.error(403, "You don't have the permission");
    }


    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("System internal error: ", e);
        return R.error(500, "Internal error");
    }
}
