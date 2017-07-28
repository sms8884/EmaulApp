package com.jaha.server.emaul.service;


import java.util.List;
import java.util.Map;

import com.jaha.server.emaul.constants.TodaySort;
import com.jaha.server.emaul.model.BoardCategory;
import com.jaha.server.emaul.model.BoardComment;
import com.jaha.server.emaul.model.BoardCommentReply;
import com.jaha.server.emaul.model.BoardPost;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.model.Hashtag;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.util.ScrollPage;

/**
 * Created by doring on 15. 3. 9..
 */
public interface BoardService {

    BoardPost save(BoardPost post);

    BoardComment save(BoardComment comment);

    BoardCommentReply save(BoardCommentReply reply);

    BoardPost get(User user, long postId);

    BoardComment getComment(User user, long commentId);

    BoardCommentReply getCommentReply(User user, long replyId);

    BoardCategory getCategory(Long id);

    List<String> getCategoryType(String postId);

    List<BoardCategory> getCategories(Long userId, String type);

    ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String sido, String sigungu, Long lastPostId, int count);

    ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count);

    ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn);

    ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn,
            String hashtag);

    ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn,
            String hashtag, TodaySort todaySort);

    List<BoardPost> getFirst16Posts(Long userId);

    ScrollPage<BoardComment> getComments(Long userId, Long postId, Long lastCommentId);

    ScrollPage<BoardCommentReply> getCommentReplies(Long userId, Long commentId, Long lastCommentReplyId);

    void increasePostViewCount(Long postId);

    Boolean isAlreadyEmpathy(Long userId, Long postId);

    void setEmpathy(Long userId, Long postId, boolean isEmpathy);

    void handleBlockPost(Long postId, boolean block);

    void handleBlockComment(Long commentId, boolean block);

    void handleBlockCommentReply(Long replyId, boolean block);

    /**
     * 뉴스카테고리 조회
     *
     * @return
     */
    List<CommonCode> getNewsCategories();

    // 마을뉴스 검색
    ScrollPage<BoardPost> getTodayPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String keyword,
            String slideYn, String hashtag, TodaySort todaySort);

    List<Hashtag> fetchHashtags(Long postId);

    // [START] 단체관리자 기능 추가 : 뉴스와 게시판 메소드 분리 by PNS 2016.09.30
    ScrollPage<BoardPost> getNewsPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn,
            String hashtag, TodaySort todaySort);
    // [END]


    /**
     * App 메인 알람영역 리스트용
     *
     * @param categoryId
     * @param top_fix
     * @return
     */
    List<Map<String, Object>> getAlarmPosts(List<BoardCategory> categoryList, String type, String topFix);

    /**
     * @param type
     * @return
     */
    List<BoardCategory> getCategories(String type);

    /**
     * @param userId
     * @param commentId
     * @param reqIp
     * @return
     */
    Boolean deleteComment(Long userId, Long commentId, String reqIp);

    /**
     * @param userId
     * @param postId
     * @param reqIp
     * @return
     */
    Boolean deletePost(Long userId, Long postId, String reqIp);

    /**
     * @param userId
     * @param replyId
     * @param reqIp
     * @return
     */
    Boolean deleteCommentReply(Long userId, Long replyId, String reqIp);

}
