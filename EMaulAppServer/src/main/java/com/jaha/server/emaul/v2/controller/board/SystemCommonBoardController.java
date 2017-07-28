/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 9. 22.
 */
package com.jaha.server.emaul.v2.controller.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.repo.CommonCodeRepository;
import com.jaha.server.emaul.v2.constants.CommonConstants.ResponseCode;
import com.jaha.server.emaul.v2.model.board.ApiPostResponse;
import com.jaha.server.emaul.v2.model.board.ApiPostResponseHeader;
import com.jaha.server.emaul.v2.model.board.BoardDto;
import com.jaha.server.emaul.v2.model.board.json.PostView;
import com.jaha.server.emaul.v2.service.board.BoardService;
import com.jaha.server.emaul.v2.util.PagingHelper;

/**
 * <pre>
 * Class Name : SystemCommonBoardController.java
 * Description : 이벤트/FAQ/시스템 공지사항 처리 게시판, 카테고리 타입이 하나만 존재하며 자하권한에만 쓰기권한이 있음
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
@RestController
public class SystemCommonBoardController {

    @Value("${event.board.category.id}")
    private Long eventBoardCategoryId;

    @Value("${faq.board.category.id}")
    private Long faqBoardCategoryId;

    @Value("${system.notice.board.category.id}")
    private Long systemNoticeBoardCategoryId;

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemCommonBoardController.class);

    @Autowired
    private BoardService boardService;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    /**
     * FAQ 게시글 서브카테고리 목록 조회
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @return
     */
    @RequestMapping(value = "/v2/api/board/faq/sub-category")
    public ApiResponse<?> findFaqBoardSubCategoryList(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto) {
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();

        try {
            List<CommonCode> subCategoryList = this.commonCodeRepository.findByCodeGroupAndUseYnOrderBySortOrderAsc("FAQ_SUBCATEGORY", "Y");

            Map<String, Object> bodyMap = new HashMap<String, Object>();
            List<Map<String, String>> codeMapList = new ArrayList<Map<String, String>>();

            for (CommonCode commonCode : subCategoryList) {
                Map<String, String> codeMap = new HashMap<String, String>();
                codeMap.put("id", commonCode.getCode());
                codeMap.put("name", commonCode.getName());

                codeMapList.add(codeMap);
            }

            bodyMap.put("subCategory", codeMapList);

            apiResponse.setHeader(new ApiResponseHeader(ResponseCode.SUCCESS));
            apiResponse.setBody(bodyMap);
        } catch (Exception e) {
            LOGGER.error("<<FAQ 서브 카테고리 조회 중 오류>>", e);

            apiResponse.setHeader(new ApiResponseHeader(ResponseCode.FAIL));
        }

        return apiResponse;
    }

    /**
     * FAQ 게시글 목록 조회
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @return
     */
    @RequestMapping(value = "/v2/api/board/faq/list")
    @JsonView({PostView.FaqList.class})
    public ApiPostResponse<?> findFaqBoardPostList(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            boardDto.setCategoryId(this.faqBoardCategoryId);
            boardDto.setDisplayYn("Y");

            // 게시글 목록 조회
            this.boardService.findBoardPostList(boardDto);
        } catch (Exception e) {
            LOGGER.error("<<FAQ 게시판 목록 조회 중 오류>>", e);

            boardDto.getApiResponse().setHeader(new ApiPostResponseHeader(ResponseCode.FAIL));
        }

        return boardDto.getApiResponse();
    }

    /**
     * 이벤트 게시글 목록 조회
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @return
     */
    @RequestMapping(value = "/v2/api/board/event/list")
    @JsonView({PostView.EventList.class})
    public ApiPostResponse<?> findEventBoardPostList(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            boardDto.setCategoryId(this.eventBoardCategoryId);

            // 게시글 목록 조회
            this.boardService.findBoardPostList(boardDto);
        } catch (Exception e) {
            LOGGER.error("<<이벤트 게시판 목록 조회 중 오류>>", e);

            boardDto.getApiResponse().setHeader(new ApiPostResponseHeader(ResponseCode.FAIL));
        }

        return boardDto.getApiResponse();
    }

    /**
     * e마을 공지사항 게시글 목록 조회
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @return
     */
    @RequestMapping(value = "/v2/api/board/system-notice/list")
    @JsonView({PostView.SystemNoticeList.class})
    public ApiPostResponse<?> findSystemNoticeBoardPostList(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            boardDto.setCategoryId(this.systemNoticeBoardCategoryId);

            // 게시글 목록 조회
            this.boardService.findBoardPostList(boardDto);
        } catch (Exception e) {
            LOGGER.error("<<e마을 공지사항 게시판 게시글 조회 중 오류>>", e);

            boardDto.getApiResponse().setHeader(new ApiPostResponseHeader(ResponseCode.FAIL));
        }

        return boardDto.getApiResponse();
    }

    /**
     * FAQ 게시글 글 상세 조회
     *
     * @param req
     * @param model
     * @param pagingHelper
     * @param boardDto
     * @param postId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/faq/post/{postId}")
    @JsonView({PostView.Faq.class})
    public ApiPostResponse<?> findFaqBoardPost(HttpServletRequest req, Model model, PagingHelper pagingHelper, BoardDto boardDto, @PathVariable(value = "postId") Long postId) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            boardDto.setCategoryId(this.faqBoardCategoryId);

            // 게시글 상세 조회
            this.boardService.findBoardPost(boardDto, postId, false);

        } catch (Exception e) {
            LOGGER.error("<<FAQ 게시판 게시글 상세 조회 중 오류>>", e);

            boardDto.getApiResponse().setHeader(new ApiPostResponseHeader(ResponseCode.FAIL));
        }

        return boardDto.getApiResponse();
    }

    /**
     * e마을 게시판 게시글 글 상세 조회
     *
     * @param req
     * @param model
     * @param pagingHelper
     * @param boardDto
     * @param postId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/system-notice/post/{postId}")
    @JsonView({PostView.SystemNotice.class})
    public ApiPostResponse<?> findSystemNoticeBoardPost(HttpServletRequest req, Model model, PagingHelper pagingHelper, BoardDto boardDto, @PathVariable(value = "postId") Long postId) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            boardDto.setCategoryId(this.systemNoticeBoardCategoryId);

            // 게시글 상세 조회
            this.boardService.findBoardPost(boardDto, postId, false);

        } catch (Exception e) {
            LOGGER.error("<<e마을 공지사항 게시판 게시글 상세 조회 중 오류>>", e);

            boardDto.getApiResponse().setHeader(new ApiPostResponseHeader(ResponseCode.FAIL));
        }

        return boardDto.getApiResponse();
    }

    /**
     * 이벤트 게시판 게시글 글 상세 조회
     *
     * @param req
     * @param model
     * @param pagingHelper
     * @param boardDto
     * @param postId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/event/post/{postId}")
    @JsonView({PostView.Event.class})
    public ApiPostResponse<?> findEventBoardPost(HttpServletRequest req, Model model, PagingHelper pagingHelper, BoardDto boardDto, @PathVariable(value = "postId") Long postId) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            boardDto.setCategoryId(this.eventBoardCategoryId);

            // 게시글 상세 조회
            this.boardService.findBoardPost(boardDto, postId, false);

        } catch (Exception e) {
            LOGGER.error("<<이벤트 게시판 게시글 상세 조회 중 오류>>", e);

            boardDto.getApiResponse().setHeader(new ApiPostResponseHeader(ResponseCode.FAIL));
        }

        return boardDto.getApiResponse();
    }

}
