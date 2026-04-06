package com.atm.iso8583.powercard;

import com.atm.iso8583.config.Iso8583Config;
import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.powercard.model.DirectDebitTransferRequest;
import com.atm.iso8583.powercard.model.DirectDebitTransferResponse;
import com.atm.iso8583.powercard.model.PowerCardField;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PowerCardDirectDebitMapper {

    private static final DateTimeFormatter TRANSMISSION_FORMAT =
            DateTimeFormatter.ofPattern("MMddHHmmss").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter LOCAL_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HHmmss").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter LOCAL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMdd").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter RRN_FORMAT =
            DateTimeFormatter.ofPattern("DDDHHmmssSSS").withZone(ZoneOffset.UTC);

    private final Iso8583Config iso8583Config;

    public PowerCardDirectDebitMapper(Iso8583Config iso8583Config) {
        this.iso8583Config = iso8583Config;
    }

    public Iso8583Request toIsoRequest(DirectDebitTransferRequest request) {
        String stan = hasText(request.getStan()) ? request.getStan() : generateStan();
        Instant now = Instant.now();

        Map<Integer, String> additionalFields = new LinkedHashMap<>();
        if (request.getAdditionalFields() != null) {
            request.getAdditionalFields().stream()
                    .filter(field -> field.getId() != null && hasText(field.getValue()))
                    .forEach(field -> additionalFields.put(field.getId(), field.getValue()));
        }

        // DE102 and DE103 model the source and destination accounts for transfer flows.
        additionalFields.put(102, request.getSourceAccount());
        additionalFields.put(103, request.getDestinationAccount());
        if (hasText(request.getNarrative())) {
            additionalFields.put(48, request.getNarrative());
        }

        return Iso8583Request.builder()
                .mti("1200")
                .processingCode(request.getProcessingCode())
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode())
                .transmissionDateTime(defaultIfBlank(request.getTransmissionDateTime(), TRANSMISSION_FORMAT.format(now)))
                .stan(stan)
                .localTime(defaultIfBlank(request.getLocalTime(), LOCAL_TIME_FORMAT.format(now)))
                .localDate(defaultIfBlank(request.getLocalDate(), LOCAL_DATE_FORMAT.format(now)))
                .retrievalReferenceNumber(defaultIfBlank(request.getRetrievalReferenceNumber(), RRN_FORMAT.format(now)))
                .acquiringInstitutionId(defaultIfBlank(request.getAcquiringInstitutionId(), iso8583Config.getInstitutionId()))
                .terminalId(request.getTerminalId())
                .merchantId(request.getMerchantId())
                .merchantCategoryCode(request.getMerchantCategoryCode())
                .posEntryMode(request.getPosEntryMode())
                .posConditionCode(request.getPosConditionCode())
                .cardAcceptorNameLocation(request.getCardAcceptorNameLocation())
                .additionalFields(additionalFields)
                .transactionRef(request.getTransactionRef())
                .build();
    }

    public DirectDebitTransferResponse toXmlResponse(DirectDebitTransferRequest request, Iso8583Response response) {
        List<PowerCardField> responseFields = new ArrayList<>();
        if (response != null && response.getAdditionalFields() != null) {
            response.getAdditionalFields().entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getKey))
                    .forEach(entry -> responseFields.add(PowerCardField.builder()
                            .id(entry.getKey())
                            .value(entry.getValue())
                            .build()));
        }

        return DirectDebitTransferResponse.builder()
                .transactionRef(request.getTransactionRef())
                .requestMti("1200")
                .responseMti(response != null ? response.getMti() : null)
                .stan(response != null ? response.getStan() : null)
                .retrievalReferenceNumber(response != null ? response.getRetrievalReferenceNumber() : null)
                .authorizationCode(response != null ? response.getAuthorizationCode() : null)
                .responseCode(response != null ? response.getResponseCode() : null)
                .responseDescription(response != null ? response.getResponseDescription() : null)
                .status(response != null ? response.getStatus() : null)
                .errorMessage(response != null ? response.getErrorMessage() : null)
                .processingTimeMs(response != null ? response.getProcessingTimeMs() : null)
                .timestamp(response != null && response.getTimestamp() != null ? response.getTimestamp().toString() : null)
                .additionalFields(responseFields.isEmpty() ? null : responseFields)
                .build();
    }

    private String generateStan() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    private String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
