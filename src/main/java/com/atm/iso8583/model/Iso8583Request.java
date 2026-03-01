package com.atm.iso8583.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

/**
 * Generic ISO 8583 request model (JSON ↔ ISO 8583).
 * Each field corresponds to an ISO 8583 data element.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "ISO 8583 message request in JSON format")
public class Iso8583Request {

    @Schema(description = "Message Type Indicator (MTI)", example = "0200", required = true)
    @NotBlank(message = "MTI is required")
    @Pattern(regexp = "0[12489][0-9]{2}", message = "MTI must be a valid 4-digit ISO 8583 message type")
    @JsonProperty("mti")
    private String mti;

    // ─── Bit 2: Primary Account Number ────────────────────────────────────────
    @Schema(description = "DE2 - Primary Account Number (PAN)", example = "4111111111111111")
    @JsonProperty("pan")
    @Size(min = 13, max = 19)
    private String pan;

    // ─── Bit 3: Processing Code ────────────────────────────────────────────────
    @Schema(description = "DE3 - Processing Code", example = "000000")
    @JsonProperty("processingCode")
    @Size(min = 6, max = 6)
    private String processingCode;

    // ─── Bit 4: Transaction Amount ─────────────────────────────────────────────
    @Schema(description = "DE4 - Transaction Amount (in smallest currency unit)", example = "000000010000")
    @JsonProperty("amount")
    @Size(min = 1, max = 12)
    private String amount;

    // ─── Bit 7: Transmission Date/Time ────────────────────────────────────────
    @Schema(description = "DE7 - Transmission Date & Time (MMDDHHmmss)", example = "0301020500")
    @JsonProperty("transmissionDateTime")
    @Size(min = 10, max = 10)
    private String transmissionDateTime;

    // ─── Bit 11: Systems Trace Audit Number ───────────────────────────────────
    @Schema(description = "DE11 - Systems Trace Audit Number (STAN)", example = "000001")
    @JsonProperty("stan")
    @Size(min = 6, max = 6)
    private String stan;

    // ─── Bit 12: Local Transaction Time ───────────────────────────────────────
    @Schema(description = "DE12 - Local Transaction Time (HHmmss)", example = "020500")
    @JsonProperty("localTime")
    @Size(min = 6, max = 6)
    private String localTime;

    // ─── Bit 13: Local Transaction Date ───────────────────────────────────────
    @Schema(description = "DE13 - Local Transaction Date (MMDD)", example = "0301")
    @JsonProperty("localDate")
    @Size(min = 4, max = 4)
    private String localDate;

    // ─── Bit 14: Expiration Date ───────────────────────────────────────────────
    @Schema(description = "DE14 - Card Expiration Date (YYMM)", example = "2712")
    @JsonProperty("expirationDate")
    @Size(min = 4, max = 4)
    private String expirationDate;

    // ─── Bit 18: Merchant Category Code ───────────────────────────────────────
    @Schema(description = "DE18 - Merchant Category Code (MCC)", example = "6011")
    @JsonProperty("merchantCategoryCode")
    @Size(min = 4, max = 4)
    private String merchantCategoryCode;

    // ─── Bit 22: POS Entry Mode ────────────────────────────────────────────────
    @Schema(description = "DE22 - POS Entry Mode", example = "021")
    @JsonProperty("posEntryMode")
    @Size(min = 3, max = 3)
    private String posEntryMode;

    // ─── Bit 23: Card Sequence Number ─────────────────────────────────────────
    @Schema(description = "DE23 - Card Sequence Number", example = "001")
    @JsonProperty("cardSequenceNumber")
    @Size(min = 3, max = 3)
    private String cardSequenceNumber;

    // ─── Bit 25: POS Condition Code ───────────────────────────────────────────
    @Schema(description = "DE25 - POS Condition Code", example = "00")
    @JsonProperty("posConditionCode")
    @Size(min = 2, max = 2)
    private String posConditionCode;

    // ─── Bit 32: Acquiring Institution ID ─────────────────────────────────────
    @Schema(description = "DE32 - Acquiring Institution ID Code", example = "000001")
    @JsonProperty("acquiringInstitutionId")
    private String acquiringInstitutionId;

    // ─── Bit 35: Track 2 Data ─────────────────────────────────────────────────
    @Schema(description = "DE35 - Track 2 Equivalent Data")
    @JsonProperty("track2Data")
    private String track2Data;

    // ─── Bit 37: Retrieval Reference Number ───────────────────────────────────
    @Schema(description = "DE37 - Retrieval Reference Number", example = "123456789012")
    @JsonProperty("retrievalReferenceNumber")
    @Size(min = 12, max = 12)
    private String retrievalReferenceNumber;

    // ─── Bit 41: Card Acceptor Terminal ID ────────────────────────────────────
    @Schema(description = "DE41 - Card Acceptor Terminal ID", example = "TERM0001")
    @JsonProperty("terminalId")
    @Size(min = 8, max = 8)
    private String terminalId;

    // ─── Bit 42: Card Acceptor ID Code ────────────────────────────────────────
    @Schema(description = "DE42 - Card Acceptor ID Code", example = "MERCHANT000001 ")
    @JsonProperty("merchantId")
    @Size(min = 15, max = 15)
    private String merchantId;

    // ─── Bit 43: Card Acceptor Name/Location ──────────────────────────────────
    @Schema(description = "DE43 - Card Acceptor Name/Location", example = "ATM BRANCH 01       PARIS      FR")
    @JsonProperty("cardAcceptorNameLocation")
    @Size(max = 40)
    private String cardAcceptorNameLocation;

    // ─── Bit 49: Currency Code ─────────────────────────────────────────────────
    @Schema(description = "DE49 - Currency Code (ISO 4217 numeric)", example = "978")
    @JsonProperty("currencyCode")
    @Size(min = 3, max = 3)
    private String currencyCode;

    // ─── Bit 52: Personal Identification Number Data ──────────────────────────
    @Schema(description = "DE52 - PIN Data (hex-encoded, 8 bytes)", example = "1234567890ABCDEF")
    @JsonProperty("pinData")
    @Size(min = 16, max = 16)
    private String pinData;

    // ─── Bit 55: ICC Data (EMV) ────────────────────────────────────────────────
    @Schema(description = "DE55 - ICC Data / EMV data (hex-encoded TLV)")
    @JsonProperty("emvData")
    private String emvData;

    // ─── Bit 60: Additional POS Data ─────────────────────────────────────────
    @Schema(description = "DE60 - Additional POS Data")
    @JsonProperty("additionalPosData")
    private String additionalPosData;

    // ─── Bit 70: Network Management Information Code ──────────────────────────
    @Schema(description = "DE70 - Network Management Information Code (for 0800)", example = "001")
    @JsonProperty("networkManagementCode")
    private String networkManagementCode;

    // ─── Bit 90: Original Data Elements (for reversal) ────────────────────────
    @Schema(description = "DE90 - Original Data Elements (for reversal 0400)")
    @JsonProperty("originalDataElements")
    private String originalDataElements;

    // ─── Extra / custom fields passed through ─────────────────────────────────
    @Schema(description = "Additional optional fields as key-value pairs (bit number → value)")
    @JsonProperty("additionalFields")
    private Map<Integer, String> additionalFields;
}
