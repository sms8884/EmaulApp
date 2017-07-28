/**
 *
 */
package com.jaha.server.emaul.v2.model.board;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;
import org.springframework.web.multipart.MultipartFile;

import com.jaha.server.emaul.v2.model.common.CommonDto;
import com.jaha.server.emaul.v2.model.group.GroupAdminVo;

/**
 * @author 전강욱(realsnake@jahasmart.com)
 */
@Alias(value = "BoardDto")
public class BoardDto extends CommonDto implements Serializable {

    /** SID */
    private static final long serialVersionUID = 5652974626189970920L;

    /** 게시판 카테고리 데이터 */
    private BoardCategoryVo boardCategory;

    /** 게시판 카테고리 유형 */
    private String categoryType;

    /** 게시판 카테고리 아이디들 */
    private List<Long> categoryIds;

    /** 게시판 카테고리 아이디 */
    private Long categoryId;

    /** 게시판 게시글 아이디 */
    private Long postId;

    /** 게시판 댓글 아이디 */
    private Long commentId;

    /** 게시판 답글 아이디 */
    private Long replyId;

    /** 사진이미지 파일들 */
    private MultipartFile[] imageFiles;

    /** 첨부파일들 */
    private MultipartFile[] attachFiles;

    /** 스크롤 페이지 형식일 경우 마지막 게시글 아이디 */
    private Long lastPostId;

    /** 스크롤 페이지 형식일 경우 마지막 댓글 아이디 */
    private Long lastCommentId;

    /** 서브 카테고리 */
    private String subCategory;

    /** 상단고정 여부 */
    private Boolean topFix;

    /** 노출여부 */
    private String displayYn;

    /** 노출 플랫폼 */
    private String displayPlatform;

    /** 게시글 목록 조회 */
    private List<?> boardPostList;

    /** 게시글 댓글 목록 조회 */
    private List<BoardCommentVo> boardCommentList;

    /** 게시글 댓글 */
    private BoardCommentVo boardCommentVo;

    /** 응답 JSON */
    private ApiPostResponse<?> apiResponse;

    /** 이벤트 게시판 ing / end */
    private String gubun;

    /** 단체 게시판 그룹 어드민 정보 */
    private GroupAdminVo groupAdmin;

    /** 단체게시판 지역검색 시 사용, 시도 */
    private String rangeSido;
    /** 단체게시판 지역검색 시 사용, 시군구 */
    private String rangeSigungu;
    /** 단체게시판 지역검색 시 사용, 동 */
    private String rangeDong;

    public BoardCategoryVo getBoardCategory() {
        return boardCategory;
    }

    public void setBoardCategory(BoardCategoryVo boardCategory) {
        this.boardCategory = boardCategory;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public MultipartFile[] getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(MultipartFile[] imageFiles) {
        this.imageFiles = imageFiles;
    }

    public MultipartFile[] getAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(MultipartFile[] attachFiles) {
        this.attachFiles = attachFiles;
    }

    public Long getLastPostId() {
        return lastPostId;
    }

    public void setLastPostId(Long lastPostId) {
        this.lastPostId = lastPostId;
    }

    public Long getLastCommentId() {
        return lastCommentId;
    }

    public void setLastCommentId(Long lastCommentId) {
        this.lastCommentId = lastCommentId;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public Boolean getTopFix() {
        return topFix;
    }

    public void setTopFix(Boolean topFix) {
        this.topFix = topFix;
    }

    public String getDisplayYn() {
        return displayYn;
    }

    public void setDisplayYn(String displayYn) {
        this.displayYn = displayYn;
    }

    public String getDisplayPlatform() {
        return displayPlatform;
    }

    public void setDisplayPlatform(String displayPlatform) {
        this.displayPlatform = displayPlatform;
    }

    public List<?> getBoardPostList() {
        return boardPostList;
    }

    public void setBoardPostList(List<?> boardPostList) {
        this.boardPostList = boardPostList;
    }

    public ApiPostResponse<?> getApiResponse() {
        return apiResponse;
    }

    public void setApiResponse(ApiPostResponse<?> apiResponse) {
        this.apiResponse = apiResponse;
    }

    public String getGubun() {
        return gubun;
    }

    public void setGubun(String gubun) {
        this.gubun = gubun;
    }

    public List<BoardCommentVo> getBoardCommentList() {
        return boardCommentList;
    }

    public void setBoardCommentList(List<BoardCommentVo> boardCommentList) {
        this.boardCommentList = boardCommentList;
    }

    public GroupAdminVo getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(GroupAdminVo groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public String getRangeSido() {
        return rangeSido;
    }

    public void setRangeSido(String rangeSido) {
        this.rangeSido = rangeSido;
    }

    public String getRangeSigungu() {
        return rangeSigungu;
    }

    public void setRangeSigungu(String rangeSigungu) {
        this.rangeSigungu = rangeSigungu;
    }

    public String getRangeDong() {
        return rangeDong;
    }

    public void setRangeDong(String rangeDong) {
        this.rangeDong = rangeDong;
    }

    /**
     * @return the boardCommentVo
     */
    public BoardCommentVo getBoardCommentVo() {
        return boardCommentVo;
    }

    /**
     * @param boardCommentVo the boardCommentVo to set
     */
    public void setBoardCommentVo(BoardCommentVo boardCommentVo) {
        this.boardCommentVo = boardCommentVo;
    }

    /**
     * @return the replyId
     */
    public Long getReplyId() {
        return replyId;
    }

    /**
     * @param replyId the replyId to set
     */
    public void setReplyId(Long replyId) {
        this.replyId = replyId;
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
