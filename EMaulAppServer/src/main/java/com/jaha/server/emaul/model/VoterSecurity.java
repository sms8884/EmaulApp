package com.jaha.server.emaul.model;

import com.jaha.server.emaul.util.Util;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "voter_security")
public class VoterSecurity {
    @Id
    @Column
    public String viId;

    @Column
    public Long voteId;

    @Column
    public String itemIdEnc="";

    @Column
    public Long itemId;

    @Column
    public String itemIdChkFname="";

    @Column
    public String regDt= Util.getDateString();

}

