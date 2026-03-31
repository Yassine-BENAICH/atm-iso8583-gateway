package com.atm.iso8583.powercard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName = "DirectDebitTransferRequest")
public class DirectDebitTransferRequest {

    @JacksonXmlProperty(localName = "transactionRef")
    private String transactionRef;

    @JacksonXmlProperty(localName = "processingCode")
    private String processingCode;

    @NotBlank(message = "amount is required")
    @JacksonXmlProperty(localName = "amount")
    private String amount;

    @JacksonXmlProperty(localName = "currencyCode")
    private String currencyCode;

    @JacksonXmlProperty(localName = "transmissionDateTime")
    private String transmissionDateTime;

    @JacksonXmlProperty(localName = "stan")
    private String stan;

    @JacksonXmlProperty(localName = "localTime")
    private String localTime;

    @JacksonXmlProperty(localName = "localDate")
    private String localDate;

    @JacksonXmlProperty(localName = "retrievalReferenceNumber")
    private String retrievalReferenceNumber;

    @JacksonXmlProperty(localName = "acquiringInstitutionId")
    private String acquiringInstitutionId;

    @JacksonXmlProperty(localName = "terminalId")
    private String terminalId;

    @JacksonXmlProperty(localName = "merchantId")
    private String merchantId;

    @JacksonXmlProperty(localName = "merchantCategoryCode")
    private String merchantCategoryCode;

    @JacksonXmlProperty(localName = "posEntryMode")
    private String posEntryMode;

    @JacksonXmlProperty(localName = "posConditionCode")
    private String posConditionCode;

    @JacksonXmlProperty(localName = "cardAcceptorNameLocation")
    private String cardAcceptorNameLocation;

    @NotBlank(message = "sourceAccount is required")
    @JacksonXmlProperty(localName = "sourceAccount")
    private String sourceAccount;

    @NotBlank(message = "destinationAccount is required")
    @JacksonXmlProperty(localName = "destinationAccount")
    private String destinationAccount;

    @JacksonXmlProperty(localName = "narrative")
    private String narrative;

    @JacksonXmlElementWrapper(localName = "additionalFields")
    @JacksonXmlProperty(localName = "field")
    private List<PowerCardField> additionalFields;
}
