package com.atm.iso8583.channel;

import com.atm.iso8583.exception.Iso8583Exception;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class Iso8583Channel {

    @Value("${iso8583.switch.host}")
    private String host;

    @Value("${iso8583.switch.port}")
    private int port;

    @Value("${iso8583.switch.timeout}")
    private int timeout;

    public ISOMsg sendAndReceive(ISOMsg request) {
        ASCIIChannel channel = null;

        try {
            GenericPackager packager = new GenericPackager(getClass().getClassLoader().getResourceAsStream("iso8583-packager.xml"));
            channel = new ASCIIChannel(host, port, packager);
            channel.setTimeout(timeout);

            channel.connect();
            channel.send(request);
            log.info("Sent ISO8583 message to {}:{}", host, port);

            ISOMsg response = channel.receive();
            log.info("Received ISO8583 response from {}:{}", host, port);

            return response;
        } catch (IOException | ISOException e) {
            throw new Iso8583Exception("Failed to communicate with switch", e);
        } finally {
            if (channel != null) {
                try {
                    channel.disconnect();
                } catch (IOException e) {
                    log.warn("Error closing channel", e);
                }
            }
        }
    }
}
