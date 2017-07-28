/**
 * Copyright (c) 2017 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2017. 1. 24.
 */
package com.jaha.server.emaul.v2.model.cache;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <pre>
 * Class Name : MetroNewsTodayCacheVo.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2017. 1. 24.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2017. 1. 24.
 * @version 1.0
 */
public class MetroNewsTodayCacheVo implements Serializable {

    /** SID */
    private static final long serialVersionUID = 2309698019910844872L;

    /** 일련번호(자동증가) */
    private Integer id;
    /** 메트로오늘의뉴스JSON데이타 */
    private String todayNewsJson;
    /** 등록일시 */
    private Date regDate;

    public Integer getId() {
        return id;
    }

    public String getTodayNewsJson() {
        return todayNewsJson;
    }

    public void setTodayNewsJson(String todayNewsJson) {
        this.todayNewsJson = todayNewsJson;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
