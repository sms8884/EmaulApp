/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 9. 22.
 */
package com.jaha.server.emaul.v2.service.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import com.google.common.collect.Lists;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.model.BaseSecuModel;
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.CommonService;
import com.jaha.server.emaul.service.UserService;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.v2.constants.BoardConstants;
import com.jaha.server.emaul.v2.constants.BoardConstants.BoardType;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAction;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAlarmSetting;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushGubun;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushMessage;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushStatus;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushTargetType;
import com.jaha.server.emaul.v2.mapper.board.BoardCategoryMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardCommentMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardCommentReplyMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardEmpathyMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostAirMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostEventMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostHashtagMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostMaulnewsMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostRangeMapper;
import com.jaha.server.emaul.v2.mapper.board.StatSharerMapper;
import com.jaha.server.emaul.v2.mapper.common.PushMapper;
import com.jaha.server.emaul.v2.mapper.group.GroupAdminMapper;
import com.jaha.server.emaul.v2.model.board.BoardCategoryVo;
import com.jaha.server.emaul.v2.model.board.BoardCommentReplyVo;
import com.jaha.server.emaul.v2.model.board.BoardCommentVo;
import com.jaha.server.emaul.v2.model.board.BoardDto;
import com.jaha.server.emaul.v2.model.board.BoardPostEventVo;
import com.jaha.server.emaul.v2.model.board.BoardPostRangeVo;
import com.jaha.server.emaul.v2.model.board.BoardPostVo;
import com.jaha.server.emaul.v2.model.board.BoardPostWrapper;
import com.jaha.server.emaul.v2.model.common.Sort;
import com.jaha.server.emaul.v2.model.group.GroupAdminVo;
import com.jaha.server.emaul.v2.util.PushUtils;

/**
 * <pre>
 * Class Name : BoardServiceImpl.java
 * Description : 게시판 서비스
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 9. 22.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 9. 22.
 * @version 1.0
 */
@Service("v2BoardService")
public class BoardServiceImpl implements BoardService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${file.path.board.attach}")
    private String filePathBoardAttach;

    @Value("${file.path.editor.image}")
    private String filePathEditorImage;

    @Value("${board.attach.download.url}")
    private String boardAttachDownloadUrl;

    @Autowired
    private BoardCategoryMapper boardCategoryMapper;

    @Autowired
    private BoardCommentMapper boardCommentMapper;

    @Autowired
    private BoardCommentReplyMapper boardCommentReplyMapper;

    @Autowired
    private BoardEmpathyMapper boardEmpathyMapper;

    @Autowired
    private BoardPostAirMapper boardPostAirMapper;

    @Autowired
    private BoardPostEventMapper boardPostEventMapper;

    @Autowired
    private BoardPostHashtagMapper boardPostHashtagMapper;

    @Autowired
    private BoardPostMapper boardPostMapper;

    @Autowired
    private BoardPostMaulnewsMapper boardPostMaulnewsMapper;

    @Autowired
    private BoardPostRangeMapper boardPostRangeMapper;

    @Autowired
    private StatSharerMapper statSharerMapper;

    @Autowired
    private BoardCategoryService boardCategoryService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private UserService userService;

    @Autowired
    private PushMapper pushMapper;

    @Autowired
    private GroupAdminMapper groupAdminMapper;

    @Autowired
    private PushUtils pushUtils;

    private final BaseSecuModel baseSecuModel = new BaseSecuModel();

    @Override
    @Transactional(readOnly = true)
    public List<? extends BoardPostVo> findBoardPostList(BoardDto param) throws Exception {
        // 게시판 카테고리 조회
        List<String> userAuthTypeList = param.getUser().type.getTrueTypes();
        BoardCategoryVo boardCategory = this.boardCategoryService.findBoardCategory(param.getCategoryId(), userAuthTypeList);
        param.setBoardCategory(boardCategory);

        Long lastPostId = (param.getLastPostId() == null || param.getLastPostId().equals(0L)) ? Long.MAX_VALUE : param.getLastPostId();
        param.setLastPostId(lastPostId);

        // 목록 레코드 수 조회
        param.getPagingHelper().setTotalCount(this.getTotalRecordCount(param));
        this.putSortList(param);

        // 목록 조회
        return this.getBoardPostList(param);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void regBoardPost(BoardDto param, BoardPostWrapper<?> boardPost) throws Exception {
        // 게시판 카테고리 조회
        List<String> userAuthTypeList = param.getUser().type.getTrueTypes();
        BoardCategoryVo boardCategory = this.boardCategoryService.findBoardCategory(param.getCategoryId(), userAuthTypeList);
        param.setBoardCategory(boardCategory);

        if (BoardConstants.ContentMode.HTML.getValue().equals(boardCategory.getContentMode())) {
            boardPost.get().setContent(String.format(BoardConstants.APP_HTML_FORMAT, boardPost.get().getContent()));
        }

        // 게시글 등록
        this.regPost(param, boardPost);

        // PUSH 발송(자하 공지사항인 경우, 관리자 공지사항인 경우, 민원 게시판인 경우, 커뮤니티 게시판이면서 글등록 후 push 발송여부가 Y인 경우)
        this.sendPushToAptJumin(param, boardPost);
    }

    @Override
    public BoardPostWrapper<?> findBoardPost(BoardDto param, Long postId, boolean isModifying) throws Exception {
        BoardPostVo post = this.boardPostMapper.selectBoardPost(postId);
        param.setCategoryId(post.getCategoryId()); // TODO: 없는 게시글에 대한 처리 필요

        // 게시판 카테고리 조회
        List<String> userAuthTypeList = param.getUser().type.getTrueTypes();
        BoardCategoryVo boardCategory = this.boardCategoryService.findBoardCategory(param.getCategoryId(), userAuthTypeList);
        param.setBoardCategory(boardCategory);

        // 게시글 조회
        BoardPostWrapper<?> boardPost = this.getBoardPost(param, postId);

        // 수정화면으로 이동 시에는 조회 수 증가 및 댓글 목록 수 조회를 하지 않는다.
        if (isModifying) {
            return boardPost;
        }

        // 조회수 업데이트
        this.boardPostMapper.updateViewCount(postId);

        return boardPost;
    }

    @Override
    public void modifyBoardPost(BoardDto param, BoardPostWrapper<?> boardPost) throws Exception {
        // 게시판 카테고리 조회
        List<String> userAuthTypeList = param.getUser().type.getTrueTypes();
        BoardCategoryVo boardCategory = this.boardCategoryService.findBoardCategory(param.getCategoryId(), userAuthTypeList);
        param.setBoardCategory(boardCategory);

        if (BoardConstants.ContentMode.HTML.getValue().equals(boardCategory.getContentMode())) {
            boardPost.get().setContent(String.format(BoardConstants.APP_HTML_FORMAT, boardPost.get().getContent()));
        }

        this.modifyPost(param, boardPost);

        // PUSH 발송(자하 공지사항인 경우, 관리자 공지사항인 경우, 민원 게시판인 경우, 커뮤니티 게시판이면서 글등록 후 push 발송여부가 Y인 경우)
        this.sendPushToAptJumin(param, boardPost);
    }

    @Override
    public void removeBoardPost(BoardDto param, BoardPostVo boardPost) throws Exception {
        // 게시판 카테고리 조회
        List<String> userAuthTypeList = param.getUser().type.getTrueTypes();
        BoardCategoryVo boardCategory = this.boardCategoryService.findBoardCategory(param.getCategoryId(), userAuthTypeList);
        param.setBoardCategory(boardCategory);

        boardPost.setDisplayYn("N");
        // 게시글 노출 여부
        this.boardPostMapper.updateDisplayYn(boardPost);
    }

    ////////////////////////////////////////////////////////////////////////////////// 공통 //////////////////////////////////////////////////////////////////////////////////
    /**
     * 게시글 총 레코드 수 반환<br />
     *
     * @param param
     * @return
     */
    private int getTotalRecordCount(BoardDto param) throws Exception {
        String categoryType = param.getBoardCategory().getType();
        int totalCount = 0;

        if (BoardType.NOTICE.getCode().equals(categoryType) || BoardType.COMMUNITY.getCode().equals(categoryType)) {
            totalCount = this.boardPostMapper.selectBoardPostListCount(param);
        } else if (BoardType.GROUP.getCode().equals(categoryType)) {
            totalCount = this.boardPostRangeMapper.selectBoardPostRangeListCount(param);
        } else if (BoardType.EVENT.getCode().equals(categoryType)) {
            totalCount = this.boardPostEventMapper.selectBoardPostEventListCount(param);
        } else {
            totalCount = this.boardPostMapper.selectBoardPostListCount(param);
        }

        return totalCount;
    }

    /**
     * 정렬 세팅(DB Query에 전달할 ORDER BY 데이터)<br />
     *
     * @param param
     * @return
     */
    private void putSortList(BoardDto param) throws Exception {
        String categoryType = param.getBoardCategory().getType();
        List<Sort> sortList = new ArrayList<Sort>();

        if (BoardType.NOTICE.getCode().equals(categoryType)) {
            Sort sort = new Sort();
            sort.setColumn("top_fix");
            sort.setAscOrDesc(BoardConstants.SortType.DESC.getValue());
            sortList.add(sort);

            sort = new Sort();
            sort.setColumn("id");
            sort.setAscOrDesc(BoardConstants.SortType.DESC.getValue());
            sortList.add(sort);

            param.getPagingHelper().setSortList(sortList);
        } else if (BoardType.GROUP.getCode().equals(categoryType) || BoardType.EVENT.getCode().equals(categoryType)) {
            if (param.getPagingHelper().getSortList() == null || param.getPagingHelper().getSortList().isEmpty()) {
                Sort sort = new Sort();
                sort.setColumn("BP.id");
                sort.setAscOrDesc(BoardConstants.SortType.DESC.getValue());
                sortList.add(sort);

                param.getPagingHelper().setSortList(sortList);
            }

            sortList = param.getPagingHelper().getSortList();
            // 게시글 등록순, 조회순, 댓글순(화면에서 전송한 파라미터는 PagingArgumentResolver에서 자동 처리(예: &sort=BP.reg_date&sort=BP.view_count,ASC)
        } else {
            if (param.getPagingHelper().getSortList() == null || param.getPagingHelper().getSortList().isEmpty()) {
                Sort sort = new Sort();
                sort.setColumn("id");
                sort.setAscOrDesc(BoardConstants.SortType.DESC.getValue());
                sortList.add(sort);
            }

            param.getPagingHelper().setSortList(sortList);
        }
    }

    /**
     * 게시글 목록 반환<br />
     *
     * @param param
     * @return
     * @throws Exception
     */
    private List<? extends BoardPostVo> getBoardPostList(BoardDto param) throws Exception {
        String categoryType = param.getBoardCategory().getType();
        List<? extends BoardPostVo> boardPostList = null;

        if (BoardType.NOTICE.getCode().equals(categoryType) || BoardType.COMMUNITY.getCode().equals(categoryType)) {
            boardPostList = this.boardPostMapper.selectBoardPostList(param);
        } else if (BoardType.GROUP.getCode().equals(categoryType)) {
            boardPostList = this.boardPostRangeMapper.selectBoardPostRangeList(param);

            // 로그인 사용자의 지역에 해당하는 그룹어드민 조회
            GroupAdminVo groupAdmin = this.getGroupAdmin(param.getUser().id);
            param.setGroupAdmin(groupAdmin);
        } else if (BoardType.EVENT.getCode().equals(categoryType)) {
            boardPostList = this.boardPostEventMapper.selectBoardPostEventList(param);
        } else if (BoardType.FAQ.getCode().equals(categoryType) || BoardType.SYSTEM_NOTICE.getCode().equals(categoryType)) {
            param.setDisplayPlatform(param.getUser().kind);
            boardPostList = this.boardPostMapper.selectBoardPostList(param);
        } else {
            boardPostList = this.boardPostMapper.selectBoardPostList(param);
        }

        Long nextPageToken = null;

        if (boardPostList != null && !boardPostList.isEmpty()) {
            int postListSize = boardPostList.size();

            if (postListSize >= param.getPagingHelper().getPageSize()) {
                BoardPostVo lastBoardPost = boardPostList.get(postListSize - 1);
                nextPageToken = lastBoardPost.getId();
            }

            if (BoardType.EVENT.getCode().equals(categoryType)) {
                for (BoardPostVo boardPost : boardPostList) {
                    List<FileInfo> fileInfoList = this.commonService.getFileGroup(categoryType, boardPost.getFileGroupKey());

                    if (fileInfoList != null && !fileInfoList.isEmpty()) {
                        for (FileInfo fileInfo : fileInfoList) {
                            String fileDownloadUrl = String.format(this.boardAttachDownloadUrl, boardPost.getId(), fileInfo.fileName);
                            boardPost.setThumbUrl(fileDownloadUrl);
                        }
                    }
                }
            }
        }

        param.getPagingHelper().setNextPageToken(nextPageToken);

        return boardPostList;
    }

    /**
     * 게시글 상세 반환<br />
     *
     * @param param
     * @return
     * @throws Exception
     */
    private BoardPostWrapper<?> getBoardPost(BoardDto param, Long postId) throws Exception {
        String categoryType = param.getBoardCategory().getType();

        // 사용자별 공감여부
        param.setUserId(param.getUser().id);
        param.setPostId(postId);
        Boolean empathyCheckYn = (this.boardEmpathyMapper.selectBoardEmpathyListCount(param) > 0) ? true : false;

        if (BoardType.NOTICE.getCode().equals(categoryType) || BoardType.COMMUNITY.getCode().equals(categoryType)) {
            BoardPostVo boardPost = this.boardPostMapper.selectBoardPost(postId);
            boardPost.setEmpathyCheckYn(empathyCheckYn);
            return new BoardPostWrapper<BoardPostVo>(boardPost);
        } else if (BoardType.GROUP.getCode().equals(categoryType)) {
            BoardPostRangeVo boardPost = this.boardPostRangeMapper.selectBoardPostRange(postId);
            boardPost.setEmpathyCheckYn(empathyCheckYn);

            if (boardPost != null) {
                List<FileInfo> fileInfoList = this.commonService.getFileGroup(categoryType, boardPost.getFileGroupKey());
                boardPost.setFileInfoList(fileInfoList);
            }

            // 로그인 사용자의 지역에 해당하는 그룹어드민 조회
            GroupAdminVo groupAdmin = this.getGroupAdmin(param.getUser().id);
            param.setGroupAdmin(groupAdmin);

            return new BoardPostWrapper<BoardPostRangeVo>(boardPost);
        } else if (BoardType.EVENT.getCode().equals(categoryType)) {
            BoardPostEventVo boardPost = this.boardPostEventMapper.selectBoardPostEvent(postId);
            boardPost.setEmpathyCheckYn(empathyCheckYn);

            if (boardPost != null) {
                List<FileInfo> fileInfoList = this.commonService.getFileGroup(categoryType, boardPost.getFileGroupKey());
                boardPost.setFileInfoList(fileInfoList);
            }

            return new BoardPostWrapper<BoardPostEventVo>(boardPost);
        } else if (BoardType.FAQ.getCode().equals(categoryType) || BoardType.SYSTEM_NOTICE.getCode().equals(categoryType)) {
            BoardPostVo boardPost = this.boardPostMapper.selectBoardPost(postId);
            boardPost.setEmpathyCheckYn(empathyCheckYn);

            if (boardPost != null) {
                List<FileInfo> fileInfoList = this.commonService.getFileGroup(categoryType, boardPost.getFileGroupKey());
                boardPost.setFileInfoList(fileInfoList);
            }

            return new BoardPostWrapper<BoardPostVo>(boardPost);
        } else {
            BoardPostVo boardPost = this.boardPostMapper.selectBoardPost(postId);
            boardPost.setEmpathyCheckYn(empathyCheckYn);
            return new BoardPostWrapper<BoardPostVo>(boardPost);
        }
    }

    /**
     * 게시글 등록
     *
     * @param boardPost
     */
    private void regPost(BoardDto param, BoardPostWrapper<?> boardPost) throws Exception {
        String categoryType = param.getBoardCategory().getType();

        if (BoardType.GROUP.getCode().equals(categoryType)) {
            BoardPostRangeVo boardPostRange = (BoardPostRangeVo) boardPost.get();
            boardPostRange.setImageCount(0);
            boardPostRange.setRangeAll(false);
            boardPostRange.setTopFix(false);

            if (PushStatus.INSTANT.getValue().equalsIgnoreCase(boardPostRange.getPushStatus())) {
                boardPostRange.setPushSendYn("Y");
            } else {
                boardPostRange.setPushSendYn("N");
            }

            this.boardPostMapper.insertBoardPost(boardPostRange);
            boardPostRange.setPostId(boardPostRange.getId());

            this.boardPostRangeMapper.insertBoardPostRange(boardPostRange);
        } else if (BoardType.EVENT.getCode().equals(categoryType)) {
            BoardPostEventVo boardPostEvent = (BoardPostEventVo) boardPost.get();
            boardPostEvent.setImageCount(0);
            boardPostEvent.setRangeAll(false); // 현재 이벤트 게시판은 전체 공개이나 일단 false
            boardPostEvent.setTopFix(false);

            if (PushStatus.INSTANT.getValue().equalsIgnoreCase(boardPostEvent.getPushStatus())) {
                boardPostEvent.setPushSendYn("Y");
            } else {
                boardPostEvent.setPushSendYn("N");
            }

            if (PushStatus.RESERV.getValue().equalsIgnoreCase(boardPostEvent.getPushStatus())) {
                boardPostEvent.setReservYn("Y");
                boardPostEvent.setOpenDate(boardPostEvent.getStartDate());
            }

            this.boardPostMapper.insertBoardPost(boardPostEvent);
            boardPostEvent.setPostId(boardPostEvent.getId());

            this.boardPostEventMapper.insertBoardPostEvent(boardPostEvent);
            this.boardPostRangeMapper.insertBoardPostRange(boardPostEvent);
        } else {
            this.boardPostMapper.insertBoardPost(boardPost.get());
        }
    }

    /**
     * 게시글 수정
     *
     * @param boardPost
     */
    private void modifyPost(BoardDto param, BoardPostWrapper<?> boardPost) throws Exception {
        String categoryType = param.getBoardCategory().getType();

        this.boardPostMapper.updateBoardPost(boardPost.get());

        if (BoardType.GROUP.getCode().equals(categoryType)) {
            this.boardPostRangeMapper.updateBoardPostRange((BoardPostRangeVo) boardPost.get());
        } else if (BoardType.EVENT.getCode().equals(categoryType)) {
            BoardPostEventVo boardPostEvent = (BoardPostEventVo) boardPost.get();

            if (PushStatus.RESERV.getValue().equalsIgnoreCase(boardPostEvent.getPushStatus())) {
                boardPostEvent.setReservYn("Y");
                boardPostEvent.setOpenDate(boardPostEvent.getStartDate());
            }

            this.boardPostEventMapper.updateBoardPostEvent(boardPostEvent);
            this.boardPostRangeMapper.updateBoardPostRange(boardPostEvent);
        }
    }

    /**
     * 게시판 분류 별로 (즉시) 푸시 발송(공지사항 등)
     *
     * @param param
     * @param boardPost
     * @throws Exception
     */
    private void sendPushToAptJumin(BoardDto param, BoardPostWrapper<?> boardPost) throws Exception {
        String categoryType = param.getBoardCategory().getType();

        if (BoardType.NOTICE.getCode().equals(categoryType)) {

        } else if (BoardType.GROUP.getCode().equals(categoryType)) { // 단체 게시판(단체관리자) 푸시 발송

        } else if (BoardType.EVENT.getCode().equals(categoryType)) { // 이벤트 게시판(자하권한) 푸시 발송

        } else {

        }
    }

    @Override
    @Transactional
    public void regBoardComment(BoardDto param, BoardCommentVo boardComment) throws Exception {

        BoardPostVo boardPost = this.boardPostMapper.selectBoardPost(param.getPostId());
        // 게시판 카테고리 조회
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(boardPost.getCategoryId());
        param.setBoardCategory(boardCategory);

        // 댓글 등록
        boardComment.setPostId(param.getPostId());
        this.boardCommentMapper.insertBoardComment(boardComment);

        // 게시글 등록자에게 푸시발송
        Long userId = boardComment.getUserId();

        if (!userId.equals(boardPost.getUserId())) {
            Long postId = boardPost.getId();
            String categoryType = boardCategory.getType();

            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetUserList(PushTargetType.USER, PushAlarmSetting.BOARD, Lists.newArrayList(boardPost.getUserId()));

            PushGubun pushGubun = null;
            String title = null;
            String value = boardComment.getContent();
            String action = String.format(PushAction.BOARD.getValue(), postId);
            boolean titleResIdYn = false;

            if (BoardType.COMPLAINT.getCode().equals(categoryType)) {
                title = PushMessage.BOARD_COMPLAINT_COMMENT_TITLE.getValue();
                pushGubun = PushGubun.BOARD_COMPLAINT;
            } else {
                title = PushMessage.BOARD_COMMENT_REG.getValue();
                pushGubun = PushGubun.BOARD_COMMENT;
                titleResIdYn = true;
            }

            this.pushUtils.sendPush(pushGubun, title, value, action, String.valueOf(postId), titleResIdYn, targetUserList);
        }

    }

    @Override
    public void modifyBoardComment(BoardDto param, BoardCommentVo boardComment) throws Exception {
        // 게시판 카테고리 조회
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(param.getCategoryId());
        param.setBoardCategory(boardCategory);

        // 댓글 수정
        // boardComment.setPostId(param.getPostId());
        boardComment.setId(param.getCommentId());
        this.boardCommentMapper.updateBoardComment(boardComment);
    }

    @Override
    public void removeBoardComment(BoardDto param, BoardCommentVo boardComment) throws Exception {
        // 게시판 카테고리 조회
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(param.getCategoryId());
        param.setBoardCategory(boardCategory);

        // 댓글 삭제
        // boardComment.setPostId(param.getPostId());
        boardComment.setDisplayYn("N");
        this.boardCommentMapper.updateDisplayYn(boardComment);

        // 댓글수 감소
        this.boardPostMapper.updateCommentCount(param.getPostId());
    }

    @Override
    public void regBoardCommentReply(BoardDto param, BoardCommentReplyVo boardCommentReply) throws Exception {
        // 게시판 카테고리 조회
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(param.getCategoryId());
        param.setBoardCategory(boardCategory);

        // 답글 등록
        boardCommentReply.setCommentId(param.getCommentId());
        this.boardCommentReplyMapper.insertBoardCommentReply(boardCommentReply);

        // 댓글 등록자에게 푸시발송
        Long userId = boardCommentReply.getUserId();
        Long commentId = boardCommentReply.getCommentId();

        // 댓글 조회
        BoardCommentVo boardComment = this.boardCommentMapper.selectBoardComment(commentId);

        if (!userId.equals(boardComment.getUserId())) {
            Long postId = boardComment.getPostId();

            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetUserList(PushTargetType.USER, PushAlarmSetting.BOARD, Lists.newArrayList(boardComment.getUserId()));
            String title = PushMessage.BOARD_REPLY_REG.getValue();
            String value = boardCommentReply.getContent();
            String action = String.format(PushAction.BOARD_COMMENT.getValue(), commentId, postId);

            this.pushUtils.sendPush(PushGubun.BOARD_REPLY, title, value, action, String.valueOf(postId), true, targetUserList);
        }

    }

    @Override
    public void modifyBoardCommentReply(BoardDto param, BoardCommentReplyVo boardCommentReply) throws Exception {
        // 게시판 카테고리 조회
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(param.getCategoryId());
        param.setBoardCategory(boardCategory);

        // 댓글 수정
        // boardComment.setPostId(param.getPostId());
        boardCommentReply.setId(param.getReplyId());
        this.boardCommentReplyMapper.updateBoardCommentReply(boardCommentReply);
    }



    @Override
    public void removeBoardCommentReply(BoardDto param, BoardCommentReplyVo boardCommentReply) throws Exception {
        // 게시판 카테고리 조회
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(param.getCategoryId());
        param.setBoardCategory(boardCategory);

        // 답글 삭제
        boardCommentReply.setDisplayYn("N");
        this.boardCommentReplyMapper.updateDisplayYn(boardCommentReply);

        // 답글수 감소
        this.boardCommentMapper.updateReplyCount(param.getCommentId());
    }

    @Override
    public List<BoardCommentVo> findBoardCommentList(BoardDto param) throws Exception {
        Long lastCommentId = (param.getLastCommentId() == null || param.getLastCommentId().equals(0L)) ? Long.MAX_VALUE : param.getLastCommentId();
        param.setLastCommentId(lastCommentId);
        Long userId = param.getUser().id;
        // 댓글 목록 레코드 수 조회
        int totalRecordCount = this.boardCommentMapper.selectBoardCommentListCount(param);
        param.getPagingHelper().setTotalCount(totalRecordCount);

        if (totalRecordCount == 0) {
            return null;
        }

        BoardPostVo boardPost = this.boardPostMapper.selectBoardPost(param.getPostId());
        // 게시판 카테고리 조회
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(boardPost.getCategoryId());
        param.setBoardCategory(boardCategory);

        List<Sort> sortList = new ArrayList<Sort>();
        Sort sort = new Sort();
        sort.setColumn("BC.id");
        sort.setAscOrDesc(BoardConstants.SortType.DESC.getValue());
        sortList.add(sort);

        param.getPagingHelper().setSortList(sortList);

        List<BoardCommentVo> boardCommentList = this.boardCommentMapper.selectBoardCommentList(param);

        if (boardCommentList == null || boardCommentList.isEmpty()) {
            param.getPagingHelper().setNextPageToken(null);
        } else {
            for (BoardCommentVo boardComment : boardCommentList) {
                // 댓글 수정삭제 여부
                if (boardComment.getUserId().equals(userId) || param.getUser().type.jaha) {
                    boardComment.setIsModifiable(true);
                } else {
                    boardComment.setIsModifiable(false);
                }
                // 댓글 등록자명 복호화
                // boardComment.setFullName(baseSecuModel.descString(boardComment.getFullName()));

                User commentUser = this.userService.getUser(boardComment.getUserId());
                boardComment.setWriterName(this.makeUserNameForCommentAndReply(commentUser, param.getBoardCategory().getUserPrivacy()));

                // 댓글의 답글 목록 조회
                param.setCommentId(boardComment.getId());
                List<BoardCommentReplyVo> boardCommentReplyList = this.boardCommentReplyMapper.selectBoardCommentReplyList(param);

                if (boardCommentReplyList != null && !boardCommentReplyList.isEmpty()) {
                    for (BoardCommentReplyVo boardCommentReply : boardCommentReplyList) {
                        // 댓글 수정삭제 여부

                        if (boardCommentReply.getUserId().equals(userId) || param.getUser().type.jaha) {
                            boardCommentReply.setIsModifiable(true);
                        } else {
                            boardCommentReply.setIsModifiable(false);
                        }
                        // 답글 등록자명 복호화
                        // boardCommentReply.setFullName(baseSecuModel.descString(boardCommentReply.getFullName()));

                        User replyUser = this.userService.getUser(boardComment.getUserId());
                        boardCommentReply.setWriterName(this.makeUserNameForCommentAndReply(replyUser, param.getBoardCategory().getUserPrivacy()));
                    }
                }

                boardComment.setCommentReply(boardCommentReplyList);
            }

            int commentListSize = boardCommentList.size();

            BoardCommentVo lastBoardComment = boardCommentList.get(commentListSize - 1);
            param.setLastPostId(lastBoardComment.getId());

            param.getPagingHelper().setNextPageToken(this.boardPostMapper.selectBoardPostNextPageToken(param));
        }

        return boardCommentList;
    }

    /**
     * api 용 댓글조회
     */
    @Override
    public BoardCommentVo findBoardComment(BoardDto param) {
        return boardCommentMapper.selectBoardComment(param.getCommentId());
    }

    /**
     * api 용 답글조회
     */
    @Override
    public BoardCommentReplyVo findBoardCommentReply(BoardDto param) {
        return boardCommentReplyMapper.selectBoardCommentReply(param.getReplyId());
    }


    /**
     * 게시판에 작성자 표시를 고객의 요구에 맞게 다양하게 처리함. 기본 규칙은 닉네임이 있으면 [닉네임 + 동]으로 표시하고 없는 경우는 [이름 + 동]으로 표시함
     *
     * @author namsuk.park
     * @param user
     * @return String 게시판용 작성자명
     */
    private String makeUserNameForCommentAndReply(User user, String userPrivacyType) {
        String userName = HtmlUtils.htmlEscape(user.getFullName());

        if (BoardConstants.UserPrivacy.ALIAS.getCode().equals(userPrivacyType)) {
            // if (user.getNickname() != null && user.house.apt.id != 255) {
            // 2017.01.19 오석민 팀장 요청으로 위시티블루밍5단지아파트 하드코딩 제거
            if (user.getNickname() != null) {
                userName = HtmlUtils.htmlEscape(user.getNickname().name);
            }
        }

        userName += " (" + HtmlUtils.htmlEscape(user.house == null ? "" : user.house.dong) + "동)";

        return userName;
    }
    ////////////////////////////////////////////////////////////////////////////////// 공통 //////////////////////////////////////////////////////////////////////////////////

    @Override
    public Long findGroupBoardExist(Long loginUserId) throws Exception {
        // 로그인 사용자의 지역에 해당하는 그룹어드민 조회
        GroupAdminVo groupAdmin = this.getGroupAdmin(loginUserId);

        Long categoryId = null;

        if (groupAdmin != null) {
            Long aptId = groupAdmin.getAptId();

            // 그룹어드민의 category가 있는지 조회
            List<BoardCategoryVo> categoryList = this.boardCategoryMapper.selectBoardCategoryListByAptId(aptId);

            if (categoryList != null && !categoryList.isEmpty()) {
                for (BoardCategoryVo boardCategory : categoryList) {
                    if (BoardType.GROUP.getCode().equals(boardCategory.getType())) {
                        categoryId = boardCategory.getId();
                        break;
                    }
                }
            }
        }

        return categoryId;
    }

    /**
     * 단체관리자 정보 조회
     *
     * @param userId
     * @return
     */
    private GroupAdminVo getGroupAdmin(Long userId) throws Exception {
        List<Long> searchUserIdList = Lists.newArrayList(userId);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("searchUserIdList", searchUserIdList);
        params.put("searchFrom", BoardType.GROUP.getCode());

        // 사용자의 아파트 지역 정보 조회
        List<SimpleUser> simpleUserList = this.pushMapper.selectTargetUserListForPush(params);

        if (simpleUserList != null && !simpleUserList.isEmpty()) {
            SimpleUser simpleUser = simpleUserList.get(0);
            String area1 = simpleUser.sido;
            String area2 = simpleUser.sigungu;
            // String area3 = simpleUser.addrDong; // 일단 구까지만

            GroupAdminVo param = new GroupAdminVo();
            param.setArea1(area1);
            param.setArea2(area2);
            // param.setUserId(simpleUser.id);

            // 로그인 사용자의 지역에 해당하는 그룹어드민 조회
            List<GroupAdminVo> groupAdmin = this.groupAdminMapper.selectGroupAdminByArea(param);

            if (groupAdmin == null || groupAdmin.isEmpty()) {
                logger.debug("<<단체관리자 정보가 없습니다>> {}", userId);
                return null;
            } else {
                return groupAdmin.get(0);
            }
        }

        logger.debug("<<사용자 정보가 없습니다>> {}", userId);
        return null;
    }

}
