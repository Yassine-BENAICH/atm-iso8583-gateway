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
@Schema(description = "Aggregated traffic metrics for ISO 8583 gateway activity")
public class TrafficMetrics {

    @Schema(description = "Monitoring service start timestamp (UTC)")
    private Instant startedAt;

    @Schema(description = "Metrics snapshot timestamp (UTC)")
    private Instant snapshotAt;

    @Schema(description = "Gateway uptime in seconds", example = "3600")
    private long uptimeSeconds;

    @Schema(description = "Total processed transactions", example = "1250")
    private long totalTransactions;

    @Schema(description = "Total successful transactions", example = "1180")
    private long successfulTransactions;

    @Schema(description = "Total declined transactions", example = "60")
    private long declinedTransactions;

    @Schema(description = "Total failed/error transactions", example = "10")
    private long errorTransactions;

    @Schema(description = "Success rate percentage", example = "94.40")
    private double successRatePercent;

    @Schema(description = "Average latency in milliseconds", example = "128.54")
    private double averageLatencyMs;

    @Schema(description = "95th percentile latency in milliseconds", example = "410")
    private long p95LatencyMs;

    @Schema(description = "Minimum latency in milliseconds", example = "41")
    private long minLatencyMs;

    @Schema(description = "Maximum latency in milliseconds", example = "752")
    private long maxLatencyMs;

    @Schema(description = "Transactions captured in the last minute", example = "42")
    private long transactionsLastMinute;

    @Schema(description = "Current number of stored recent events", example = "200")
    private int recentEventsCount;
}
