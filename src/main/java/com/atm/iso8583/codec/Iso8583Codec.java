package com.atm.iso8583.codec;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import org.jpos.iso.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON → ISOMsg (request path): {@link #toISOMsg(Iso8583Request)}
 *
 * ISOMsg → JSON (response path): {@link #fromISOMsg(ISOMsg)}
 */
@Component
public class Iso8583Codec {

    private static final Logger log = LoggerFactory.getLogger(Iso8583Codec.class);

    // ─── MTI response code descriptions ────────────────────────────────────────
    private static final Map<String, String> RESPONSE_DESC = new HashMap<>();
    static {
        RESPONSE_DESC.put("00", "Approved");
        RESPONSE_DESC.put("01", "Refer to card issuer");
        RESPONSE_DESC.put("04", "Pick-up card");
        RESPONSE_DESC.put("05", "Do not honor");
        RESPONSE_DESC.put("06", "Error");
        RESPONSE_DESC.put("07", "Pick-up card, special conditions");
        RESPONSE_DESC.put("12", "Invalid transaction");
        RESPONSE_DESC.put("13", "Invalid amount");
        RESPONSE_DESC.put("14", "Invalid card number");
        RESPONSE_DESC.put("19", "Re-enter transaction");
        RESPONSE_DESC.put("20", "Invalid response");
        RESPONSE_DESC.put("25", "Unable to locate record");
        RESPONSE_DESC.put("30", "Format error");
        RESPONSE_DESC.put("41", "Lost card, pick up");
        RESPONSE_DESC.put("43", "Stolen card, pick up");
        RESPONSE_DESC.put("51", "Insufficient funds");
        RESPONSE_DESC.put("54", "Expired card");
        RESPONSE_DESC.put("55", "Incorrect PIN");
        RESPONSE_DESC.put("57", "Transaction not permitted to cardholder");
        RESPONSE_DESC.put("58", "Transaction not permitted to terminal");
        RESPONSE_DESC.put("61", "Exceeds withdrawal limit");
        RESPONSE_DESC.put("62", "Restricted card");
        RESPONSE_DESC.put("65", "Exceeds withdrawal frequency limit");
        RESPONSE_DESC.put("68", "Response received too late");
        RESPONSE_DESC.put("75", "PIN tries exceeded");
        RESPONSE_DESC.put("76", "Unable to locate previous message (no match)");
        RESPONSE_DESC.put("77",
                "Previous message located for a repeat or reversal, but repeat or reversal data are inconsistent with original message");
        RESPONSE_DESC.put("78", "Blocked, first used");
        RESPONSE_DESC.put("79", "Lifecycle (general)");
        RESPONSE_DESC.put("80", "Credit issuer unavailable");
        RESPONSE_DESC.put("81",
                "PIN cryptographic error found (error found by VIC security module during PIN decryption)");
        RESPONSE_DESC.put("82", "Incorrect CVV");
        RESPONSE_DESC.put("83", "Unable to verify PIN");
        RESPONSE_DESC.put("84", "Invalid authorization lifecycle");
        RESPONSE_DESC.put("85", "No reason to decline (valid for all zero-amount transactions)");
        RESPONSE_DESC.put("88", "Unable to dispense");
        RESPONSE_DESC.put("89", "Administration error");
        RESPONSE_DESC.put("91", "Issuer or switch inoperative");
        RESPONSE_DESC.put("92", "Financial institution or intermediate network facility cannot be found for routing");
        RESPONSE_DESC.put("93", "Transaction cannot be completed – violation of law");
        RESPONSE_DESC.put("94", "Duplicate transmission");
        RESPONSE_DESC.put("95", "Reconcile error");
        RESPONSE_DESC.put("96", "System malfunction");
    }

    // ─── JSON → ISOMsg ─────────────────────────────────────────────────────────

    /**
     * Converts a JSON request model into a jPOS {@link ISOMsg}.
     *
     * @param req the incoming JSON request
     * @return populated ISOMsg ready for transmission
     * @throws ISOException if any field value is invalid
     */
    public ISOMsg toISOMsg(Iso8583Request req) throws ISOException {
        log.debug("Converting JSON to ISOMsg | MTI={}", req.getMti());

        ISOMsg msg = new ISOMsg();
        msg.setMTI(req.getMti());

        // DE 2 – PAN
        setIfPresent(msg, 2, req.getPan());
        // DE 3 – Processing Code
        setIfPresent(msg, 3, req.getProcessingCode());
        // DE 4 – Amount
        setIfPresent(msg, 4, req.getAmount());
        // DE 7 – Transmission Date/Time
        setIfPresent(msg, 7, req.getTransmissionDateTime());
        // DE 11 – STAN
        setIfPresent(msg, 11, req.getStan());
        // DE 12 – Local Time
        setIfPresent(msg, 12, req.getLocalTime());
        // DE 13 – Local Date
        setIfPresent(msg, 13, req.getLocalDate());
        // DE 14 – Expiration Date
        setIfPresent(msg, 14, req.getExpirationDate());
        // DE 18 – Merchant Category Code
        setIfPresent(msg, 18, req.getMerchantCategoryCode());
        // DE 22 – POS Entry Mode
        setIfPresent(msg, 22, req.getPosEntryMode());
        // DE 23 – Card Sequence Number
        setIfPresent(msg, 23, req.getCardSequenceNumber());
        // DE 25 – POS Condition Code
        setIfPresent(msg, 25, req.getPosConditionCode());
        // DE 32 – Acquiring Institution ID
        setIfPresent(msg, 32, req.getAcquiringInstitutionId());
        // DE 35 – Track 2 Data
        setIfPresent(msg, 35, req.getTrack2Data());
        // DE 37 – RRN
        setIfPresent(msg, 37, req.getRetrievalReferenceNumber());
        // DE 41 – Terminal ID
        setIfPresent(msg, 41, req.getTerminalId());
        // DE 42 – Merchant ID
        setIfPresent(msg, 42, req.getMerchantId());
        // DE 43 – Card Acceptor Name/Location
        setIfPresent(msg, 43, req.getCardAcceptorNameLocation());
        // DE 49 – Currency Code
        setIfPresent(msg, 49, req.getCurrencyCode());
        // DE 52 – PIN Data (binary hex)
        if (req.getPinData() != null && !req.getPinData().isBlank()) {
            msg.set(52, ISOUtil.hex2byte(req.getPinData()));
        }
        // DE 55 – EMV / ICC Data (binary hex)
        if (req.getEmvData() != null && !req.getEmvData().isBlank()) {
            msg.set(55, ISOUtil.hex2byte(req.getEmvData()));
        }
        // DE 60 – Additional POS data
        setIfPresent(msg, 60, req.getAdditionalPosData());
        // DE 70 – Network Management Code
        setIfPresent(msg, 70, req.getNetworkManagementCode());
        // DE 90 – Original Data Elements
        setIfPresent(msg, 90, req.getOriginalDataElements());

        // Generic fields map (bit number -> value)
        if (req.getFields() != null) {
            req.getFields().forEach((bitStr, value) -> {
                if (value == null || value.isBlank()) {
                    return;
                }
                try {
                    int bit = Integer.parseInt(bitStr);
                    msg.set(bit, value);
                } catch (NumberFormatException e) {
                    log.warn("Skipping invalid field key '{}': not a number", bitStr);
                } catch (Exception e) {
                    log.warn("Could not set field DE{}: {}", bitStr, e.getMessage());
                }
            });
        }

        // Extra/additional fields
        if (req.getAdditionalFields() != null) {
            req.getAdditionalFields().forEach((bit, value) -> {
                try {
                    msg.set(bit, value);
                } catch (Exception e) {
                    log.warn("Could not set additional field DE{}: {}", bit, e.getMessage());
                }
            });
        }

        return msg;
    }

    // ─── ISOMsg → JSON ─────────────────────────────────────────────────────────

    /**
     * Converts a jPOS {@link ISOMsg} received from the switch into a JSON response
     * model.
     *
     * @param msg the ISOMsg received from the network
     * @return populated {@link Iso8583Response}
     * @throws ISOException if parsing fails
     */
    public Iso8583Response fromISOMsg(ISOMsg msg) throws ISOException {
        log.debug("Converting ISOMsg to JSON | MTI={}", msg.getMTI());

        String responseCode = getField(msg, 39);
        String mti = msg.getMTI();

        Iso8583Response.Iso8583ResponseBuilder builder = Iso8583Response.builder()
                .mti(mti)
                .processingCode(getField(msg, 3))
                .amount(getField(msg, 4))
                .transmissionDateTime(getField(msg, 7))
                .stan(getField(msg, 11))
                .localTime(getField(msg, 12))
                .localDate(getField(msg, 13))
                .retrievalReferenceNumber(getField(msg, 37))
                .authorizationCode(getField(msg, 38))
                .responseCode(responseCode)
                .terminalId(getField(msg, 41))
                .merchantId(getField(msg, 42))
                .currencyCode(getField(msg, 49))
                .networkManagementCode(getField(msg, 70))
                .responseDescription(RESPONSE_DESC.getOrDefault(responseCode, "Unknown response code"))
                .timestamp(java.time.Instant.now());

        // DE 55 – EMV response data (binary → hex)
        if (msg.hasField(55)) {
            byte[] emvBytes = msg.getBytes(55);
            if (emvBytes != null) {
                builder.emvResponseData(ISOUtil.hexString(emvBytes));
            }
        }

        // Collect any remaining fields into additionalFields
        Map<Integer, String> additional = new HashMap<>();
        for (int bit = 2; bit <= 128; bit++) {
            if (msg.hasField(bit) && !isKnownField(bit)) {
                try {
                    String value = msg.getString(bit);
                    if (value != null) {
                        additional.put(bit, value);
                    }
                } catch (Exception e) {
                    log.trace("Cannot stringify DE{}: {}", bit, e.getMessage());
                }
            }
        }
        if (!additional.isEmpty()) {
            builder.additionalFields(additional);
        }

        // Set overall status based on response code
        if ("00".equals(responseCode) || "85".equals(responseCode)) {
            builder.status("SUCCESS");
        } else if (responseCode != null) {
            builder.status("DECLINED");
        } else {
            builder.status("UNKNOWN");
        }

        return builder.build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void setIfPresent(ISOMsg msg, int fieldNo, String value) {
        if (value != null && !value.isBlank()) {
            msg.set(fieldNo, value);
        }
    }

    private String getField(ISOMsg msg, int fieldNo) {
        try {
            return msg.hasField(fieldNo) ? msg.getString(fieldNo) : null;
        } catch (Exception e) {
            log.trace("Error reading DE{}: {}", fieldNo, e.getMessage());
            return null;
        }
    }

    /**
     * Returns true for data elements that are already mapped to named response
     * fields.
     */
    private boolean isKnownField(int bit) {
        return switch (bit) {
            case 3, 4, 7, 11, 12, 13, 37, 38, 39, 41, 42, 49, 55, 70 -> true;
            default -> false;
        };
    }

}
