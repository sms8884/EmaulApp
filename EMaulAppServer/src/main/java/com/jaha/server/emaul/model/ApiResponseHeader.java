package com.jaha.server.emaul.model;

import java.io.Serializable;

import com.jaha.server.emaul.v2.constants.CommonConstants.ResponseCode;

public class ApiResponseHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 응답 코드 "00" : 요청 성공, 이외의 경우 오류상태
     */
    private String resultCode = "00";

    /**
     * 응답 메시지
     */
    private String resultMessage = "SUCCESS";

    public ApiResponseHeader() {

    }

    public ApiResponseHeader(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public ApiResponseHeader(ResponseCode responseCode) {
        this.resultCode = responseCode.getCode();
        this.resultMessage = responseCode.getMessage();
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

}
