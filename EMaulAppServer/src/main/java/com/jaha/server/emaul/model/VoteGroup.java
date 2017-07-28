package com.jaha.server.emaul.model;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "vote_group", indexes = {@Index(name = "idx_target_apt_vote_group", columnList = "targetApt")})
public class VoteGroup {


    /** 선거구 일련번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    /** 선거구 설명 */
    @Column(length = 1000)
    public String description;

    /** 아파트 아이디 */
    @Column
    public Long targetApt;

    /** 선거구 명 */
    @Column
    public String name;
    /** 선거구 투표대상 */
    @Column
    @JsonIgnore
    public String jsonArrayTarget;
    /** 유권자 수 */
    @Column
    public Long votersCount; // 0l
    /** 세대 선택 방식 */
    @Column
    public String groupType;
    /** 등록자 아이디 */
    @Column
    public Long userId;
    /** 등록일시 */
    @Column
    public Date regDate;
    /** 수정자 아이디 */
    @Column
    public Long modId;
    /** 수정일시 */
    @Column
    public Date modDate;
    /** 사용여부 */
    @Column
    public String useYn;

}
