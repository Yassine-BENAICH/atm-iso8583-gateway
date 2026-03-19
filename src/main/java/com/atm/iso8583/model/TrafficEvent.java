package com.atm.iso8583.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Single ISO 8583 transaction event captured for monitoring")
public class TrafficEvent {

    @Schema(description = "Event timestamp (UTC)")
    private Instant timestamp;

    @Schema(description = "Request MTI", example = "0200")
    private String requestMti;

    @Schema(description = "Response MTI", example = "0210")
    private String responseMti;

    @Schema(description = "System Trace Audit Number (DE11)", example = "000123")
    private String stan;

    @Schema(description = "Response code (DE39)", example = "00")
    private String responseCode;

    @Schema(description = "Outcome classification: SUCCESS, DECLINED, ERROR", example = "SUCCESS")
    private String status;

    @Schema(description = "End-to-end processing latency in milliseconds", example = "142")
    private long latencyMs;

    @Schema(description = "Error details when status is ERROR")
    private String errorMessage;
}
