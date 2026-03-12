package com.atm.iso8583.controller;

import com.atm.iso8583.model.Iso8583Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Iso8583ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/iso8583/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void testStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/iso8583/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void testSendMessageValidationError_MissingMti() throws Exception {
        Iso8583Request request = Iso8583Request.builder()
                .fields(Map.of("2", "1234567890123456"))
                .build();

        mockMvc.perform(post("/api/iso8583/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testSendMessageValidationError_InvalidMti() throws Exception {
        Iso8583Request request = Iso8583Request.builder()
                .mti("ABC")
                .fields(Map.of("2", "1234567890123456"))
                .build();

        mockMvc.perform(post("/api/iso8583/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testSendMessageValidationError_EmptyFields() throws Exception {
        Iso8583Request request = Iso8583Request.builder()
                .mti("0200")
                .fields(new HashMap<>())
                .build();

        mockMvc.perform(post("/api/iso8583/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testRequestIdHeaderPropagation() throws Exception {
        String requestId = "TEST-REQ-123";

        mockMvc.perform(get("/api/iso8583/health")
                        .header("X-Request-ID", requestId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-ID", requestId));
    }
}
