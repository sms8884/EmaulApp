package com.jaha.server.emaul.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by doring on 15. 3. 9..
 */
@Entity
@Table(name = "voter", indexes = {
        @Index(name = "idx_date_voter", columnList = "voteDate"),
        @Index(name = "idx_apt_id_voter", columnList = "aptId"),
        @Index(name = "idx_dong_voter", columnList = "dong"),
        @Index(name = "idx_ho_voter", columnList = "ho")
})
public class Voter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false)
    public User user;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "voteId", referencedColumnName = "id", nullable = false)
    public Vote vote;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "voteItemId", referencedColumnName = "id")
    public VoteItem voteItem;

    @Column(length = 1000)
    public String userContent;

    public Long aptId;
    @Column(length = 20)
    public String dong;
    @Column(length = 20)
    public String ho;

    public Date voteDate;

    @Column(length = 250)
    public String signImageUri;

    @Column(nullable = false, length = 10, columnDefinition = "VARCHAR(10) NOT NULL DEFAULT 'mobile'")
    public String registerType = "mobile";

    @Column(length = 40)
    public String voterName;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean mandated = false;

    @Column(length = 40)
    public String mandateVoterName;
}
