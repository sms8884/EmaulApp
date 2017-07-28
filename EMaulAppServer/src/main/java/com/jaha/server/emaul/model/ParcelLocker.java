package com.jaha.server.emaul.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
 * @description 무인택배함
 */
@Entity
@Table(name = "parcel_locker")
public class ParcelLocker {

    /** 일련번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(targetEntity = Apt.class, cascade = {})
    @JoinColumn(name = "aptId", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Apt apt;

    /** 택배함UUID(무인택배함 단말기에서 서버로 통신할 때 사용) */
    @Column(length = 40, nullable = false)
    private String uuid;

    /** 인증키 */
    @Column(length = 300, nullable = false)
    private String authKey;

    /** 택배함명칭 */
    @Column(length = 100, nullable = false)
    private String name;

    /** 설치위치 */
    @Column(length = 100, nullable = true)
    private String location;

    /** 택배함수량 */
    @Column(nullable = true)
    private Integer count;

    /** 상태(active/unactive) */
    @Column(length = 10, nullable = false)
    private String status;

    /** 등록일시 */
    @Column(nullable = false)
    private Date regDate;

    /** 공개키 */
    @Column(length = 3000, nullable = true)
    private String publicKey;

    /** 개인키 */
    @Column(length = 3000, nullable = true)
    private String privateKey;

    // @PrePersist
    // public void prePersist() {
    //
    // }
    // @PostLoad
    // public void postPersist() {
    //
    // }
    // @PreUpdate
    // public void preUpdate() {
    //
    // }
    // @PreRemove
    // public void preRemove() {
    //
    // }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the authKey
     */
    public String getAuthKey() {
        return authKey;
    }

    /**
     * @param authKey the authKey to set
     */
    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the count
     */
    public Integer getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the regDate
     */
    public Date getRegDate() {
        return regDate;
    }

    /**
     * @param regDate the regDate to set
     */
    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    /**
     * @return the apt
     */
    public Apt getApt() {
        return apt;
    }

    /**
     * @param apt the apt to set
     */
    public void setApt(Apt apt) {
        this.apt = apt;
    }

    /**
     * @return the publicKey
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey the publicKey to set
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return the privateKey
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * @param privateKey the privateKey to set
     */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ParcelLocker [id=" + id + ", apt=" + apt + ", uuid=" + uuid + ", authKey=" + authKey + ", name=" + name + ", location=" + location + ", count=" + count + ", status=" + status
                + ", regDate=" + regDate + ", publicKey=" + publicKey + ", privateKey=" + privateKey + "]";
    }

}
