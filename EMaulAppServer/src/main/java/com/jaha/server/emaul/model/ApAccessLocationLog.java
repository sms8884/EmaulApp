package com.jaha.server.emaul.model;

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
 * Created by shavrani on 16-08-12
 */
@Entity
@Table(name = "ap_access_location_log")
public class ApAccessLocationLog extends BaseSecuModel {

    @Transient
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public Long apId;

    public Long userId;

    public Date accessDate;

    public String mobileDeviceModel;

    public String mobileDeviceOs;

    public Long accessDeviceId;

    public Date appear;

    public Date disAppear;

    @PrePersist
    public void prePersist() {
        accessDate = new Date();
    }

    @PreUpdate
    public void preUpdate() {}

    @PreRemove
    public void preRemove() {}

}
