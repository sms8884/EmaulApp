package com.jaha.server.emaul.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jaha.server.emaul.util.HtmlUtil;

/**
 * Created by doring on 15. 3. 9..
 */
@Entity
@Table(name = "board_post", indexes = {@Index(name = "idx_date_post", columnList = "regDate"), @Index(name = "idx_range_sido_post", columnList = "rangeSido"),
        @Index(name = "idx_range_sigungu_post", columnList = "rangeSigungu")})
public class BoardPost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @ManyToOne(cascade = {})
    @JoinColumn(name = "categoryId", referencedColumnName = "id")
    public BoardCategory category;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean rangeAll = false;

    @Column(length = 20, nullable = false)
    public String rangeSido;

    @Column(length = 20, nullable = false)
    public String rangeSigungu;

    @Column(length = 300)
    public String title;

    @Column(columnDefinition = "TEXT NULL DEFAULT NULL")
    public String content;

    @Column(nullable = false)
    public Date regDate;

    @ManyToOne(targetEntity = User.class, cascade = {})
    @JoinColumn(name = "userId", referencedColumnName = "id")
    public User user;

    public Integer imageCount;

    @Column(length = 200)
    public String file1;
    @Column(length = 200)
    public String file2;

    @Column(nullable = false, columnDefinition = "BIGINT(20) NOT NULL DEFAULT 1")
    public Long viewCount = 1l;

    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean blocked = false;

    @Column(nullable = false, columnDefinition = "BIGINT(20) NOT NULL DEFAULT 0")
    public Long commentCount = 0l;

    @ManyToMany
    @JoinTable(name = "board_post_has_tag", joinColumns = @JoinColumn(name = "postId"), inverseJoinColumns = @JoinColumn(name = "tag"))
    public List<BoardTag> tags;

    @Transient
    public List<Hashtag> hashtags;

    @Transient
    public Boolean isDeletable = false;

    @Column(nullable = false, columnDefinition = "BIGINT(20) NOT NULL DEFAULT 1")
    public Long countEmpathy = 1l;

    /** 뉴스구분 */
    public String newsType;

    /** 뉴스 카테고리 */
    public String newsCategory;

    /** 출력여부 */
    @JsonIgnore
    public String displayYn;

    /** 잠금화면출력여부 */
    @JsonIgnore
    public String slideYn;

    /** 카드뉴스 이미지 갯수 */
    @Transient
    public int cardImageCount;

    /** 카드뉴스 이미지 URL 목록 */
    @Transient
    public List<String> cardImageList;

    /* 게시판 상단 고정 컬럼 추가 */
    @Column(nullable = false, columnDefinition = "BIT(1) NOT NULL DEFAULT 0")
    public Boolean topFix = false;

    /** 공감확인여부 */
    @Transient
    public int empathyCheckYn = 0;

    @JsonIgnore
    @Column(nullable = true)
    public Long modId;

    @JsonIgnore
    @Column(nullable = true)
    public Date modDate;

    @JsonIgnore
    @Column(length = 30, nullable = true)
    public String reqIp;

    // @PrePersist
    // public void prePersist() {
    //
    // }

    @PostLoad
    public void postPersist() {
        // 게시판 카테고리 모드가 html이고 게시판 title이 비어있는 경우에만 게시판 내용을 변환하여 타이틀로 수정
        if ("html".equals(this.category.contentMode)) {
            if (StringUtils.isBlank(this.title)) {
                String tempContent = this.content;
                String tempTitle = HtmlUtil.removeTag(tempContent);

                if (tempTitle.length() > 300) {
                    this.title = tempTitle.substring(0, 300);
                } else {
                    this.title = tempTitle;
                }
            }

            // if (this.content.indexOf("<!DOCTYPE html>") > -1) {
            // this.content = this.content.replaceAll("(\r\n|\n)", StringUtils.EMPTY);
            // }
        }
    }

    public void delete(Long userId, String reqIp) {
        this.displayYn = "N";
        this.reqIp = reqIp;
        this.modId = userId;
        this.modDate = new Date();
    }

    // @PreUpdate
    // public void preUpdate() {
    //
    // }

    // @PreRemove
    // public void preRemove() {
    //
    // }

}
