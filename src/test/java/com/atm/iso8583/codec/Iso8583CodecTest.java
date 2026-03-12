package com.atm.iso8583.codec;

import com.atm.iso8583.iso.IsoField;
import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Iso8583CodecTest {

    private final Iso8583Codec codec = new Iso8583Codec();

    @Test
    void testJsonToIso() {
        try {
            Iso8583Request request = new Iso8583Request();
            request.setMti("0200");
            Map<String, String> fields = new HashMap<>();
            fields.put("2", "1234567890123456");
            fields.put("3", "000000");
            fields.put("4", "000000010000");
            request.setFields(fields);

            ISOMsg isoMsg = codec.jsonToIso(request);

            assertNotNull(isoMsg);
            assertEquals("0200", isoMsg.getMTI());
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }

    @Test
    void testIsoToJson() {
        try {
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setMTI("0210");
            isoMsg.set(IsoField.RESPONSE_CODE.id(), "00");

            Iso8583Response response = codec.isoToJson(isoMsg);

            assertNotNull(response);
            assertEquals("0210", response.getMti());
            assertEquals("00", response.getResponseCode());
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
}
