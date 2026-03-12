package com.atm.iso8583.codec;

import com.atm.iso8583.exception.Iso8583Exception;
import com.atm.iso8583.iso.IsoField;
import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class Iso8583Codec {

    private final GenericPackager packager;

    public Iso8583Codec() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            this.packager = new GenericPackager(classLoader.getResourceAsStream("iso8583-packager.xml"));
        } catch (ISOException e) {
            throw new Iso8583Exception("Failed to initialize ISO8583 packager", e);
        }
    }

    public ISOMsg jsonToIso(Iso8583Request request) {
        try {
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.setMTI(request.getMti());

            if (request.getFields() != null) {
                for (Map.Entry<String, String> entry : request.getFields().entrySet()) {
                    int fieldId = Integer.parseInt(entry.getKey());
                    isoMsg.set(fieldId, entry.getValue());
                }
            }

            log.debug("Converted JSON to ISO8583: MTI={}", request.getMti());
            return isoMsg;
        } catch (ISOException e) {
            throw new Iso8583Exception("Failed to convert JSON to ISO8583", e);
        }
    }

    public Iso8583Response isoToJson(ISOMsg isoMsg) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (int i = 0; i <= isoMsg.getMaxField(); i++) {
                if (isoMsg.hasField(i)) {
                    fields.put(String.valueOf(i), isoMsg.getString(i));
                }
            }
            
            String responseCode = isoMsg.hasField(39) ? isoMsg.getString(39) : null;
            
            Iso8583Response response = Iso8583Response.builder()
                    .mti(isoMsg.getMTI())
                    .fields(fields)
                    .responseCode(responseCode)
                    .build();

            log.debug("Converted ISO8583 to JSON: MTI={}, ResponseCode={}", isoMsg.getMTI(), responseCode);
            return response;
        } catch (ISOException e) {
            throw new Iso8583Exception("Failed to convert ISO8583 to JSON", e);
        }
    }
}
