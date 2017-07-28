package com.jaha.server.emaul.model;

import java.io.Serializable;
import java.util.Date;

import com.jaha.server.emaul.util.StringUtil;

public class UserLoginLog extends BaseSecuModel implements Serializable {

    private static final long serialVersionUID = 1352759087210957071L;

    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";

    public Long id;

    public String type;

    public Long userId;

    private String userNm;

    public String maker;

    public String model;

    public String appVersion;

    public Date regDate;

    public String getUserNm() {
        return StringUtil.isBlank(userNm) ? userNm : descString(userNm);
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }

}
