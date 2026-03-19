package com.atm.iso8583.iso;

public final class IsoMtiUtil {

    private IsoMtiUtil() {
    }

    public static String toResponseMti(String requestMti) {
        if (requestMti == null || requestMti.length() != 4) {
            throw new IllegalArgumentException("MTI must be a 4-character string");
        }
        return requestMti.substring(0, 2) + "10";
    }
}

