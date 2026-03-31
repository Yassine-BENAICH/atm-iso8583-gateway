package com.atm.iso8583.powercard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PowerCardField {

    @JacksonXmlProperty(localName = "id")
    private Integer id;

    @JacksonXmlProperty(localName = "value")
    private String value;
}
