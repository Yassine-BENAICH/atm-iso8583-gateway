package com.atm.iso8583.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "ISO 8583 message response")
public class Iso8583Response {

    @Schema(description = "Message Type Indicator", example = "0210")
    @JsonProperty("mti")
    private String mti;

    @Schema(description = "ISO 8583 field values")
    @JsonProperty("fields")
    private Map<String, String> fields;

    @Schema(description = "Response code (Field 39)", example = "00")
    @JsonProperty("responseCode")
    private String responseCode;

    @Schema(description = "Response message", example = "Approved")
    @JsonProperty("message")
    private String message;

    @Schema(description = "Transaction reference", example = "TXN-12345")
    @JsonProperty("transactionRef")
    private String transactionRef;

    @Schema(description = "Processing timestamp")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Processing duration in milliseconds", example = "150")
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    @Schema(description = "Success indicator")
    @JsonProperty("success")
    private Boolean success;
}
