/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 9. 22.
 */
package com.jaha.server.emaul.v2.service.board;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.jaha.server.emaul.model.FileInfo;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.TagUtils;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.v2.constants.BoardConstants;
import com.jaha.server.emaul.v2.constants.BoardConstants.BoardType;
import com.jaha.server.emaul.v2.constants.CommonConstants.ResponseCode;
import com.jaha.server.emaul.v2.model.board.ApiPostResponse;
import com.jaha.server.emaul.v2.model.board.ApiPostResponseBody;
import com.jaha.server.emaul.v2.model.board.ApiPostResponseHeader;
import com.jaha.server.emaul.v2.model.board.BoardCategoryVo;
import com.jaha.server.emaul.v2.model.board.BoardCommentReplyVo;
import com.jaha.server.emaul.v2.model.board.BoardCommentVo;
import com.jaha.server.emaul.v2.model.board.BoardDto;
import com.jaha.server.emaul.v2.model.board.BoardPostVo;
import com.jaha.server.emaul.v2.model.board.BoardPostWrapper;

/**
 * <pre>
 * Class Name : BoardServiceAdvice.java
 * Description : 게시판 서비스 실행 이후, 작성자명/제목명 등 공통 변경 처리
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
@Aspect
@Component
public class BoardServiceAdvice {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${board.attach.download.url}")
    private String boardAttachDownloadUrl;

    @Autowired
    private UserService userService;

    @SuppressWarnings("unused")
    private SimpleDateFormat POST_SDF = new SimpleDateFormat("yyyy.MM.dd");

    /**
     * 에러없이 정상적으로 타켓 메소드가 실행된 이후에 실행됨
     *
     * @param joinPoint
     */
    @AfterReturning(pointcut = "execution(public * com.jaha.server.emaul.v2.service.board.*BoardServiceImpl.find*(..))", returning = "returnObject")
    public void executeAfterReturnFindMethod(JoinPoint joinPoint, Object returnObject) throws Throwable {
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        logger.debug("<<클래스명: {}, 메소드명: {}>>", className, methodName);
        // logger.debug("<<joinPoint>> {}", joinPoint);

        Object params[] = joinPoint.getArgs();

        BoardCategoryVo boardCategory = null;
        BoardDto boardDto = null;

        for (Object param : params) {
            if (param instanceof BoardDto) {
                boardCategory = ((BoardDto) param).getBoardCategory();
                boardDto = (BoardDto) param;
                // logger.debug(ToStringBuilder.reflectionToString(param));
            }
        }

        if (boardDto == null) {
            return;
        }

        if (returnObject != null) {
            if (methodName.startsWith("findBoardComment")) { // 댓글 처리
                this.makeBoardCommentApiResponse(boardDto, returnObject);
            } else { // 게시글 처리
                if (returnObject instanceof BoardPostWrapper<?>) {
                    this.makeBoardPostWrapperApiResponse(boardDto, boardCategory, returnObject);
                } else {
                    this.makeBoardPostApiResponse(boardDto, boardCategory, returnObject);
                }
            }
        }
    }

    /**
     * 댓글 응답 API 만들기
     *
     * @param boardDto
     * @param returnObject
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void makeBoardCommentApiResponse(BoardDto boardDto, Object returnObject) throws Exception {
        ApiPostResponse<ApiPostResponseBody> apiResponse = (ApiPostResponse<ApiPostResponseBody>) boardDto.getApiResponse();
        ApiPostResponseBody body = new ApiPostResponseBody();
        body.setCategory(boardDto.getBoardCategory());

        if (returnObject instanceof List<?>) {
            List<BoardCommentVo> boardCommentList = (List<BoardCommentVo>) returnObject;
            body.setContent(boardCommentList);
            apiResponse.setFooter(boardDto.getPagingHelper());
        } else if (returnObject instanceof BoardCommentVo) {
            BoardCommentVo boardComment = (BoardCommentVo) returnObject;

            body.setContent(boardComment);

            Map<String, Boolean> map = new HashMap<String, Boolean>();

            if (boardDto.getUser() != null && boardDto.getUser().id.equals(boardComment.getUserId())) {
                map.put("isModifiable", true);
                apiResponse.setFooter(true);
            } else {
                map.put("isModifiable", false);
            }

            apiResponse.setFooter(map);

        } else if (returnObject instanceof BoardCommentReplyVo) {
            BoardCommentReplyVo boardCommentReply = (BoardCommentReplyVo) returnObject;

            body.setContent(boardCommentReply);

            Map<String, Boolean> map = new HashMap<String, Boolean>();

            if (boardDto.getUser() != null && boardDto.getUser().id.equals(boardCommentReply.getUserId())) {
                map.put("isModifiable", true);
                apiResponse.setFooter(true);
            } else {
                map.put("isModifiable", false);
            }

            apiResponse.setFooter(map);

        }

        apiResponse.setHeader(new ApiPostResponseHeader(ResponseCode.SUCCESS));
        apiResponse.setBody(body);

        boardDto.setApiResponse(apiResponse);
    }

    /**
     * 게시글 응답 API 만들기
     *
     * @param boardDto
     * @param boardCategory
     * @param returnObject
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void makeBoardPostApiResponse(BoardDto boardDto, BoardCategoryVo boardCategory, Object returnObject) throws Exception {
        ApiPostResponse<ApiPostResponseBody> apiResponse = (ApiPostResponse<ApiPostResponseBody>) boardDto.getApiResponse();
        ApiPostResponseBody body = new ApiPostResponseBody();
        body.setCategory(boardDto.getBoardCategory());

        if (returnObject instanceof List<?>) {
            List<? extends BoardPostVo> boardPostList = (List<? extends BoardPostVo>) returnObject;

            for (BoardPostVo boardPost : boardPostList) {
                User user = this.userService.getUser(boardPost.getUserId());

                boardPost.setWriterName(this.makeUserNameForPost(user, boardCategory.getUserPrivacy()));
                if (user.type.jaha) {
                    boardPost.setUserAptName("e마을");
                } else {
                    boardPost.setUserAptName(user.house.apt.name);
                }

                this.convertBoardPostTitle(boardCategory, boardPost);
            }

            returnObject = boardPostList;

            body.setContent(boardPostList);

            if (BoardType.GROUP.getCode().equals(boardCategory.getType())) {
                body.setContentExt(boardDto.getGroupAdmin());
            }

            apiResponse.setFooter(boardDto.getPagingHelper());

            // logger.debug(returnObject.toString());
        } else if (returnObject instanceof BoardPostVo) {
            BoardPostVo boardPost = (BoardPostVo) returnObject;
            User user = this.userService.getUser(boardPost.getUserId());

            boardPost.setWriterName(this.makeUserNameForPost(user, boardCategory.getUserPrivacy()));
            boardPost.setUserAptName(user.house.apt.name);

            this.convertBoardPostTitle(boardCategory, boardPost);
            this.convertBoardPostBody(boardCategory, boardPost);
            // logger.debug(returnObject.toString());

            body.setContent(boardPost);

            Map<String, Boolean> map = new HashMap<String, Boolean>();

            if (boardDto.getUser() != null && boardDto.getUser().id.equals(boardPost.getUserId())) {
                map.put("isModifiable", true);
            } else {
                map.put("isModifiable", false);
            }

            apiResponse.setFooter(map);
        }

        apiResponse.setHeader(new ApiPostResponseHeader(ResponseCode.SUCCESS));
        apiResponse.setBody(body);

        boardDto.setApiResponse(apiResponse);
    }

    /**
     * 게시글 응답 API 만들기
     *
     * @param boardDto
     * @param boardCategory
     * @param returnObject
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void makeBoardPostWrapperApiResponse(BoardDto boardDto, BoardCategoryVo boardCategory, Object returnObject) throws Exception {
        ApiPostResponse<ApiPostResponseBody> apiResponse = (ApiPostResponse<ApiPostResponseBody>) boardDto.getApiResponse();
        ApiPostResponseBody body = new ApiPostResponseBody();
        body.setCategory(boardDto.getBoardCategory());

        BoardPostWrapper<?> boardPostWrapper = (BoardPostWrapper<?>) returnObject;
        BoardPostVo boardPost = boardPostWrapper.get();

        User user = this.userService.getUser(boardPost.getUserId());

        boardPost.setWriterName(this.makeUserNameForPost(user, boardCategory.getUserPrivacy()));
        boardPost.setUserAptName(user.house.apt.name);

        this.convertBoardPostTitle(boardCategory, boardPost);
        this.convertBoardPostBody(boardCategory, boardPost);
        // logger.debug(returnObject.toString());

        if (BoardType.EVENT.getCode().equals(boardDto.getBoardCategory().getType())) {
            List<FileInfo> fileInfoList = boardPost.getFileInfoList();

            if (fileInfoList != null && !fileInfoList.isEmpty()) {
                for (FileInfo fileInfo : fileInfoList) {
                    String fileDownloadUrl = String.format(this.boardAttachDownloadUrl, boardPost.getId(), fileInfo.fileName);
                    boardPost.setThumbUrl(fileDownloadUrl);
                }
            }
        } else if (BoardType.GROUP.getCode().equals(boardDto.getBoardCategory().getType())) {
            body.setContentExt(boardDto.getGroupAdmin());
        }

        body.setContent(boardPost);

        List<FileInfo> fileInfoList = boardPost.getFileInfoList();

        if (fileInfoList != null && !fileInfoList.isEmpty()) {
            List<Map<String, String>> attachFile = new ArrayList<Map<String, String>>();

            for (FileInfo fileInfo : fileInfoList) {
                Map<String, String> fileMap = new HashMap<String, String>();

                String fileName = fileInfo.fileOriginName;
                String fileDownloadUrl = String.format(this.boardAttachDownloadUrl, boardPost.getId(), fileInfo.fileName);

                fileMap.put("fileName", fileName);
                fileMap.put("fileDownloadUrl", fileDownloadUrl);

                attachFile.add(fileMap);
            }

            boardPost.setAttachFile(attachFile);
        }

        Map<String, Boolean> map = new HashMap<String, Boolean>();

        if (boardDto.getUser() != null && boardDto.getUser().id.equals(boardPost.getUserId())) {
            map.put("isModifiable", true);
        } else {
            map.put("isModifiable", false);
        }

        apiResponse.setFooter(map);

        apiResponse.setHeader(new ApiPostResponseHeader(ResponseCode.SUCCESS));
        apiResponse.setBody(body);

        boardDto.setApiResponse(apiResponse);
    }

    /**
     * 게시판에 작성자 표시를 고객의 요구에 맞게 다양하게 처리함. 기본 규칙은 닉네임이 있으면 [닉네임 + 동]으로 표시하고 없는 경우는 [이름 + 동]으로 표시함
     *
     * @author namsuk.park
     * @param user
     * @return String 게시판용 작성자명
     */
    private String makeUserNameForPost(User user, String userPrivacyType) {
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

    /**
     * 게시판 카테고리 모드가 html이고 게시판 title이 비어있는 경우에만 게시판 내용을 변환하여 타이틀로 수정
     *
     * @param boardCategory
     * @param boardPost
     */
    private void convertBoardPostTitle(BoardCategoryVo boardCategory, BoardPostVo boardPost) {
        // if (BoardConstants.ContentMode.HTML.getValue().equals(boardCategory.getContentMode()) && StringUtils.isBlank(boardPost.getTitle())) {
        if (StringUtils.isBlank(boardPost.getTitle())) {
            String tempContent = boardPost.getContent();
            String tempTitle = TagUtils.removeTag(tempContent).replaceAll("<!DOCTYPE html>", StringUtils.EMPTY);

            if (tempTitle.length() > 300) {
                boardPost.setTitle(tempTitle.substring(0, 300));
            } else {
                boardPost.setTitle(tempTitle);
            }
        }
    }

    /**
     * 게시판 카테고리 모드가 html인 경우, <body> 태그 안의 내용 추출
     *
     * @param boardCategory
     * @param boardPost
     */
    private void convertBoardPostBody(BoardCategoryVo boardCategory, BoardPostVo boardPost) {
        String content = StringUtils.defaultString(boardPost.getContent());

        if (BoardConstants.ContentMode.HTML.getValue().equals(boardCategory.getContentMode())) {
            if (content.indexOf("<!DOCTYPE html>") > -1) {
                content = content.replaceAll("(\r\n|\n)", StringUtils.EMPTY);
            }

            String contentOnlyBody = TagUtils.extractBody(content);

            boardPost.setContentOnlyBody(contentOnlyBody);
            return;
        }

        boardPost.setContentOnlyBody(content);
    }

}
