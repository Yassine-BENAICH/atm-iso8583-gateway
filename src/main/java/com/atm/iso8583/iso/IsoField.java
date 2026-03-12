package com.atm.iso8583.iso;

/**
 * Minimal set of ISO 8583 field identifiers used by this project.
 * Add more fields here as you expand the packager definition.
 */
public enum IsoField {
    MTI(0),
    PRIMARY_BITMAP(1),
    PAN(2),
    PROCESSING_CODE(3),
    AMOUNT_TRANSACTION(4),
    TRANSMISSION_DATETIME(7),
    STAN(11),
    LOCAL_TIME(12),
    LOCAL_DATE(13),
    RETRIEVAL_REFERENCE_NUMBER(37),
    RESPONSE_CODE(39),
    TERMINAL_ID(41),
    CURRENCY_CODE(49);

    private final int id;

    IsoField(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}

