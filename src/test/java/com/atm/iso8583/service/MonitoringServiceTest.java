package com.atm.iso8583.service;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.model.TrafficEvent;
import com.atm.iso8583.model.TrafficMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MonitoringServiceTest {

    @Test
    void shouldAggregateMetricsAndReturnNewestEventsFirst() {
        MonitoringService monitoringService = new MonitoringService();

        Iso8583Request successRequest = Iso8583Request.builder()
                .mti("0200")
                .stan("000001")
                .fields(Map.of("2", "4111111111111111"))
                .build();
        Iso8583Response successResponse = Iso8583Response.builder()
                .mti("0210")
                .stan("000001")
                .responseCode("00")
                .status("SUCCESS")
                .build();
        monitoringService.recordTransaction(successRequest, successResponse, 120);

        Iso8583Request declinedRequest = Iso8583Request.builder()
                .mti("0200")
                .stan("000002")
                .fields(Map.of("2", "4111111111111111"))
                .build();
        Iso8583Response declinedResponse = Iso8583Response.builder()
                .mti("0210")
                .stan("000002")
                .responseCode("05")
                .status("DECLINED")
                .build();
        monitoringService.recordTransaction(declinedRequest, declinedResponse, 220);

        Iso8583Request errorRequest = Iso8583Request.builder()
                .mti("0200")
                .stan("000003")
                .fields(Map.of("2", "4111111111111111"))
                .build();
        Iso8583Response errorResponse = Iso8583Response.builder()
                .status("ERROR")
                .errorMessage("Switch timeout")
                .build();
        monitoringService.recordTransaction(errorRequest, errorResponse, 50);

        TrafficMetrics metrics = monitoringService.getCurrentMetrics();
        assertNotNull(metrics);
        assertEquals(3L, metrics.getTotalTransactions());
        assertEquals(1L, metrics.getSuccessfulTransactions());
        assertEquals(1L, metrics.getDeclinedTransactions());
        assertEquals(1L, metrics.getErrorTransactions());
        assertEquals(33.33D, metrics.getSuccessRatePercent());
        assertEquals(130.0D, metrics.getAverageLatencyMs());
        assertEquals(50L, metrics.getMinLatencyMs());
        assertEquals(220L, metrics.getMaxLatencyMs());

        List<TrafficEvent> events = monitoringService.getRecentEvents(2);
        assertEquals(2, events.size());
        assertEquals("ERROR", events.get(0).getStatus());
        assertEquals("DECLINED", events.get(1).getStatus());
    }
}
