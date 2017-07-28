package com.jaha.server.emaul.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Weather implements Serializable {

    /** SID */
    private static final long serialVersionUID = -6355590660321814087L;

    /** 날씨 아이콘 상태 */
    private String status = "00";

    /** 현재기온 */
    private float current = -50.0f;

    /** 오늘 최저 기온 */
    private float todayMin = -50.0f;

    /** 오늘 최고 기온 */
    private float todayMax = -50.0f;

    /** 내일 오전 기온 */
    private float nextMin = -50.0f;

    /** 내일 오후 기온 */
    private float nextMax = -50.0f;

    /** 전날 대비 기온 차이 */
    private String diff;

    /** 기준일자 */
    private String baseDate;

    /** 기준시간 */
    private String baseTime;

    /** 강우량 */
    private int rainfall;

    /** 적설량 */
    private int snowfall;

    /** 하늘상태(SKY) 코드 */
    private int sky;

    /** 강수형태(PTY) 코드 */
    private int pty;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    public float getTodayMin() {
        return todayMin;
    }

    public void setTodayMin(float todayMin) {
        this.todayMin = todayMin;
    }

    public float getTodayMax() {
        return todayMax;
    }

    public void setTodayMax(float todayMax) {
        this.todayMax = todayMax;
    }

    public float getNextMin() {
        return nextMin;
    }

    public void setNextMin(float nextMin) {
        this.nextMin = nextMin;
    }

    public float getNextMax() {
        return nextMax;
    }

    public void setNextMax(float nextMax) {
        this.nextMax = nextMax;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public String getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(String baseDate) {
        this.baseDate = baseDate;
    }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public int getRainfall() {
        return rainfall;
    }

    public void setRainfall(int rainfall) {
        this.rainfall = rainfall;
    }

    public int getSnowfall() {
        return snowfall;
    }

    public void setSnowfall(int snowfall) {
        this.snowfall = snowfall;
    }

    public int getSky() {
        return sky;
    }

    public void setSky(int sky) {
        this.sky = sky;
    }

    public int getPty() {
        return pty;
    }

    public void setPty(int pty) {
        this.pty = pty;
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
