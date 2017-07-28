package com.jaha.server.emaul.model;

import javax.persistence.*;

/**
 * Created by doring on 15. 3. 18..
 */
@Entity
@Table(name = "vote_type")
public class VoteType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Column(length = 20, nullable = false)
    public String main;

    @Column(length = 20, nullable = false)
    public String sub;
}
