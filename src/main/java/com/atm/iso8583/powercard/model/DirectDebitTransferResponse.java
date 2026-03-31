package com.atm.iso8583.powercard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
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
@JacksonXmlRootElement(localName = "DirectDebitTransferResponse")
public class DirectDebitTransferResponse {

    @JacksonXmlProperty(localName = "transactionRef")
    private String transactionRef;

    @JacksonXmlProperty(localName = "requestMti")
    private String requestMti;

    @JacksonXmlProperty(localName = "responseMti")
    private String responseMti;

    @JacksonXmlProperty(localName = "stan")
    private String stan;

    @JacksonXmlProperty(localName = "retrievalReferenceNumber")
    private String retrievalReferenceNumber;

    @JacksonXmlProperty(localName = "authorizationCode")
    private String authorizationCode;

    @JacksonXmlProperty(localName = "responseCode")
    private String responseCode;

    @JacksonXmlProperty(localName = "responseDescription")
    private String responseDescription;

    @JacksonXmlProperty(localName = "status")
    private String status;

    @JacksonXmlProperty(localName = "errorMessage")
    private String errorMessage;

    @JacksonXmlProperty(localName = "processingTimeMs")
    private Long processingTimeMs;

    @JacksonXmlProperty(localName = "timestamp")
    private String timestamp;

    @JacksonXmlElementWrapper(localName = "additionalFields")
    @JacksonXmlProperty(localName = "field")
    private List<PowerCardField> additionalFields;
}
