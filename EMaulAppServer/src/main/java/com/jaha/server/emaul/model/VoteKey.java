package com.jaha.server.emaul.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jaha.server.emaul.config.TbDateSerializer;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "vote_key")
public class VoteKey {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long vkId;

    @Column
    public String keyName;

    @Column
    public Long aptId;

    @Column
    @JsonSerialize(using=TbDateSerializer.class)
    public Date startDt;

    @Column
    @JsonSerialize(using=TbDateSerializer.class)
    public Date endDt;

    @Column
    public String useYn;

    @Column
    public String adminName;

    @Column
    public String adminEmail;

    @Column
    public String keyVoteDec;

    @Column
    public String keyVoteEnc;

    @Column
    public String keyCheckDec;

    @Column
    public String keyCheckEnc;

    @Column
    public String keyBase1;

    @Column
    public String keyBase2;

    @Column
    public String keyBase3;

    @Column(name = "key_base1_uname")
    public String keyBase1Uname;

    @Column(name = "key_base2_uname")
    public String keyBase2Uname;

    @Column(name = "key_base3_uname")
    public String keyBase3Uname;

    @Column
    public String createSignFname;

    @Column
    public String grantSignFname;


    @Column
    public String checkSignFname;

    @Column
    public String keyGrantDec;

    @Column
    public String keyGrantYn;

    @Column
    public String regDt;


    @Column
    public String regTm;


    @Column
    @JsonSerialize(using=TbDateSerializer.class)
    public Date uptDt;

    @Column
    public String keyLevel;

    @Transient
    public int keyStatus;


    public final static int KEY_STATUS_WAIT = 1;
    public final static int KEY_STATUS_LINK = 2;
    public final static int KEY_STATUS_START = 3;
    public final static int KEY_STATUS_END = 4;
}