/**
 *
 */
package com.jaha.server.emaul.v2.model.board;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonView;
import com.jaha.server.emaul.v2.model.board.json.CommentView;

/**
 * @author 전강욱(realsnake@jahasmart.com) <br />
 *         This Domain class mapped db-table called board_comment_reply
 */
@Alias(value = "BoardCommentReplyVo")
public class BoardCommentReplyVo implements Serializable {

    /** SID */
    private static final long serialVersionUID = 3125041959520343713L;

    /** 답글일련번호 */
    @JsonView({CommentView.Comment.class, CommentView.CommentList.class})
    private Long id;

    /** 댓글일련번호 */
    @JsonView({CommentView.Comment.class, CommentView.CommentList.class})
    private Long commentId;

    /** 답글내용 */
    @JsonView({CommentView.Comment.class, CommentView.CommentList.class})
    private String content;

    /** 차단여부 */
    private Boolean blocked;

    /** 노출여부 */
    private String displayYn;

    /** 요청IP */
    private String reqIp;

    /** 등록자아이디 */
    private Long userId;

    /** 등록일시 */
    @JsonView({CommentView.Comment.class, CommentView.CommentList.class})
    private Date regDate;

    /** 수정자아이디 */
    private Long modId;

    /** 수정일시 */
    private Date modDate;

    /** 테이블칼럼아님, 답글 등록자명 */
    private String fullName;

    /** 테이블칼럼아님, 답글 등록자 닉네임 */
    private String nickname;

    /** 테이블칼럼아님, 답글 등록자 동 */
    private String dong;

    /** 테이블칼럼아님, 답글 등록자 호 */
    private String ho;

    /** 테이블칼럼아님, 답글 등록자 이름/닉네임(호) */
    @JsonView({CommentView.CommentList.class})
    private String writerName;

    /** 테이블칼럼아님, 댓글 수정삭제 권한 */
    @JsonView({CommentView.CommentList.class})
    private Boolean isModifiable;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public String getDisplayYn() {
        return displayYn;
    }

    public void setDisplayYn(String displayYn) {
        this.displayYn = displayYn;
    }

    public String getReqIp() {
        return reqIp;
    }

    public void setReqIp(String reqIp) {
        this.reqIp = reqIp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public Long getModId() {
        return modId;
    }

    public void setModId(Long modId) {
        this.modId = modId;
    }

    public Date getModDate() {
        return modDate;
    }

    public void setModDate(Date modDate) {
        this.modDate = modDate;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDong() {
        return dong;
    }

    public void setDong(String dong) {
        this.dong = dong;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    /**
     * @return the isModifiable
     */
    public Boolean getIsModifiable() {
        return isModifiable;
    }

    /**
     * @param isModifiable the isModifiable to set
     */
    public void setIsModifiable(Boolean isModifiable) {
        this.isModifiable = isModifiable;
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
