package com.atm.iso8583.simulator;

import com.atm.iso8583.iso.IsoField;
import com.atm.iso8583.iso.IsoMtiUtil;
import lombok.extern.slf4j.Slf4j;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;

@Slf4j
public class Iso8583MockSwitch implements Runnable {

    private static final int PORT = 9000;

    public static void main(String[] args) {
        new Thread(new Iso8583MockSwitch()).start();
    }

    @Override
    public void run() {
        try {
            GenericPackager packager = new GenericPackager(getClass().getClassLoader().getResourceAsStream("iso8583-packager.xml"));
            ServerChannel channel = new ASCIIChannel(packager);
            ISOServer server = new ISOServer(PORT, channel, null);

            server.addISORequestListener((source, m) -> {
                try {
                    log.info("Received message: MTI={}", m.getMTI());
                    ISOMsg response = (ISOMsg) m.clone();
                    response.setMTI(IsoMtiUtil.toResponseMti(m.getMTI()));
                    response.set(IsoField.RESPONSE_CODE.id(), "00");
                    source.send(response);
                    log.info("Sent response: MTI={}, ResponseCode=00", response.getMTI());
                } catch (Exception e) {
                    log.error("Error processing message", e);
                }
                return true;
            });

            new Thread(server).start();
            log.info("Mock Switch started on port {}", PORT);
        } catch (Exception e) {
            log.error("Failed to start Mock Switch", e);
        }
    }
}
