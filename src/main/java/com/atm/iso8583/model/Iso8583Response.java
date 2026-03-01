package com.atm.iso8583.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

/**
 * ISO 8583 response model returned to the REST client (JSON).
 * Contains response-specific fields in addition to echo fields from the
 * request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "ISO 8583 response converted to JSON")
public class Iso8583Response {

    @Schema(description = "Response MTI", example = "0210")
    @JsonProperty("mti")
    private String mti;

    // ─── Bit 3: Processing Code (echo) ────────────────────────────────────────
    @Schema(description = "DE3 - Processing Code (echoed from request)", example = "000000")
    @JsonProperty("processingCode")
    private String processingCode;

    // ─── Bit 4: Amount (echo) ─────────────────────────────────────────────────
    @Schema(description = "DE4 - Transaction Amount", example = "000000010000")
    @JsonProperty("amount")
    private String amount;

    // ─── Bit 7: Transmission Date/Time (echo) ─────────────────────────────────
    @Schema(description = "DE7 - Transmission Date/Time", example = "0301020500")
    @JsonProperty("transmissionDateTime")
    private String transmissionDateTime;

    // ─── Bit 11: STAN (echo) ──────────────────────────────────────────────────
    @Schema(description = "DE11 - STAN (echoed from request)", example = "000001")
    @JsonProperty("stan")
    private String stan;

    // ─── Bit 12: Local Time (echo) ────────────────────────────────────────────
    @Schema(description = "DE12 - Local Transaction Time", example = "020500")
    @JsonProperty("localTime")
    private String localTime;

    // ─── Bit 13: Local Date (echo) ────────────────────────────────────────────
    @Schema(description = "DE13 - Local Transaction Date", example = "0301")
    @JsonProperty("localDate")
    private String localDate;

    // ─── Bit 37: Retrieval Reference Number (echo) ────────────────────────────
    @Schema(description = "DE37 - Retrieval Reference Number", example = "123456789012")
    @JsonProperty("retrievalReferenceNumber")
    private String retrievalReferenceNumber;

    // ─── Bit 38: Authorization Identification Response ────────────────────────
    @Schema(description = "DE38 - Authorization Identification Response Code", example = "AUTH01")
    @JsonProperty("authorizationCode")
    private String authorizationCode;

    // ─── Bit 39: Response Code ─────────────────────────────────────────────────
    @Schema(description = "DE39 - Response Code", example = "00")
    @JsonProperty("responseCode")
    private String responseCode;

    // ─── Bit 41: Terminal ID (echo) ───────────────────────────────────────────
    @Schema(description = "DE41 - Terminal ID", example = "TERM0001")
    @JsonProperty("terminalId")
    private String terminalId;

    // ─── Bit 42: Merchant ID (echo) ───────────────────────────────────────────
    @Schema(description = "DE42 - Merchant ID", example = "MERCHANT000001 ")
    @JsonProperty("merchantId")
    private String merchantId;

    // ─── Bit 49: Currency Code (echo) ─────────────────────────────────────────
    @Schema(description = "DE49 - Currency Code", example = "978")
    @JsonProperty("currencyCode")
    private String currencyCode;

    // ─── Bit 55: ICC/EMV Response Data ────────────────────────────────────────
    @Schema(description = "DE55 - ICC/EMV Response Data (hex-encoded)")
    @JsonProperty("emvResponseData")
    private String emvResponseData;

    // ─── Bit 70: Network Management Code (for 0810) ───────────────────────────
    @Schema(description = "DE70 - Network Management Code", example = "001")
    @JsonProperty("networkManagementCode")
    private String networkManagementCode;

    // ─── Additional response fields ────────────────────────────────────────────
    @Schema(description = "Additional response fields as bit-number → value map")
    @JsonProperty("additionalFields")
    private Map<Integer, String> additionalFields;

    // ─── API-level metadata ────────────────────────────────────────────────────
    @Schema(description = "Human-readable description of the response code")
    @JsonProperty("responseDescription")
    private String responseDescription;

    @Schema(description = "Timestamp of the API response")
    @JsonProperty("timestamp")
    private Instant timestamp;

    @Schema(description = "Processing status: SUCCESS or ERROR")
    @JsonProperty("status")
    private String status;

    @Schema(description = "Error message in case of connectivity or parsing failure")
    @JsonProperty("errorMessage")
    private String errorMessage;
}
