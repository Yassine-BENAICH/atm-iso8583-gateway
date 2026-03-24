package com.atm.iso8583.controller;

import com.atm.iso8583.model.TrafficEvent;
import com.atm.iso8583.model.TrafficMetrics;
import com.atm.iso8583.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/monitoring")
@Tag(name = "Monitoring", description = "Traffic metrics and recent transaction events")
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping(value = "/metrics",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Get aggregated gateway metrics")
    public ResponseEntity<TrafficMetrics> metrics() {
        return ResponseEntity.ok(monitoringService.getCurrentMetrics());
    }

    @GetMapping(value = "/events",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Operation(summary = "Get recent transaction events")
    public ResponseEntity<List<TrafficEvent>> recentEvents(
            @Parameter(description = "Maximum number of events to return (1-500)")
            @RequestParam(defaultValue = "50")
            @Min(value = 1, message = "limit must be greater than or equal to 1")
            @Max(value = 500, message = "limit must be less than or equal to 500")
            int limit) {
        return ResponseEntity.ok(monitoringService.getRecentEvents(limit));
    }
}
