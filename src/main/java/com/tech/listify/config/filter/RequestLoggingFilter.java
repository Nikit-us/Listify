package com.tech.listify.config.filter;

import com.tech.listify.service.impl.HitCounterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final HitCounterService hitCounterService;
    private static final int MAX_PAYLOAD_LENGTH = 1000;
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList("password", "token", "jwt");
    private static final Pattern SENSITIVE_FIELD_PATTERN = buildSensitiveFieldPattern();

    private static final List<String> NON_LOGGABLE_CONTENT_TYPES = List.of(
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.TEXT_PLAIN_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        hitCounterService.incrementHit(request.getRequestURI());

        boolean isMultipart = request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
        if (isMultipart) {
            logRequestResponse(request, response, filterChain, null, null);
        } else {
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
            try {
                logRequestResponse(requestWrapper, responseWrapper, filterChain, requestWrapper, responseWrapper);
            } finally {
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    private void logRequestResponse(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain,
                                    ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper)
            throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        log.info("REQ [{}] --> {} {}", requestId, request.getMethod(), request.getRequestURI());

        filterChain.doFilter(requestWrapper != null ? requestWrapper : request, responseWrapper != null ? responseWrapper : response);

        long duration = System.currentTimeMillis() - startTime;
        log.info("REQ [{}] <-- {} {} - {}ms", requestId, response.getStatus(), request.getRequestURI(), duration);

        if (log.isDebugEnabled() && requestWrapper != null && responseWrapper != null) {
            logRequestBody(requestId, requestWrapper);
            logResponseBody(requestId, responseWrapper);
        }
    }

    private void logRequestBody(String requestId, ContentCachingRequestWrapper request) {
        String requestBody = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
        log.debug("REQ [{}] Request Body: {}", requestId, sanitizeAndTruncate(requestBody));
    }

    private void logResponseBody(String requestId, ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        if (contentType != null && NON_LOGGABLE_CONTENT_TYPES.stream().anyMatch(contentType::startsWith)) {
            log.debug("REQ [{}] Response Body: [Omitted for content type: {}]", requestId, contentType);
        } else {
            String responseBody = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());
            log.debug("REQ [{}] Response Body: {}", requestId, sanitizeAndTruncate(responseBody));
        }
    }

    private String sanitizeAndTruncate(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String sanitized = sanitize(text);
        return sanitized.length() > MAX_PAYLOAD_LENGTH ? sanitized.substring(0, MAX_PAYLOAD_LENGTH) + "..." : sanitized;
    }

    private String sanitize(String text) {
        Matcher matcher = SENSITIVE_FIELD_PATTERN.matcher(text);
        return matcher.replaceAll("$1\"****\"");
    }

    private static Pattern buildSensitiveFieldPattern() {
        String fieldsRegex = String.join("|", SENSITIVE_FIELDS);
        return Pattern.compile("(\"(?:" + fieldsRegex + ")\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE);
    }

    private String getContentAsString(byte[] buf, String encoding) {
        if (buf == null || buf.length == 0) return "";
        try {
            return new String(buf, encoding != null ? encoding : "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "[unknown encoding]";
        }
    }
}