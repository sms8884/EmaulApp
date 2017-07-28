package com.jaha.server.emaul.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Created by shavrani on 16-06-23
 */
@Entity
@Table(name = "apt_ap_access_log")
public class AptApAccessLog extends BaseSecuModel implements Serializable {

    @Transient
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public String apId;

    public Long userId;

    public Date accessDate;

    public String mobileDeviceModel;

    public String mobileDeviceOs;

    public String appVersion;

    public String openType;

    public long delayTime;

    public String success;

    public String expIp;

    public String memo;

    public Long accessDeviceId;

    public String waitingYn;

    public String inOut;


    @PrePersist
    public void prePersist() {}

    @PreUpdate
    public void preUpdate() {}

    @PreRemove
    public void preRemove() {}

}
