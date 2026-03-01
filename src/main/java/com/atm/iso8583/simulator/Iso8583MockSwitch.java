package com.atm.iso8583.simulator;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple mock ISO 8583 switch for testing.
 * Listens on port 9000, accepts connections, responds to messages.
 */
public class Iso8583MockSwitch {

    private final int port;
    private final GenericPackager packager;

    public static void main(String[] args) throws Exception {
        int port = 9000;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        new Iso8583MockSwitch(port).start();
    }

    public Iso8583MockSwitch(int port) throws ISOException {
        this.port = port;
        // Load the packager from the classpath
        // (src/main/resources/packager/custom_iso87.xml)
        InputStream is = getClass().getClassLoader().getResourceAsStream("packager/custom_iso87.xml");
        if (is == null) {
            System.err.println("CRITICAL: Cannot find 'packager/custom_iso87.xml' in resources!");
            System.exit(1);
        }
        this.packager = new GenericPackager(is);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("MOCK SWITCH: Started and listening on port " + port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("MOCK SWITCH: New connection from " + clientSocket.getRemoteSocketAddress());
                    handleClient(clientSocket);
                } catch (Exception e) {
                    System.err.println("MOCK SWITCH: Error handling client: " + e.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket socket) {
        new Thread(() -> {
            try (DataInputStream in = new DataInputStream(socket.getInputStream());
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                // Read 4-byte length header
                int length = in.readInt();
                if (length <= 0)
                    return;

                byte[] requestBytes = new byte[length];
                in.readFully(requestBytes);
                System.out.println("MOCK SWITCH: Received " + length + " bytes");

                ISOMsg req = new ISOMsg();
                req.setPackager(packager);
                req.unpack(requestBytes);

                String mti = req.getMTI();
                System.out.println("MOCK SWITCH: Request MTI=" + mti + ", STAN=" + req.getString(11));

                // Prepare response
                ISOMsg resp = (ISOMsg) req.clone();
                resp.setResponseMTI();
                resp.set(39, "00"); // Approved

                // Additional logic per MTI
                if ("0800".equals(mti)) {
                    // Echo response (0810)
                } else if ("0100".equals(mti)) {
                    // Auth response (0110)
                    resp.set(38, "AUTH01"); // Auth Code
                } else if ("0200".equals(mti)) {
                    // Auth response (0210)
                    resp.set(38, "AUTH02"); // Auth Code
                }

                byte[] responseBytes = resp.pack();

                // Write 4-byte length header and payload
                out.writeInt(responseBytes.length);
                out.write(responseBytes);
                out.flush();
                System.out.println(
                        "MOCK SWITCH: Sent response MTI=" + resp.getMTI() + " (" + responseBytes.length + " bytes)");

            } catch (EOFException e) {
                System.out.println("MOCK SWITCH: Connection closed by client");
            } catch (Exception e) {
                System.err.println("MOCK SWITCH: Packet handling error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }).start();
    }
}
