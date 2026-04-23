package com.atm.iso8583.controller;

import com.atm.iso8583.config.Iso8583Config;
import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.service.Iso8583GatewayService;
import com.atm.iso8583.service.Iso8583ResponseStatusResolver;
import com.atm.iso8583.service.TransactionService;

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
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping({ "/api/iso8583", "/iso8583" })
@RequiredArgsConstructor
@Tag(name = "ISO 8583 Gateway", description = "REST API for ISO 8583 message processing")
public class Iso8583Controller {

        private final Iso8583GatewayService gatewayService;
        private final Iso8583Config iso8583Config;
        private final Iso8583ResponseStatusResolver statusResolver;
        private final TransactionService transactionService;

        @PostMapping(value = "/send", produces = { MediaType.APPLICATION_JSON_VALUE,
                        MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
                                        MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Send ISO 8583 message", description = "Converts JSON request to ISO 8583 format, sends to payment switch, and returns response")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Message processed successfully", content = @Content(schema = @Schema(implementation = Iso8583Response.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or validation error"),
                        @ApiResponse(responseCode = "500", description = "Internal server error or switch communication failure"),
                        @ApiResponse(responseCode = "503", description = "Service unavailable - switch not reachable")
        })
        public ResponseEntity<Iso8583Response> sendMessage(
                        @Valid @RequestBody Iso8583Request request,
                        @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
                return processAndRespond(request, requestId);
        }

        @PostMapping(value = "/authorize", produces = { MediaType.APPLICATION_JSON_VALUE,
                        MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
                                        MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Authorization request", description = "Shortcut endpoint equivalent to /send with MTI 0100")
        public ResponseEntity<Iso8583Response> authorize(@Valid @RequestBody Iso8583Request request) {
                request.setMti("0100");
                return processAndRespond(request, null);
        }

        @PostMapping(value = "/financial", produces = { MediaType.APPLICATION_JSON_VALUE,
                        MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
                                        MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Financial request", description = "Shortcut endpoint equivalent to /send with MTI 0200")
        public ResponseEntity<Iso8583Response> financial(@Valid @RequestBody Iso8583Request request) {
                request.setMti("0200");
                return processAndRespond(request, null);
        }

        @PostMapping(value = "/presentment", produces = { MediaType.APPLICATION_JSON_VALUE,
                        MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
                                        MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Presentment request", description = "Shortcut endpoint equivalent to /send with MTI 1200")
        public ResponseEntity<Iso8583Response> presentment(@Valid @RequestBody Iso8583Request request) {
                request.setMti("1200");
                return processAndRespond(request, null);
        }

        @PostMapping(value = "/reversal", produces = { MediaType.APPLICATION_JSON_VALUE,
                        MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
                                        MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Reversal request", description = "Shortcut endpoint equivalent to /send with MTI 0400")
        public ResponseEntity<Iso8583Response> reversal(@Valid @RequestBody Iso8583Request request) {
                request.setMti("0400");
                return processAndRespond(request, null);
        }

        @PostMapping(value = "/echo", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Network echo", description = "Sends an 0800/301 echo test to check switch connectivity")
        public ResponseEntity<Iso8583Response> echo() {
                Iso8583Response response = gatewayService.sendEchoTest(iso8583Config.getInstitutionId());
                return ResponseEntity.status(Objects.requireNonNull(statusResolver.resolve(response))).body(response);
        }

        @GetMapping(value = "/config", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Gateway config", description = "Returns current ISO 8583 network configuration")
        public ResponseEntity<Map<String, Object>> config() {
                Map<String, Object> cfg = new HashMap<>();
                cfg.put("host", iso8583Config.getHost());
                cfg.put("port", iso8583Config.getPort());
                cfg.put("connectTimeout", iso8583Config.getConnectTimeout());
                cfg.put("readTimeout", iso8583Config.getReadTimeout());
                cfg.put("headerLength", iso8583Config.getHeaderLength());
                cfg.put("institutionId", iso8583Config.getInstitutionId());
                return ResponseEntity.ok(cfg);
        }

        private ResponseEntity<Iso8583Response> processAndRespond(Iso8583Request request, String requestId) {
                log.info("Received ISO8583 request - MTI: {}, RequestID: {}, TransactionRef: {}",
                                request.getMti(), requestId, request.getTransactionRef());

                Iso8583Response response = gatewayService.processTransaction(request);
                if (response == null) {
                        log.error("Gateway service returned null response");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }

                HttpStatus status = statusResolver.resolve(response);

                log.info("Completed ISO8583 request - MTI: {}, ResponseCode: {}, Status: {}, Duration: {}ms",
                                response.getMti(), response.getResponseCode(), status, response.getProcessingTimeMs());

                transactionService.saveTransaction(request, response);

                return ResponseEntity.status(Objects.requireNonNull(status)).body(response);
        }

        @GetMapping(value = "/health", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Health check", description = "Check if the gateway service is running and healthy")
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

        @GetMapping(value = "/status", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
        @Operation(summary = "Gateway status", description = "Get detailed gateway status and statistics")
        public ResponseEntity<Map<String, Object>> status() {
                Map<String, Object> status = new HashMap<>();
                status.put("status", "ACTIVE");
                status.put("version", "1.0.0");
                status.put("uptime", System.currentTimeMillis());

                log.debug("Status check requested");
                return ResponseEntity.ok(status);
        }
}
