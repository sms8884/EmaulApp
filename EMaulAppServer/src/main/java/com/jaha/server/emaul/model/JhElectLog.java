package com.jaha.server.emaul.model;

import com.jaha.server.emaul.util.Util;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Administrator on 2015-06-28.
 */

@Entity
@Table(name = "jh_elect_log")
public class JhElectLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    public Long id;

    @Column(name="adm_id",length = 45,nullable = false)
    public String admId="";

    @Column(name="ele_name",length = 45,nullable = false)
    public String eleName="";

    @Column(name="ele_tel",length = 45,nullable = false)
    public String eleTel="";


    @Column(name="sign_img_fname",length = 128,nullable = false)
    public String signImgFname="";

    @Column(name="reg_dt",length = 10,nullable = false)
    public String regDt= Util.getDateString();

    @Column(name="upt_dt")
    public Date uptDt;
}

