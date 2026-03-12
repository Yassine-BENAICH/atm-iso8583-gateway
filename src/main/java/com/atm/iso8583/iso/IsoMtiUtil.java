package com.atm.iso8583.iso;

public final class IsoMtiUtil {

    private IsoMtiUtil() {
    }

    /**
     * For the mock switch: turn a request MTI (e.g. 0200) into a response MTI (e.g. 0210).
     * This keeps the first two digits and forces the last two digits to "10".
     */
    public static String toResponseMti(String requestMti) {
        if (requestMti == null || requestMti.length() != 4) {
            throw new IllegalArgumentException("MTI must be a 4-character string");
        }
        return requestMti.substring(0, 2) + "10";
    }
}

