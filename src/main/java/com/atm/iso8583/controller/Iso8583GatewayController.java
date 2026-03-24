package com.atm.iso8583.controller;

import com.atm.iso8583.config.Iso8583Config;
import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.service.Iso8583GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller exposing ISO 8583 gateway endpoints.
 */
@RestController
@RequestMapping("/iso8583")
@Tag(name = "ISO 8583 Gateway", description = "Endpoints for ISO 8583 message exchange with the payment switch")
public class Iso8583GatewayController {

    private final Iso8583GatewayService gatewayService;
    private final Iso8583Config config;

    public Iso8583GatewayController(Iso8583GatewayService gatewayService,
            Iso8583Config config) {
        this.gatewayService = gatewayService;
        this.config = config;
    }

    @Operation(summary = "Send ISO 8583 message", description = """
            Converts the payload (JSON or XML) to an ISO 8583 message, sends it to the configured
            switch, waits for the ISO 8583 response, converts it back to JSON or XML and returns it.

            Supports common MTIs: 0100 (auth), 0200 (financial), 0400 (reversal), 0800 (network), 1200 (presentment).
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ISO 8583 message fields", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Iso8583Request.class), examples = {
            @ExampleObject(name = "Authorization Request (0100)", value = """
                    {
                      "mti": "0100",
                      "pan": "4111111111111111",
                      "processingCode": "000000",
                      "amount": "000000010000",
                      "transmissionDateTime": "0301020500",
                      "stan": "000001",
                      "localTime": "020500",
                      "localDate": "0301",
                      "expirationDate": "2712",
                      "merchantCategoryCode": "6011",
                      "posEntryMode": "021",
                      "posConditionCode": "00",
                      "acquiringInstitutionId": "000001",
                      "retrievalReferenceNumber": "123456789012",
                      "terminalId": "TERM0001",
                      "merchantId": "MERCHANT000001 ",
                      "cardAcceptorNameLocation": "ATM BRANCH 01       PARIS      FR",
                      "currencyCode": "978"
                    }
                    """),
            @ExampleObject(name = "Financial Request (0200)", value = """
                    {
                      "mti": "0200",
                      "pan": "4111111111111111",
                      "processingCode": "010000",
                      "amount": "000000050000",
                      "transmissionDateTime": "0301030000",
                      "stan": "000002",
                      "localTime": "030000",
                      "localDate": "0301",
                      "expirationDate": "2712",
                      "merchantCategoryCode": "6011",
                      "posEntryMode": "021",
                      "posConditionCode": "00",
                      "acquiringInstitutionId": "000001",
                      "retrievalReferenceNumber": "123456789013",
                      "terminalId": "TERM0001",
                      "merchantId": "MERCHANT000001 ",
                      "currencyCode": "978",
                      "currencyCode": "978",
                      "pinData": "1234567890ABCDEF"
                    }
                    """),
            @ExampleObject(name = "Reversal (0400)", value = """
                    {
                      "mti": "0400",
                      "pan": "4111111111111111",
                      "processingCode": "000000",
                      "amount": "000000010000",
                      "transmissionDateTime": "0301040000",
                      "stan": "000003",
                      "localTime": "040000",
                      "localDate": "0301",
                      "acquiringInstitutionId": "000001",
                      "retrievalReferenceNumber": "123456789012",
                      "terminalId": "TERM0001",
                      "merchantId": "MERCHANT000001 ",
                      "currencyCode": "978",
                      "originalDataElements": "01000000012345600000010000301020500"
                    }
                    """),
            @ExampleObject(name = "Network Echo (0800)", value = """
                    {
                      "mti": "0800",
                      "stan": "000000",
                      "transmissionDateTime": "0301020500",
                      "acquiringInstitutionId": "000001",
                      "networkManagementCode": "301"
                    }
                    """),
            @ExampleObject(name = "Presentment (1200)", value = """
                    {
                      "mti": "1200",
                      "pan": "4111111111111111",
                      "processingCode": "200000",
                      "amount": "000000050000",
                      "transmissionDateTime": "0301040000",
                      "stan": "000010",
                      "localTime": "040000",
                      "localDate": "0301",
                      "acquiringInstitutionId": "000001",
                      "retrievalReferenceNumber": "123456789099",
                      "terminalId": "TERM0001",
                      "merchantId": "MERCHANT000001 ",
                      "currencyCode": "978"
                    }
                    """)
            }),
            @Content(mediaType = MediaType.APPLICATION_XML_VALUE, schema = @Schema(implementation = Iso8583Request.class))
    }))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Switch response successfully converted", content = @Content(schema = @Schema(implementation = Iso8583Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "503", description = "Switch unreachable or timeout")
    })
    @PostMapping(value = "/send",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Iso8583Response> send(@Valid @RequestBody Iso8583Request request) {
        Iso8583Response response = gatewayService.process(request);
        int httpStatus = resolveHttpStatus(response);
        return ResponseEntity.status(httpStatus).body(response);
    }

    // Convenience endpoints

    @Operation(summary = "Authorization request (0100/0110)", description = "Shortcut endpoint - equivalent to POST /send with MTI=0100")
    @PostMapping(value = "/authorize",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Iso8583Response> authorize(@Valid @RequestBody Iso8583Request request) {
        request.setMti("0100");
        return send(request);
    }

    @Operation(summary = "Financial transaction request (0200/0210)", description = "Shortcut endpoint - equivalent to POST /send with MTI=0200")
    @PostMapping(value = "/financial",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Iso8583Response> financial(@Valid @RequestBody Iso8583Request request) {
        request.setMti("0200");
        return send(request);
    }

    @Operation(summary = "Financial presentment request (1200/1210)", description = "Shortcut endpoint - equivalent to POST /send with MTI=1200")
    @PostMapping(value = "/presentment",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Iso8583Response> presentment(@Valid @RequestBody Iso8583Request request) {
        request.setMti("1200");
        return send(request);
    }

    @Operation(summary = "Reversal request (0400/0410)", description = "Shortcut endpoint - equivalent to POST /send with MTI=0400")
    @PostMapping(value = "/reversal",
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE },
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Iso8583Response> reversal(@Valid @RequestBody Iso8583Request request) {
        request.setMti("0400");
        return send(request);
    }

    @Operation(summary = "Network Management / Echo test (0800/0810)", description = "Sends a network echo (0800/NMC=301) to verify switch connectivity")
    @PostMapping(value = "/echo",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Iso8583Response> echo() {
        Iso8583Response response = gatewayService.sendEchoTest(config.getInstitutionId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Gateway Configuration", description = "Returns the current target host and port configuration")
    @GetMapping(value = "/config",
            produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> cfgMap = new HashMap<>();
        cfgMap.put("host", config.getHost());
        cfgMap.put("port", config.getPort());
        cfgMap.put("institutionId", config.getInstitutionId());
        cfgMap.put("connectTimeout", config.getConnectTimeout());
        cfgMap.put("readTimeout", config.getReadTimeout());
        return ResponseEntity.ok(cfgMap);
    }

    // HTTP status helpers

    private int resolveHttpStatus(Iso8583Response response) {
        if ("ERROR".equals(response.getStatus())) {
            String err = response.getErrorMessage();
            if (err != null && err.contains("timeout"))
                return 504;
            return 503;
        }
        return 200;
    }
}
