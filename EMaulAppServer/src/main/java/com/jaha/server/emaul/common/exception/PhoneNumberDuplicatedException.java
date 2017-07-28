package com.jaha.server.emaul.common.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhoneNumberDuplicatedException extends RuntimeException {

    /** SID */
    private static final long serialVersionUID = -5809921873051357274L;

    private final static Logger log = LoggerFactory.getLogger(PhoneNumberDuplicatedException.class);

    private String phoneNumber;
    private String message;

    public PhoneNumberDuplicatedException(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.message = String.format("이미 등록된 핸드폰번호 입니다. [%s]", phoneNumber);
        log.info("이미 등록된 핸드폰번호 입니다. [{}]", phoneNumber);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
