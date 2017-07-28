package com.jaha.server.emaul.model;

import javax.persistence.*;

/**
 * Created by doring on 15. 4. 2..
 */
@Entity
@Table(name = "app_version")
public class AppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(length = 15)
    public String kind;

    public Integer versionCode;
    public Integer forceVersionCode;

    @Column(length = 10)
    public String versionName;
}
