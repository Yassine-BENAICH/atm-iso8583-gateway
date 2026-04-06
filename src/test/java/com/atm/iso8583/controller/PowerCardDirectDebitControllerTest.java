package com.atm.iso8583.controller;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.service.Iso8583GatewayService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@SpringBootTest
@AutoConfigureMockMvc
class PowerCardDirectDebitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private Iso8583GatewayService gatewayService;

    @Test
    void testDirectDebitXmlRequestMapsToIso1200AndReturnsXml() throws Exception {
        when(gatewayService.processTransaction(any(Iso8583Request.class)))
                .thenReturn(Iso8583Response.builder()
                        .mti("1210")
                        .stan("123456")
                        .retrievalReferenceNumber("123456789012")
                        .authorizationCode("AUTH99")
                        .responseCode("00")
                        .responseDescription("Approved")
                        .status("SUCCESS")
                        .processingTimeMs(142L)
                        .timestamp(Instant.parse("2026-03-25T12:00:00Z"))
                        .additionalFields(Map.of(102, "SRC000123", 103, "DST000456"))
                        .build());

        String xml = """
                <DirectDebitTransferRequest>
                  <transactionRef>DDR-0001</transactionRef>
                  <processingCode>400000</processingCode>
                  <amount>000000010000</amount>
                  <currencyCode>978</currencyCode>
                  <transmissionDateTime>0325120000</transmissionDateTime>
                  <stan>123456</stan>
                  <localTime>120000</localTime>
                  <localDate>0325</localDate>
                  <retrievalReferenceNumber>123456789012</retrievalReferenceNumber>
                  <acquiringInstitutionId>000001</acquiringInstitutionId>
                  <terminalId>TERM0001</terminalId>
                  <merchantId>MERCHANT000001 </merchantId>
                  <merchantCategoryCode>6011</merchantCategoryCode>
                  <posEntryMode>021</posEntryMode>
                  <posConditionCode>00</posConditionCode>
                  <sourceAccount>SRC000123</sourceAccount>
                  <destinationAccount>DST000456</destinationAccount>
                  <narrative>Payroll transfer</narrative>
                  <additionalFields>
                    <field>
                      <id>48</id>
                      <value>Original narrative</value>
                    </field>
                  </additionalFields>
                </DirectDebitTransferRequest>
                """;

        mockMvc.perform(post("/api/powercard/direct-debit")
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .content(xml))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("/DirectDebitTransferResponse/requestMti").string("1200"))
                .andExpect(xpath("/DirectDebitTransferResponse/responseMti").string("1210"))
                .andExpect(xpath("/DirectDebitTransferResponse/responseCode").string("00"))
                .andExpect(xpath("/DirectDebitTransferResponse/status").string("SUCCESS"))
                .andExpect(xpath("/DirectDebitTransferResponse/transactionRef").string("DDR-0001"));

        ArgumentCaptor<Iso8583Request> captor = ArgumentCaptor.forClass(Iso8583Request.class);
        verify(gatewayService).processTransaction(captor.capture());

        Iso8583Request isoRequest = captor.getValue();
        assertThat(isoRequest.getMti()).isEqualTo("1200");
        assertThat(isoRequest.getProcessingCode()).isEqualTo("400000");
        assertThat(isoRequest.getAmount()).isEqualTo("000000010000");
        assertThat(isoRequest.getStan()).isEqualTo("123456");
        assertThat(isoRequest.getAdditionalFields()).containsEntry(102, "SRC000123");
        assertThat(isoRequest.getAdditionalFields()).containsEntry(103, "DST000456");
        assertThat(isoRequest.getAdditionalFields()).containsEntry(48, "Payroll transfer");
    }

    @Test
    void testDirectDebitValidationErrorWhenAmountMissing() throws Exception {
        String xml = """
                <DirectDebitTransferRequest>
                  <transactionRef>DDR-0002</transactionRef>
                  <sourceAccount>SRC000123</sourceAccount>
                  <destinationAccount>DST000456</destinationAccount>
                </DirectDebitTransferRequest>
                """;

        mockMvc.perform(post("/api/powercard/direct-debit")
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .content(xml))
                .andExpect(status().isBadRequest());
    }
}
