package com.atm.iso8583.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void metricsEndpointShouldReturnAggregatedMetrics() throws Exception {
        mockMvc.perform(get("/api/monitoring/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").exists())
                .andExpect(jsonPath("$.successRatePercent").exists())
                .andExpect(jsonPath("$.averageLatencyMs").exists());
    }

    @Test
    void eventsEndpointShouldReturnArray() throws Exception {
        mockMvc.perform(get("/api/monitoring/events").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void eventsEndpointShouldValidateLimitRange() throws Exception {
        mockMvc.perform(get("/api/monitoring/events").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Constraint Violation"));
    }
}
