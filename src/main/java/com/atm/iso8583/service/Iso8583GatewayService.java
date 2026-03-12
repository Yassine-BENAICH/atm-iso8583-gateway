package com.atm.iso8583.service;

import com.atm.iso8583.channel.Iso8583Channel;
import com.atm.iso8583.codec.Iso8583Codec;
import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class Iso8583GatewayService {

    private final Iso8583Codec codec;
    private final Iso8583Channel channel;
    private static final Map<String, String> RESPONSE_MESSAGES = new HashMap<>();

    static {
        RESPONSE_MESSAGES.put("00", "Approved");
        RESPONSE_MESSAGES.put("01", "Refer to card issuer");
        RESPONSE_MESSAGES.put("03", "Invalid merchant");
        RESPONSE_MESSAGES.put("05", "Do not honor");
        RESPONSE_MESSAGES.put("12", "Invalid transaction");
        RESPONSE_MESSAGES.put("13", "Invalid amount");
        RESPONSE_MESSAGES.put("14", "Invalid card number");
        RESPONSE_MESSAGES.put("30", "Format error");
        RESPONSE_MESSAGES.put("41", "Lost card");
        RESPONSE_MESSAGES.put("43", "Stolen card");
        RESPONSE_MESSAGES.put("51", "Insufficient funds");
        RESPONSE_MESSAGES.put("54", "Expired card");
        RESPONSE_MESSAGES.put("55", "Incorrect PIN");
        RESPONSE_MESSAGES.put("61", "Exceeds withdrawal limit");
        RESPONSE_MESSAGES.put("91", "Issuer or switch inoperative");
        RESPONSE_MESSAGES.put("96", "System malfunction");
    }

    public Iso8583Response processTransaction(Iso8583Request request) {
        long startTime = System.currentTimeMillis();
        String transactionRef = request.getTransactionRef();
        
        log.info("[{}] Processing transaction: MTI={}, Fields={}",
                transactionRef, request.getMti(), request.getFields().keySet());
        
        try {
            ISOMsg isoRequest = codec.jsonToIso(request);
            log.debug("[{}] Converted to ISO8583 message", transactionRef);
            
            ISOMsg isoResponse = channel.sendAndReceive(isoRequest);
            log.debug("[{}] Received ISO8583 response", transactionRef);
            
            Iso8583Response response = codec.isoToJson(isoResponse);
            
            long processingTime = System.currentTimeMillis() - startTime;
            String responseCode = response.getResponseCode();
            
            response.setTransactionRef(transactionRef);
            response.setTimestamp(LocalDateTime.now());
            response.setProcessingTimeMs(processingTime);
            response.setSuccess("00".equals(responseCode));
            response.setMessage(RESPONSE_MESSAGES.getOrDefault(responseCode, "Unknown response"));
            
            log.info("[{}] Transaction completed: ResponseCode={}, Message={}, Duration={}ms",
                    transactionRef, responseCode, response.getMessage(), processingTime);
            
            return response;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("[{}] Transaction failed after {}ms: {}",
                    transactionRef, processingTime, e.getMessage(), e);
            throw e;
        }
    }
}
