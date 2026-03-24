package com.atm.iso8583.controller;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.service.Iso8583GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/iso8583")
@RequiredArgsConstructor
@Tag(name = "ISO 8583 Gateway", description = "REST API for ISO 8583 message processing")
public class Iso8583Controller {

    private final Iso8583GatewayService gatewayService;

    @PostMapping(value = "/send",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Send ISO 8583 message",
            description = "Converts JSON request to ISO 8583 format, sends to payment switch, and returns response"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Message processed successfully",
                    content = @Content(schema = @Schema(implementation = Iso8583Response.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation error"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error or switch communication failure"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Service unavailable - switch not reachable"
            )
    })
    public ResponseEntity<Iso8583Response> sendMessage(
            @Valid @RequestBody Iso8583Request request,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
        
        log.info("Received ISO8583 request - MTI: {}, RequestID: {}, TransactionRef: {}",
                request.getMti(), requestId, request.getTransactionRef());
        
        Iso8583Response response = gatewayService.processTransaction(request);
        if (response == null) {
            log.error("Gateway service returned null response");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        HttpStatus status = determineHttpStatus(response.getResponseCode());
        
        log.info("Completed ISO8583 request - MTI: {}, ResponseCode: {}, Status: {}, Duration: {}ms",
                response.getMti(), response.getResponseCode(), status, response.getProcessingTimeMs());
        
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping(value = "/health",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Health check",
            description = "Check if the gateway service is running and healthy"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gateway is healthy")
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ISO 8583 Gateway");
        health.put("timestamp", System.currentTimeMillis());
        
        log.debug("Health check requested");
        return ResponseEntity.ok(health);
    }

    @GetMapping(value = "/status",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(
            summary = "Gateway status",
            description = "Get detailed gateway status and statistics"
    )
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ACTIVE");
        status.put("version", "1.0.0");
        status.put("uptime", System.currentTimeMillis());
        
        log.debug("Status check requested");
        return ResponseEntity.ok(status);
    }

    private HttpStatus determineHttpStatus(String responseCode) {
        if (responseCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        return switch (responseCode) {
            case "00" -> HttpStatus.OK;
            case "05", "14", "41", "43" -> HttpStatus.FORBIDDEN;
            case "51", "61" -> HttpStatus.PAYMENT_REQUIRED;
            case "91", "96" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
