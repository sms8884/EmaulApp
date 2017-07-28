package com.jaha.server.emaul.common.code;

public enum ParcelCode {

    /* @formatter:off */
    RESP_NOTEXISTS("01", "NOTEXISTS"), RESP_AUTHFAILS("02", "AUTHFAILS"), RESP_DECRYPTIONFAILS("03", "DECRYPTIONFAILS"), RESP_SUCCESS("00", "SUCCESS"), RESP_RUNTIMEFAILS("99", "RUNTIMEFAILS");

    /* @formatter:on */
    ParcelCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private final String code;

    private final String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("Code:%s, Message:%s", getCode(), getMessage());
    }

}
