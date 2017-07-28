/**
 *
 */
package com.jaha.server.emaul.v2.model.board;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.v2.model.board.json.PostView;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////

/**
 * @author 전강욱(realsnake@jahasmart.com) <br />
 *         This Domain class mapped db-table called board_post
 */
@Alias(value = "BoardPostVo")
public class BoardPostVo implements Serializable {

    /** SID */
    private static final long serialVersionUID = 5420162070851614004L;

    /** 게시글일련번호 */
    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class})
    private Long id;

    /** 게시판분류일련번호 */
    @JsonIgnore
    private Long categoryId;

    /** 서브카테고리(현재는 FAQ에서만 사용) */
    @JsonView({PostView.Faq.class, PostView.FaqList.class})
    private String subCategory;

    /** 제목 */
    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class})
    private String title;

    /** 내용 */
    @JsonView({PostView.Faq.class, PostView.SystemNotice.class, PostView.Event.class, PostView.Group.class})
    private String content;

    /** 이미지수 */
    private Integer imageCount;

    /** 전체공개여부 */
    private Boolean rangeAll;

    /** 첨부파일1 */
    private String file1;

    /** 첨부파일2 */
    private String file2;

    /** 조회수 */
    @JsonView({PostView.Event.class, PostView.Group.class})
    private Long viewCount;

    /** 공감수 */
    @JsonView({PostView.Event.class, PostView.Group.class})
    private Long countEmpathy;

    /** 댓글수 */
    @JsonView({PostView.Event.class, PostView.Group.class})
    private Long commentCount;

    /** 댓글 표시 여부 */
    private String commentDisplayYn;

    /** 차단여부 */
    private Boolean blocked;

    /** 노출여부 */
    private String displayYn;

    /** 상단고정여부 */
    private Boolean topFix;

    /** 파일그룹일련번호 */
    private String fileGroupKey;

    /** 노출플랫폼(전체/web/ios/android) */
    private String displayPlatform;

    /** 푸시발송여부 */
    private String pushSendYn;

    /** 숨김(가리기) 여부 */
    private String blindYn;

    /** 에디터 이미지를 썸네일한 URL */
    private String firstEditorImageThumbUrl;

    /** 요청IP */
    private String reqIp;

    /** 등록자아이디 */
    private Long userId;

    /** 등록일시 */
    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class})
    private Date regDate;

    /** 수정자아이디 */
    private Long modId;

    /** 수정일시 */
    private Date modDate;

    /** 카테고리 명 */
    private String categoryName;

    /** 카테고리 타입 */
    private String categoryType;

    /** 테이블칼럼아님, 게시자 정보 */
    private String writerName;

    /** 테이블칼럼아님, 아파트명 */
    private String userAptName;

    /** 테이블칼럼아님, <body> 태그 안에 있는 게시글 내용 */
    private String contentOnlyBody;

    /** 테이블칼럼아님, 게시글 댓글 목록 */
    private List<BoardCommentVo> boardCommentList;

    /** 테이블칼럼아님, 첨부파일목록 */
    private List<FileInfo> fileInfoList;

    /** 테이블칼럼아님, JSON용 첨부파일목록 */
    @JsonView({PostView.Faq.class, PostView.Group.class, PostView.SystemNotice.class})
    private List<Map<String, String>> attachFile;

    /** 테이블칼럼아님, JSON용 공감체크여부 */
    @JsonView({PostView.Event.class, PostView.Group.class})
    private Boolean empathyCheckYn;

    /** 테이블칼럼아님, JSON용 썸네일URL */
    @JsonView({PostView.Event.class, PostView.EventList.class})
    private String thumbUrl;

    /**
     * @return the blindYn
     */
    public String getBlindYn() {
        return blindYn;
    }

    /**
     * @param blindYn the blindYn to set
     */
    public void setBlindYn(String blindYn) {
        this.blindYn = blindYn;
    }

    /**
     * @return the commentDisplayYn
     */
    public String getCommentDisplayYn() {
        return commentDisplayYn;
    }

    /**
     * @param commentDisplayYn the commentDisplayYn to set
     */
    public void setCommentDisplayYn(String commentDisplayYn) {
        this.commentDisplayYn = commentDisplayYn;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public void setImageCount(Integer imageCount) {
        this.imageCount = imageCount;
    }

    public Boolean getRangeAll() {
        return rangeAll;
    }

    public void setRangeAll(Boolean rangeAll) {
        this.rangeAll = rangeAll;
    }

    public String getFile1() {
        return file1;
    }

    public void setFile1(String file1) {
        this.file1 = file1;
    }

    public String getFile2() {
        return file2;
    }

    public void setFile2(String file2) {
        this.file2 = file2;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getCountEmpathy() {
        return countEmpathy;
    }

    public void setCountEmpathy(Long countEmpathy) {
        this.countEmpathy = countEmpathy;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
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

    public Boolean getTopFix() {
        return topFix;
    }

    public void setTopFix(Boolean topFix) {
        this.topFix = topFix;
    }

    public String getFileGroupKey() {
        return fileGroupKey;
    }

    public void setFileGroupKey(String fileGroupKey) {
        this.fileGroupKey = fileGroupKey;
    }

    public String getDisplayPlatform() {
        return displayPlatform;
    }

    public void setDisplayPlatform(String displayPlatform) {
        this.displayPlatform = displayPlatform;
    }

    public String getPushSendYn() {
        return pushSendYn;
    }

    public void setPushSendYn(String pushSendYn) {
        this.pushSendYn = pushSendYn;
    }

    public String getFirstEditorImageThumbUrl() {
        return firstEditorImageThumbUrl;
    }

    public void setFirstEditorImageThumbUrl(String firstEditorImageThumbUrl) {
        this.firstEditorImageThumbUrl = firstEditorImageThumbUrl;
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

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public String getUserAptName() {
        return userAptName;
    }

    public void setUserAptName(String userAptName) {
        this.userAptName = userAptName;
    }

    public String getContentOnlyBody() {
        return contentOnlyBody;
    }

    public void setContentOnlyBody(String contentOnlyBody) {
        this.contentOnlyBody = contentOnlyBody;
    }

    public List<BoardCommentVo> getBoardCommentList() {
        return boardCommentList;
    }

    public void setBoardCommentList(List<BoardCommentVo> boardCommentList) {
        this.boardCommentList = boardCommentList;
    }

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public void setFileInfoList(List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }

    public List<Map<String, String>> getAttachFile() {
        return attachFile;
    }

    public void setAttachFile(List<Map<String, String>> attachFile) {
        this.attachFile = attachFile;
    }

    public Boolean getEmpathyCheckYn() {
        return empathyCheckYn;
    }

    public void setEmpathyCheckYn(Boolean empathyCheckYn) {
        this.empathyCheckYn = empathyCheckYn;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
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
