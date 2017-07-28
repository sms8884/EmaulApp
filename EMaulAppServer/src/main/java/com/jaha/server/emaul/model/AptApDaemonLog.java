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
@Table(name = "apt_ap_daemon_log")
public class AptApDaemonLog extends BaseSecuModel implements Serializable {

    @Transient
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public Long apId;

    public String code;

    public String message;

    public Date regDate;


    @PrePersist
    public void prePersist() {}

    @PreUpdate
    public void preUpdate() {}

    @PreRemove
    public void preRemove() {}

}
