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

/**
 * Core gateway service.
 *
 * <p>
 * Orchestrates the full request/response cycle:
 * <ol>
 * <li>JSON → ISOMsg via {@link Iso8583Codec}</li>
 * <li>ISOMsg → bytes via jPOS packager</li>
 * <li>Bytes sent over TCP via {@link Iso8583Channel}</li>
 * <li>Response bytes → ISOMsg via jPOS packager</li>
 * <li>ISOMsg → JSON via {@link Iso8583Codec}</li>
 * </ol>
 */
@Service
public class Iso8583GatewayService {

    private static final Logger log = LoggerFactory.getLogger(Iso8583GatewayService.class);

    private final Iso8583Codec codec;
    private final Iso8583Channel channel;
    private final GenericPackager packager;

    public Iso8583GatewayService(Iso8583Codec codec,
            Iso8583Channel channel) throws ISOException {
        this.codec = codec;
        this.channel = channel;
        // Load the locally editable packager XML from resources.
        // You can now modify src/main/resources/packager/custom_iso87.xml to match your
        // host.
        InputStream packagerXml = getClass().getClassLoader()
                .getResourceAsStream("packager/custom_iso87.xml");
        if (packagerXml == null) {
            throw new ISOException("Cannot find packager/custom_iso87.xml in resources");
        }
        this.packager = new GenericPackager(packagerXml);
    }

    /**
     * Full round-trip: JSON request → ISO 8583 bytes → switch → ISO 8583 bytes →
     * JSON response.
     *
     * @param request JSON request DTO
     * @return JSON response DTO
     */
    public Iso8583Response process(Iso8583Request request) {
        long start = System.currentTimeMillis();
        log.info("Processing request | MTI={} STAN={}", request.getMti(), request.getStan());

        try {
            // Step 1: JSON → ISOMsg
            ISOMsg requestMsg = codec.toISOMsg(request);
            requestMsg.setPackager(packager);

            // Step 2: ISOMsg → bytes
            byte[] requestBytes = requestMsg.pack();
            log.info("Packed ISO 8583 message | {} bytes", requestBytes.length);

            // Step 3: Send over TCP, receive response bytes
            byte[] responseBytes = channel.sendAndReceive(requestBytes);

            // Step 4: bytes → ISOMsg
            ISOMsg responseMsg = new ISOMsg();
            responseMsg.setPackager(packager);
            responseMsg.unpack(responseBytes);

            // Step 5: ISOMsg → JSON
            Iso8583Response response = codec.fromISOMsg(responseMsg);

            long elapsed = System.currentTimeMillis() - start;
            log.info("Request completed | MTI={} RC={} elapsed={}ms",
                    response.getMti(), response.getResponseCode(), elapsed);

            return response;

        } catch (ISOException e) {
            log.error("ISO 8583 codec error: {}", e.getMessage(), e);
            return buildErrorResponse("ISO codec error: " + e.getMessage());
        } catch (IOException e) {
            log.error("Network error communicating with switch: {}", e.getMessage(), e);
            return buildErrorResponse("Network error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing request: {}", e.getMessage(), e);
            return buildErrorResponse("Unexpected error: " + e.getMessage());
        }
    }

    // ─── Echo / network-management helpers ─────────────────────────────────────

    /**
     * Sends an ISO 8583 0800 Network Management (echo) request.
     */
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

    // ─── Private helpers ────────────────────────────────────────────────────────

    private Iso8583Response buildErrorResponse(String message) {
        return Iso8583Response.builder()
                .status("ERROR")
                .errorMessage(message)
                .timestamp(Instant.now())
                .build();
    }

    private String currentTransmissionDateTime() {
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMddHHmmss")
                .withZone(java.time.ZoneOffset.UTC);
        return fmt.format(java.time.Instant.now());
    }

    /** Backward-compatible wrapper used by legacy controller */
    public Iso8583Response processTransaction(Iso8583Request request) {
        return process(request);
    }
}
