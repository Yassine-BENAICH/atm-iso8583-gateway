package com.atm.iso8583.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        response.setHeader(REQUEST_ID_HEADER, requestId);

        log.info("[{}] Incoming request: {} {} from {}",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long duration = System.currentTimeMillis() - (startTime != null ? startTime : 0);

        if (ex != null) {
            log.error("[{}] Request failed: {} {} - Status: {} - Duration: {}ms - Error: {}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration,
                    ex.getMessage());
        } else {
            log.info("[{}] Request completed: {} {} - Status: {} - Duration: {}ms",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
        }
    }
}
