package com.atm.iso8583.controller;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.powercard.PowerCardDirectDebitMapper;
import com.atm.iso8583.powercard.model.DirectDebitTransferRequest;
import com.atm.iso8583.powercard.model.DirectDebitTransferResponse;
import com.atm.iso8583.service.Iso8583GatewayService;
import com.atm.iso8583.service.Iso8583ResponseStatusResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/powercard")
@RequiredArgsConstructor
@Tag(name = "powerCARD Direct Debit", description = "XML facade for powerCARD direct debit transfer messages")
public class PowerCardDirectDebitController {

        private final Iso8583GatewayService gatewayService;
        private final PowerCardDirectDebitMapper mapper;
        private final Iso8583ResponseStatusResolver statusResolver;

        @PostMapping(value = "/direct-debit", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
        @Operation(summary = "Submit a direct debit transfer to powerCARD", description = "Accepts XML, maps it to ISO 8583 MTI 1200, forwards it to powerCARD, and returns XML.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Direct debit accepted by the host", content = @Content(schema = @Schema(implementation = DirectDebitTransferResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid XML payload"),
                        @ApiResponse(responseCode = "503", description = "powerCARD host unavailable")
        })
        public ResponseEntity<DirectDebitTransferResponse> directDebit(
                        @Valid @RequestBody DirectDebitTransferRequest request,
                        @RequestHeader(value = "X-Request-ID", required = false) String requestId) {

                log.info("Received powerCARD direct debit request | RequestID={} Ref={}",
                                requestId, request.getTransactionRef());

                Iso8583Request isoRequest = mapper.toIsoRequest(request);
                Iso8583Response isoResponse = gatewayService.processTransaction(isoRequest);
                DirectDebitTransferResponse response = mapper.toXmlResponse(request, isoResponse);

                log.info("Completed powerCARD direct debit request | RequestID={} Ref={} RC={} Status={}",
                                requestId, request.getTransactionRef(), response.getResponseCode(),
                                response.getStatus());

                return ResponseEntity.status(Objects.requireNonNull(statusResolver.resolve(isoResponse)))
                                .body(response);
        }
}
