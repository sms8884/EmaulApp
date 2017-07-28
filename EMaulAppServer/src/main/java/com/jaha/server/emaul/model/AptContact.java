package com.jaha.server.emaul.model;

import javax.persistence.*;

/**
 * Created by doring on 15. 6. 7..
 */
@Entity
public class AptContact {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(length = 50, nullable = false)
    public String name;

    @Column(length = 30, nullable = false)
    public String phoneNumber;
}
