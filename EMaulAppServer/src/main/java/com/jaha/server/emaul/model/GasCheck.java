package com.jaha.server.emaul.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by doring on 15. 4. 6..
 */
@Entity
@Table(name = "gas_check", indexes = {
        @Index(name = "idx_date_gas_check", columnList = "date")
})
public class GasCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public Date date;

    @Column(length = 100)
    public String imageUri;

    @ManyToOne(targetEntity = User.class, cascade = {})
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @JsonIgnore
    public User user;
}
