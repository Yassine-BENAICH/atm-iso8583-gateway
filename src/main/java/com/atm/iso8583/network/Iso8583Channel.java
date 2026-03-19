package com.atm.iso8583.network;

import com.atm.iso8583.config.Iso8583Config;
import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Component
public class Iso8583Channel {

    private static final Logger log = LoggerFactory.getLogger(Iso8583Channel.class);

    private final Iso8583Config config;

    public Iso8583Channel(Iso8583Config config) {
        this.config = config;
    }

    /**
     * Sends an ISO 8583 message to the switch and waits for the response.
     */
    public byte[] sendAndReceive(byte[] requestBytes) throws IOException {
        log.info("Connecting to switch {}:{}", config.getHost(), config.getPort());

        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(config.getHost(), config.getPort()),
                    config.getConnectTimeout());
            socket.setSoTimeout(config.getReadTimeout());

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // ── Write request with length header ──────────────────────────────
            writeMessage(out, requestBytes);
            log.debug("Sent {} bytes to switch", requestBytes.length);

            // ── Read response with length header ──────────────────────────────
            byte[] responseBytes = readMessage(in);
            log.debug("Received {} bytes from switch", responseBytes.length);

            return responseBytes;

        } catch (SocketTimeoutException e) {
            log.error("Timeout waiting for response from switch ({}ms)", config.getReadTimeout());
            throw new IOException("Switch response timeout after " + config.getReadTimeout() + "ms", e);
        } catch (IOException e) {
            log.error("I/O error communicating with switch: {}", e.getMessage());
            throw e;
        }
    }

    // ─── Framing helpers ────────────────────────────────────────────────────────

    private void writeMessage(DataOutputStream out, byte[] payload) throws IOException {
        int len = config.getHeaderLength();
        switch (len) {
            case 2 -> out.writeShort(payload.length);
            case 4 -> out.writeInt(payload.length);
            default -> {
                // Generic big-endian write for other header sizes
                byte[] header = new byte[len];
                int remaining = payload.length;
                for (int i = len - 1; i >= 0; i--) {
                    header[i] = (byte) (remaining & 0xFF);
                    remaining >>= 8;
                }
                out.write(header);
            }
        }
        out.write(payload);
        out.flush();
        log.trace("TX hex: {}", ISOUtil.hexString(payload));
    }

    /**
     * Reads a length-prefixed message from the input stream.
     */
    private byte[] readMessage(DataInputStream in) throws IOException {
        int len = config.getHeaderLength();
        int messageLength = switch (len) {
            case 2 -> in.readUnsignedShort();
            case 4 -> in.readInt();
            default -> {
                byte[] header = new byte[len];
                in.readFully(header);
                int value = 0;
                for (byte b : header) {
                    value = (value << 8) | (b & 0xFF);
                }
                yield value;
            }
        };

        if (messageLength <= 0 || messageLength > 65536) {
            throw new IOException("Invalid message length received: " + messageLength);
        }

        byte[] payload = new byte[messageLength];
        in.readFully(payload);
        log.trace("RX hex: {}", ISOUtil.hexString(payload));
        return payload;
    }
}
