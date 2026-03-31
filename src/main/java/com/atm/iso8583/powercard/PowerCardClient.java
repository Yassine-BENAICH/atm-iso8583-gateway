package com.atm.iso8583.powercard;

import java.io.IOException;

public interface PowerCardClient {

    byte[] exchange(byte[] requestBytes) throws IOException;
}
