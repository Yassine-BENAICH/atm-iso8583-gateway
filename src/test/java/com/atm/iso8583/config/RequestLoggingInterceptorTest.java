package com.atm.iso8583.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestLoggingInterceptorTest {

    private final RequestLoggingInterceptor interceptor = new RequestLoggingInterceptor();

    @Test
    void preHandleShouldReuseExistingRequestIdHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/iso8583/health");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("X-Request-ID", "REQ-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean shouldContinue = interceptor.preHandle(request, response, new Object());

        assertTrue(shouldContinue);
        assertEquals("REQ-123", request.getAttribute("requestId"));
        assertNotNull(request.getAttribute("startTime"));
        assertEquals("REQ-123", response.getHeader("X-Request-ID"));
    }

    @Test
    void preHandleShouldGenerateRequestIdWhenHeaderIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/iso8583/send");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        String generatedRequestId = (String) request.getAttribute("requestId");
        assertNotNull(generatedRequestId);
        assertDoesNotThrow(() -> UUID.fromString(generatedRequestId));
        assertEquals(generatedRequestId, response.getHeader("X-Request-ID"));
        assertNotNull(request.getAttribute("startTime"));
    }

    @Test
    void afterCompletionShouldNotThrowWhenStartTimeIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/iso8583/status");
        request.setRemoteAddr("127.0.0.1");
        request.setAttribute("requestId", "REQ-456");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        assertDoesNotThrow(() -> interceptor.afterCompletion(request, response, new Object(), null));
    }

    @Test
    void afterCompletionShouldNotThrowWhenExceptionIsProvided() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/iso8583/send");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);
        interceptor.preHandle(request, response, new Object());

        RuntimeException failure = new RuntimeException("boom");
        assertDoesNotThrow(() -> interceptor.afterCompletion(request, response, new Object(), failure));
    }
}
