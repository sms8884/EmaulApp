/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 9. 22.
 */
package com.jaha.server.emaul.v2.model.common;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonView;
import com.jaha.server.emaul.v2.model.board.json.CommentView;
import com.jaha.server.emaul.v2.model.board.json.PostView;

/**
 * @author 전강욱(realsnake@jahasmart.com)
 * @설명 : 페이징 공통 VO
 */
public class Paging implements Serializable {

    /** SID */
    private static final long serialVersionUID = -6569747806283501636L;

    /** 마지막 게시글 수 */
    @JsonView({PostView.FaqList.class, PostView.SystemNoticeList.class, PostView.GroupList.class, PostView.EventList.class, CommentView.CommentList.class})
    private Long nextPageToken;
    /** 한 페이지 당 게시물 개수 */
    @JsonView({PostView.FaqList.class, PostView.SystemNoticeList.class, PostView.GroupList.class, PostView.EventList.class, CommentView.CommentList.class})
    private int pageSize = 20;
    /** 전체 게시글 수 */
    @JsonView({PostView.FaqList.class, PostView.SystemNoticeList.class, PostView.GroupList.class, PostView.EventList.class, CommentView.CommentList.class})
    private long totalCount;

    public Long getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(Long nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
