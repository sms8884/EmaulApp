package com.jaha.server.emaul.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author shavrani
 * @since 2017. 2. 7.
 * @version 1.0
 */
public class AptApBrokenLog implements Serializable {

    private static final long serialVersionUID = -8514477761941325688L;

    private Long id;

    private Long apId;

    private String expIp;

    private String modem;

    private String firmwareVersion;

    private String appVersion;

    private Long regUser;

    private Date regDate;

    private String memo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApId() {
        return apId;
    }

    public void setApId(Long apId) {
        this.apId = apId;
    }

    public String getExpIp() {
        return expIp;
    }

    public void setExpIp(String expIp) {
        this.expIp = expIp;
    }

    public String getModem() {
        return modem;
    }

    public void setModem(String modem) {
        this.modem = modem;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Long getRegUser() {
        return regUser;
    }

    public void setRegUser(Long regUser) {
        this.regUser = regUser;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

}
