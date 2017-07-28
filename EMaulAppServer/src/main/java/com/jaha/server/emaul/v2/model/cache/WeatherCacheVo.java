/**
 * Copyright (c) 2017 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2017. 1. 31.
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
 * Class Name : WeatherCacheVo.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2017. 1. 31.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2017. 1. 31.
 * @version 1.0
 */
public class WeatherCacheVo implements Serializable {

    /** SID */
    private static final long serialVersionUID = -7341175223728635542L;

    /** 일련번호 */
    private Integer id;
    /** 위도 */
    private Double lat;
    /** 경도 */
    private Double lng;
    /** 주소(최소 구단위) */
    private String addr;
    /** 동네날씨/생활기상지수 객체를 BASE64 인코딩한 값 */
    private String weatherObj;
    /** 등록일시 */
    private Date regDate;


    public Integer getId() {
        return id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getWeatherObj() {
        return weatherObj;
    }

    public void setWeatherObj(String weatherObj) {
        this.weatherObj = weatherObj;
    }

    public Date getRegDate() {
        return regDate;
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
