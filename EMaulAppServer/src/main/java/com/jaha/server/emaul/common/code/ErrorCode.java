package com.jaha.server.emaul.common.code;

public enum ErrorCode {

    /* @formatter:off */
    COMMON_SUCCESS("00", "SUCCESS"), COMMON_FAIL("99", "FAIL")

    , RIVER_LVL_ERROR_81("81", "서울 열린데이터 광장 API 통신 오류"), RIVER_LVL_ERROR_82("82", "하천 수위 현황 API 조회 오류"), RIVER_LVL_ERROR_83("83", "구(Gu)코드 오류")

    , WEATHER_ERROR_91("91", "공공데이터포탈 API 통신 오류"), WEATHER_ERROR_92("92", "날씨 API 조회 오류")

    ;
    /* @formatter:on */

    ErrorCode(String code, String message) {
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
