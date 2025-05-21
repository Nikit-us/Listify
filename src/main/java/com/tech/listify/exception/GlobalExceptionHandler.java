package com.tech.listify.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String PATH = "path";

    /**
     * Обработка ошибки: Ресурс не найден (404 Not Found).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    /**
     * Обработка ошибки: Конфликт (например, пользователь уже существует) (409 Conflict).
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest request) {
        log.warn("Conflict during operation: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    /**
     * Обработка ошибки: Ошибка хранения/обработки файла (500 Internal Server Error).
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Object> handleFileStorageException(FileStorageException ex, WebRequest request) {
        log.error("File storage error: {}", ex.getMessage(), ex.getCause());
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "File Storage Error", ex.getMessage(), request);
    }

    /**
     * Обработка ошибки: Неудачная аутентификация (например, неверный пароль на /login) (401 Unauthorized).
     * Примечание: Ошибки 401 при доступе к защищенным ресурсам БЕЗ токена или с НЕВАЛИДНЫМ токеном
     * обычно обрабатываются через AuthenticationEntryPoint. Этот обработчик для исключений,
     * брошенных, например, при вызове authenticationManager.authenticate().
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Authentication attempt failed: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Ошибка аутентификации: неверные учетные данные", request);
    }

    /**
     * Обработка ошибки: Доступ запрещен (недостаточно прав) (403 Forbidden).
     * Примечание: Ошибки 403, генерируемые механизмом авторизации Spring Security (например, @PreAuthorize),
     * обычно обрабатываются через AccessDeniedHandler. Этот обработчик может ловить AccessDeniedException,
     * если она была выброшена вручную из кода приложения, ИЛИ если AccessDeniedHandler не настроен.
     * Рекомендуется использовать AccessDeniedHandler.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleSpringAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access Denied: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", "Доступ запрещен: недостаточно прав.", request);
    }

    /**
     * Обработка ошибки: Ошибка валидации аргументов метода (@Valid) (400 Bad Request).
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        String path = getPath(request);
        log.warn("Validation failed on path [{}]: {}", path, errors);
        Map<String, Object> body = createErrorBodyMap(HttpStatus.BAD_REQUEST, "Validation Error", errors, path);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка ошибки: Неподдерживаемый HTTP метод (405 Method Not Allowed).
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = "Метод " + ex.getMethod() + " не поддерживается для данного ресурса.";
        log.warn("Method Not Allowed on path [{}]: {}", getPath(request), message);
        return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", message, request, headers);
    }

    /**
     * Обработка ошибки: Неподдерживаемый тип медиа (415 Unsupported Media Type).
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = "Тип контента '" + ex.getContentType() + "' не поддерживается. Поддерживаемые типы: " + ex.getSupportedMediaTypes();
        log.warn("Unsupported Media Type on path [{}]: {}", getPath(request), message);
        return createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type", message, request, headers);
    }

    /**
     * Обработка ошибки: Не найден обработчик для запрошенного пути (404 Not Found).
     * Это может случиться для запросов к статике или не существующим API путям.
     */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request){
        String message = "Ресурс не найден по пути: " + ex.getResourcePath();
        log.warn("No resource found: {}", message);
        return createErrorResponse(HttpStatus.NOT_FOUND, "Not Found", message, request, headers);
    }


    /**
     * Обработка всех остальных непредвиденных исключений (500 Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        String path = getPath(request);
        log.error("An unexpected error occurred on path [{}]: {}", path, ex.getMessage(), ex); // Логируем со стектрейсом!
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Произошла непредвиденная ошибка сервера.", request);
    }

    /**
     * Создает стандартизированный ResponseEntity для ошибки.
     */
    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String errorType, Object message, WebRequest request) {
        return createErrorResponse(status, errorType, message, request, new HttpHeaders());
    }

    /**
     * Создает стандартизированный ResponseEntity для ошибки с указанными заголовками.
     */
    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String errorType, Object message, WebRequest request, HttpHeaders headers) {
        Map<String, Object> body = createErrorBodyMap(status, errorType, message, getPath(request));
        return new ResponseEntity<>(body, headers, status);
    }


    /**
     * Создает стандартизированное тело ответа об ошибке в виде Map.
     */
    private Map<String, Object> createErrorBodyMap(HttpStatus status, String errorType, Object message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, Instant.now().toString());
        body.put(STATUS, status.value());
        body.put(ERROR, errorType);
        body.put(MESSAGE, message);
        body.put(PATH, path);
        return body;
    }

    /**
     * Извлекает путь запроса из WebRequest.
     */
    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        // Возвращаем базовую информацию, если не ServletWebRequest
        return request.getDescription(false).replace("uri=", "");
    }
}