package com.jaha.server.emaul.model;

import java.io.Serializable;
import java.util.Date;

import com.jaha.server.emaul.util.StringUtil;

public class AppPageViewLog extends BaseSecuModel implements Serializable {

    private static final long serialVersionUID = 2559862489877629108L;

    public Long id;

    public String pageCode;

    public Long userId;

    private String userNm;

    public Date regDate;

    public String getUserNm() {
        return StringUtil.isBlank(userNm) ? userNm : descString(userNm);
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }

}
