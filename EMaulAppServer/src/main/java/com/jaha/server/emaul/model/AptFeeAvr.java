package com.jaha.server.emaul.model;

import javax.persistence.*;

/**
 * Created by doring on 15. 5. 4..
 */
@Entity
@Table(name = "apt_fee_avr", indexes = {
        @Index(name = "idx_apt_fee_avr_search", columnList = "aptId, date, houseSize")
})
public class AptFeeAvr {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(length = 20, nullable = false)
    public String houseSize;

    // yyyyMM
    @Column(length = 10, nullable = false)
    public String date;

    public Long aptId;

    @Column(length = 2000, nullable = false)
    public String json;
}
