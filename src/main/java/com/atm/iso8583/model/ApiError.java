package com.atm.iso8583.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * Standard API error response wrapper.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public class ApiError {

    @Schema(description = "HTTP status code", example = "400")
    @JsonProperty("status")
    private int status;

    @Schema(description = "Error title", example = "Validation Error")
    @JsonProperty("error")
    private String error;

    @Schema(description = "Detailed error message")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Request path that caused the error")
    @JsonProperty("path")
    private String path;

    @Schema(description = "Time of the error")
    @JsonProperty("timestamp")
    @Builder.Default
    private Instant timestamp = Instant.now();
}
