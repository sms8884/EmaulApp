package com.jaha.server.emaul.model;

import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Lists;

/**
 * Created by doring on 15. 3. 9..
 */
@Entity
@Table(name = "user_type")
public class UserType {

    public UserType(Long userId) {
        this.userId = userId;
    }

    public UserType() {

    }

    @Id
    public Long userId;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean jaha = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean admin = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean user = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 1")
    public Boolean anonymous = true;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean gasChecker = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean parcelChecker = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean houseHost = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean buildingGuard = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean official = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean communityMaster = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean blocked = false;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean deactivated = false;

    // [START] 단체관리자 기능 추가 by PNS 2016.09.19
    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean groupAdmin = false;
    // [END]

    // 소유가 권한 추가 : 2016.11.16 cyt
    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean owner = false;


    @Transient
    public List<String> getTrueTypes() {
        List<String> ret = Lists.newArrayList();

        Field[] fields = UserType.class.getDeclaredFields();
        for (Field field : fields) {
            if ("userId".equals(field.getName())) {
                continue;
            }
            try {
                if ((Boolean) field.get(this)) {
                    ret.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }
}
