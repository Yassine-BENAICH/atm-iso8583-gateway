package com.atm.iso8583.exception;

import com.atm.iso8583.model.ApiError;
import com.atm.iso8583.model.ErrorResponse;
import com.atm.iso8583.service.MonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";
    private final MonitoringService monitoringService;

    public GlobalExceptionHandler(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        String detailMessage = String.join("; ", details);

        log.warn("Validation error on {}: {}", req.getRequestURI(), detailMessage);
        recordIsoApiFailure(req, HttpStatus.BAD_REQUEST, detailMessage);
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .path(req.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
    }

    /** Constraint violations (e.g. path / query params) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex,
            HttpServletRequest req) {
        String details = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("Constraint violation on {}: {}", req.getRequestURI(), details);
        recordIsoApiFailure(req, HttpStatus.BAD_REQUEST, details);
        return ResponseEntity.badRequest().body(ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message(details)
                .path(req.getRequestURI())
                .build());
    }

    /** Catch-all handler */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        recordIsoApiFailure(req, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.internalServerError().body(ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .build());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found on {}: {}", req.getRequestURI(), ex.getMessage());
        recordIsoApiFailure(req, HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .build());
    }

    private void recordIsoApiFailure(HttpServletRequest req, HttpStatus status, String message) {
        String path = req.getRequestURI();
        if (path == null || (!path.startsWith("/api/iso8583") && !path.startsWith("/api/powercard"))) {
            return;
        }

        long latencyMs = 0L;
        Object started = req.getAttribute(START_TIME_ATTRIBUTE);
        if (started instanceof Long startedAt) {
            latencyMs = Math.max(0L, System.currentTimeMillis() - startedAt);
        }

        monitoringService.recordApiFailure(path, status.value(), message, latencyMs);
    }
}
