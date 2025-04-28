package com.tech.listify.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler { // Наследуем для удобной обработки Spring MVC исключений

    // Обработчик для UserAlreadyExistsException (409 Conflict)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        log.warn("Conflict during operation: {}", ex.getMessage());
        Map<String, Object> body = createErrorBody(HttpStatus.CONFLICT, "Ошибка регистрации", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // Обработчик для ResourceNotFoundException (404 Not Found) - добавим его позже, когда он понадобится
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        Map<String, Object> body = createErrorBody(HttpStatus.NOT_FOUND, "Ресурс не найден", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Переопределяем метод для обработки ошибок валидации (@Valid) - 400 Bad Request
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("Validation failed: {}", errors);
        Map<String, Object> body = createErrorBody(HttpStatus.BAD_REQUEST, "Ошибка валидации", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Обработчик для AuthenticationException (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Authentication attempt failed: {}", ex.getMessage());

        Map<String, Object> body = createErrorBody(HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Ошибка аутентификации: неверные учетные данные");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // Общий обработчик для других RuntimeException (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        Map<String, Object> body = createErrorBody(HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorBody(HttpStatus status, String error, Object message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        //request.getDescription(false)
        return body;
    }
}