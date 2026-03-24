package com.atm.iso8583.service;

import com.atm.iso8583.codec.Iso8583Codec;
import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import com.atm.iso8583.network.Iso8583Channel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class Iso8583GatewayService {

    private static final Logger log = LoggerFactory.getLogger(Iso8583GatewayService.class);

    private final Iso8583Codec codec;
    private final Iso8583Channel channel;
    private final MonitoringService monitoringService;
    private final GenericPackager packager;

    public Iso8583GatewayService(Iso8583Codec codec,
            Iso8583Channel channel,
            MonitoringService monitoringService) throws ISOException {
        this.codec = codec;
        this.channel = channel;
        this.monitoringService = monitoringService;

        InputStream packagerXml = getClass().getClassLoader().getResourceAsStream("packager/custom_iso87.xml");
        if (packagerXml == null) {
            throw new ISOException("Cannot find packager/custom_iso87.xml in resources");
        }
        this.packager = new GenericPackager(packagerXml);
    }

    public Iso8583Response process(Iso8583Request request) {
        long start = System.currentTimeMillis();
        log.info("Processing request | MTI={} STAN={}", request.getMti(), request.getStan());

        Iso8583Response response = null;
        try {
            ISOMsg requestMsg = codec.toISOMsg(request);
            requestMsg.setPackager(packager);

            byte[] requestBytes = requestMsg.pack();
            log.info("Packed ISO 8583 message | {} bytes", requestBytes.length);

            byte[] responseBytes = channel.sendAndReceive(requestBytes);

            ISOMsg responseMsg = new ISOMsg();
            responseMsg.setPackager(packager);
            responseMsg.unpack(responseBytes);

            response = codec.fromISOMsg(responseMsg);
            log.info("Request completed | MTI={} RC={}",
                    response.getMti(), response.getResponseCode());
        } catch (ISOException e) {
            log.error("ISO 8583 codec error: {}", e.getMessage(), e);
            response = buildErrorResponse("ISO codec error: " + e.getMessage());
        } catch (IOException e) {
            log.error("Network error communicating with switch: {}", e.getMessage(), e);
            response = buildErrorResponse("Network error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing request: {}", e.getMessage(), e);
            response = buildErrorResponse("Unexpected error: " + e.getMessage());
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (response == null) {
                response = buildErrorResponse("Unexpected error: no response produced");
            }
            response.setProcessingTimeMs(elapsed);
            monitoringService.recordTransaction(request, response, elapsed);
            log.info("Transaction finalized | outcome={} RC={} elapsed={}ms",
                    response.getStatus(), response.getResponseCode(), elapsed);
        }

        return response;
    }

    public Iso8583Response sendEchoTest(String institutionId) {
        Iso8583Request echoReq = Iso8583Request.builder()
                .mti("0800")
                .stan("000000")
                .transmissionDateTime(currentTransmissionDateTime())
                .networkManagementCode("301")
                .acquiringInstitutionId(institutionId)
                .build();
        return process(echoReq);
    }

    private Iso8583Response buildErrorResponse(String message) {
        return Iso8583Response.builder()
                .status("ERROR")
                .errorMessage(message)
                .timestamp(Instant.now())
                .build();
    }

    private String currentTransmissionDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss").withZone(ZoneOffset.UTC);
        return formatter.format(Instant.now());
    }

    public Iso8583Response processTransaction(Iso8583Request request) {
        return process(request);
    }
}
