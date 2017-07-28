package com.jaha.server.emaul.common.exception;

import com.jaha.server.emaul.common.code.ErrorCode;

public class EmaulException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String code = null;
    private String message = null;
    private Object object = null;

    EmaulException() {
        super();
    }

    public EmaulException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public EmaulException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public EmaulException(ErrorCode errorCode, Object object) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.object = object;
    }

    public EmaulException(String code, String message, Object object) {
        this.code = code;
        this.message = message;
        this.object = object;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public Object getObject() {
        return object;
    }
}
