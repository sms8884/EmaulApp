package com.jaha.server.emaul.model;

import javax.persistence.*;

/**
 * Created by doring on 15. 3. 5..
 */
@Entity
@Table(name = "vote_item")
public class VoteItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public Long parentId;

    @Column(length = 200)
    public String title;

    @Column(length = 1000)
    public String commitment;

    @Column(length = 1000)
    public String profile;

    public Boolean isSubjective;

    public Integer imageCount;
}
