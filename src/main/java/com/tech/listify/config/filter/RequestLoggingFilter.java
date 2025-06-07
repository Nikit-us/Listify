package com.tech.listify.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        boolean isMultipart = request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
        if (isMultipart) {
            logRequestResponse(request, response, filterChain, null, null);
        } else {
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            logRequestResponse(requestWrapper, responseWrapper, filterChain, requestWrapper, responseWrapper);

            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestResponse(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain,
                                    ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper)
            throws IOException, ServletException {

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        log.info("REQ [{}] --> {} {}", requestId, request.getMethod(), request.getRequestURI());

        if (requestWrapper != null) {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } else {
            filterChain.doFilter(request, response);
        }


        long duration = System.currentTimeMillis() - startTime;

        log.info("REQ [{}] <-- {} {} - {}ms", requestId, response.getStatus(), request.getMethod(), duration);

        if (requestWrapper != null && responseWrapper != null) {
            String requestBody = getContentAsString(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getContentAsString(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());

            if (log.isDebugEnabled()) {
                log.debug("REQ [{}] Request Body: {}", requestId, requestBody.length() > 500 ? requestBody.substring(0, 500) + "..." : requestBody);
                log.debug("REQ [{}] Response Body: {}", requestId, responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
            }
        }
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