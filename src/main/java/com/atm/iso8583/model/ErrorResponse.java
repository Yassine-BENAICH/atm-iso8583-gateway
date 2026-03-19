package com.atm.iso8583.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "Error code", example = "VALIDATION_ERROR")
    @JsonProperty("errorCode")
    private String errorCode;

    @Schema(description = "Error message", example = "Invalid request data")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Detailed error messages")
    @JsonProperty("details")
    private List<String> details;

    @Schema(description = "Request path", example = "/api/iso8583/send")
    @JsonProperty("path")
    private String path;

    @Schema(description = "Timestamp of error")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Transaction reference if available")
    @JsonProperty("transactionRef")
    private String transactionRef;
}
