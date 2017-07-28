package com.jaha.server.emaul.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by doring on 15. 3. 30..
 */
@Entity
@Table(name = "apt_fee", indexes = {
        @Index(name = "idx_apt_fee_search", columnList = "date, houseId")
})
public class AptFee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @ManyToOne(targetEntity = House.class, cascade = {})
    @JoinColumn(name = "houseId", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    public House house;

    @Column(length = 20, nullable = false)
    public String date; // yyyyMM

    @Column(length = 2000, nullable = false)
    public String json;
}
