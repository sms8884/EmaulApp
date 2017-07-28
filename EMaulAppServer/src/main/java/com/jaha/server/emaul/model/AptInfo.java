package com.jaha.server.emaul.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by doring on 15. 6. 7..
 */
@Entity
public class AptInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(length = 2000)
    public String introduce;

    @Column(length = 200)
    public String aptOfficePhoneNumber;

    @Column(length = 1000)
    public String trafficInfo;

    @Column(length = 1000)
    public String trafficBusInfo;

    @Column(length = 250)
    public String urlLogo;

    @Column(length = 250)
    public String urlAptPhoto;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean canAnonymousViewFee = false;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinColumn(name = "aptInfoId", referencedColumnName = "id")
    public List<AptContact> contacts;
}
