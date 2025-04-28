package com.tech.listify.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper; // Jackson для преобразования в JSON

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed: {}", authException.getMessage()); // Логируем реальную причину

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Статус 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString()); // Преобразуем в строку для простоты
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");

        // --- ВАЖНО: Не раскрываем причину ошибки ---
        // Вместо детальной ошибки (неправильный email/пароль) даем общее сообщение
        // Это стандартная практика безопасности, чтобы не помогать злоумышленникам
        body.put("message", "Ошибка аутентификации: неверные учетные данные"); // Общее сообщение
        // Если хочешь, можно вернуть authException.getMessage(), но это менее безопасно

        body.put("path", request.getRequestURI());

        // Записываем JSON в тело ответа
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}