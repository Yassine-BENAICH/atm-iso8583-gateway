package com.atm.iso8583.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ISO 8583 message request")
public class Iso8583Request {

    @NotBlank(message = "MTI is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "MTI must be a 4-digit numeric value")
    @Schema(description = "Message Type Indicator (4 digits)", example = "0200", required = true)
    @JsonProperty("mti")
    private String mti;

    @NotNull(message = "Fields map is required")
    @Size(min = 1, message = "At least one field is required")
    @Schema(description = "ISO 8583 field values (field number as key)", required = true)
    @JsonProperty("fields")
    private Map<String, String> fields;

    @Schema(description = "Optional transaction reference for tracking", example = "TXN-12345")
    @JsonProperty("transactionRef")
    private String transactionRef;
}
