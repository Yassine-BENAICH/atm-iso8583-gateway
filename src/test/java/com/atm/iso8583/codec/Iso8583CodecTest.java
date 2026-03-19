package com.atm.iso8583.codec;

import com.atm.iso8583.model.Iso8583Request;
import com.atm.iso8583.model.Iso8583Response;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Iso8583CodecTest {

    private Iso8583Codec codec;
    private GenericPackager packager;

    @BeforeEach
    void setUp() throws Exception {
        codec = new Iso8583Codec();
        // Load the local custom packager from resources
        java.io.InputStream is = getClass().getClassLoader()
                .getResourceAsStream("packager/custom_iso87.xml");
        if (is == null) {
            throw new org.jpos.iso.ISOException("Cannot find packager/custom_iso87.xml");
        }
        packager = new GenericPackager(is);
    }

    // ─── toISOMsg ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toISOMsg: standard auth request fields are mapped correctly")
    void toISOMsg_authRequest() throws Exception {
        Iso8583Request req = Iso8583Request.builder()
                .mti("0100")
                .pan("4111111111111111")
                .processingCode("000000")
                .amount("000000010000")
                .stan("000001")
                .terminalId("TERM0001")
                .merchantId("MERCHANT000001 ")
                .currencyCode("978")
                .build();

        ISOMsg msg = codec.toISOMsg(req);
        msg.setPackager(packager);

        assertThat(msg.getMTI()).isEqualTo("0100");
        assertThat(msg.getString(2)).isEqualTo("4111111111111111");
        assertThat(msg.getString(3)).isEqualTo("000000");
        assertThat(msg.getString(4)).isEqualTo("000000010000");
        assertThat(msg.getString(11)).isEqualTo("000001");
        assertThat(msg.getString(41)).isEqualTo("TERM0001");
        assertThat(msg.getString(49)).isEqualTo("978");
    }

    @Test
    @DisplayName("toISOMsg: null fields are not set in ISOMsg")
    void toISOMsg_nullFieldsNotSet() throws Exception {
        Iso8583Request req = Iso8583Request.builder()
                .mti("0800")
                .stan("000000")
                .networkManagementCode("301")
                .build();

        ISOMsg msg = codec.toISOMsg(req);
        assertThat(msg.hasField(2)).isFalse();
        assertThat(msg.hasField(70)).isTrue();
        assertThat(msg.getString(70)).isEqualTo("301");
    }

    // ─── fromISOMsg ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("fromISOMsg: standard response fields are mapped correctly")
    void fromISOMsg_standardResponse() throws Exception {
        ISOMsg msg = new ISOMsg();
        msg.setPackager(packager);
        msg.setMTI("0110");
        msg.set(3, "000000");
        msg.set(4, "000000010000");
        msg.set(11, "000001");
        msg.set(37, "123456789012");
        msg.set(38, "AUTH01");
        msg.set(39, "00");
        msg.set(41, "TERM0001");
        msg.set(49, "978");

        Iso8583Response response = codec.fromISOMsg(msg);

        assertThat(response.getMti()).isEqualTo("0110");
        assertThat(response.getResponseCode()).isEqualTo("00");
        assertThat(response.getAuthorizationCode()).isEqualTo("AUTH01");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getResponseDescription()).isEqualTo("Approved");
        assertThat(response.getStan()).isEqualTo("000001");
    }

    @Test
    @DisplayName("fromISOMsg: declined response sets status to DECLINED")
    void fromISOMsg_declined() throws Exception {
        ISOMsg msg = new ISOMsg();
        msg.setPackager(packager);
        msg.setMTI("0110");
        msg.set(39, "51");

        Iso8583Response response = codec.fromISOMsg(msg);

        assertThat(response.getStatus()).isEqualTo("DECLINED");
        assertThat(response.getResponseDescription()).isEqualTo("Insufficient funds");
    }

    @Test
    @DisplayName("fromISOMsg: unknown response code returns fallback description")
    void fromISOMsg_unknownCode() throws Exception {
        ISOMsg msg = new ISOMsg();
        msg.setPackager(packager);
        msg.setMTI("0110");
        msg.set(39, "ZZ");

        Iso8583Response response = codec.fromISOMsg(msg);
        assertThat(response.getResponseDescription()).startsWith("Unknown response code");
    }

    // ─── Round-trip ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Round-trip: toISOMsg → pack → unpack → fromISOMsg preserves key fields")
    void roundTrip() throws Exception {
        Iso8583Request req = Iso8583Request.builder()
                .mti("0200")
                .pan("5500000000000004")
                .processingCode("010000")
                .amount("000000020000")
                .stan("000099")
                .terminalId("TERM0042")
                .merchantId("MERCHANT000001 ")
                .currencyCode("840")
                .build();

        ISOMsg packMsg = codec.toISOMsg(req);
        packMsg.setPackager(packager);
        byte[] bytes = packMsg.pack();

        ISOMsg unpackMsg = new ISOMsg();
        unpackMsg.setPackager(packager);
        unpackMsg.unpack(bytes);
        // Simulate a response by adding response-only fields
        unpackMsg.setMTI("0210");
        unpackMsg.set(38, "AUTHXX");
        unpackMsg.set(39, "00");

        Iso8583Response resp = codec.fromISOMsg(unpackMsg);
        assertThat(resp.getProcessingCode()).isEqualTo("010000");
        assertThat(resp.getStan()).isEqualTo("000099");
        assertThat(resp.getStatus()).isEqualTo("SUCCESS");
    }
}
