package com.jaha.server.emaul.model;

import javax.persistence.*;

/**
 * Created by doring on 15. 3. 31..
 */
@Entity
@Table(name = "house", indexes = {
        @Index(name = "idx_house_dong", columnList = "dong"),
        @Index(name = "idx_house_ho", columnList = "ho")
})
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    // apt
    @ManyToOne(targetEntity = Apt.class, cascade = {})
    @JoinColumn(name = "aptId", referencedColumnName = "id")
    public Apt apt;

    @Column(length = 20)
    public String dong;

    @Column(length = 20)
    public String ho;

    @Column(length = 20)
    public String sizePyung;

    @Column(length = 20)
    public String sizeMeter;
}
