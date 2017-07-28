package com.jaha.server.emaul.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "pollution", indexes = {@Index(name = "idx_pollution_base_date", columnList = "baseDate")})
public class Pollution {

    /** 일련번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long seq;

    /** 기준일자 */
    @JsonIgnore
    private String baseDate;

    /** 통보시간 */
    private String dataTime;

    /** 통보코드 */
    private String informCode;

    /** 예보개황 */
    @JsonIgnore
    private String informOverall;

    /** 발생원인 */
    private String informCause;

    /** 예보등급 */
    private String informGrade;

    /** 예측통보시간 */
    private String informData;

    /** 등록일시 */
    @JsonIgnore
    private Date regDt;

    /** PUSH 발송 여부 */
    @JsonIgnore
    private String pushYn;

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public String getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(String baseDate) {
        this.baseDate = baseDate;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public String getInformCode() {
        return informCode;
    }

    public void setInformCode(String informCode) {
        this.informCode = informCode;
    }

    public String getInformOverall() {
        return informOverall;
    }

    public void setInformOverall(String informOverall) {
        this.informOverall = informOverall;
    }

    public String getInformCause() {
        return informCause;
    }

    public void setInformCause(String informCause) {
        this.informCause = informCause;
    }

    public String getInformGrade() {
        return informGrade;
    }

    public void setInformGrade(String informGrade) {
        this.informGrade = informGrade;
    }

    public String getInformData() {
        return informData;
    }

    public void setInformData(String informData) {
        this.informData = informData;
    }

    public Date getRegDt() {
        return regDt;
    }

    public void setRegDt(Date regDt) {
        this.regDt = regDt;
    }

    public String getPushYn() {
        return pushYn;
    }

    public void setPushYn(String pushYn) {
        this.pushYn = pushYn;
    }

}
