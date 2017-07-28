/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 11. 8.
 */
package com.jaha.server.emaul.v2.controller.board;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.v2.constants.BoardConstants.BoardType;
import com.jaha.server.emaul.v2.constants.CommonConstants.ResponseCode;
import com.jaha.server.emaul.v2.model.board.ApiPostResponse;
import com.jaha.server.emaul.v2.model.board.ApiPostResponseHeader;
import com.jaha.server.emaul.v2.model.board.BoardCategoryVo;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.v2.model.board.BoardCommentReplyVo;
import com.jaha.server.emaul.v2.model.board.BoardCommentVo;
import com.jaha.server.emaul.v2.model.board.BoardDto;
import com.jaha.server.emaul.v2.model.board.BoardPostVo;
import com.jaha.server.emaul.v2.model.board.json.CommentView;
import com.jaha.server.emaul.v2.service.board.BoardCategoryService;
import com.jaha.server.emaul.v2.service.board.BoardService;
import com.jaha.server.emaul.v2.util.PagingHelper;

/**
 * <pre>
 * Class Name : CommonBoardController.java
 * Description : 게시판 댓글/답글, 이미지 뷰 등 공통 컨트롤러
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 11. 8.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 11. 8.
 * @version 1.0
 */
@RestController
public class CommonBoardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonBoardController.class);

    @Value("${file.path.temp}")
    private String tempFilePath;

    @Value("${file.path.board.image}")
    private String filePathBoardImage;

    @Value("${file.path.board.attach}")
    private String filePathBoardAttach;

    @Autowired
    private BoardCategoryService boardCategoryService;

    @Autowired
    private BoardService boardService;

    /**
     * 게시글 목록 조회
     *
     * @param req
     * @param model
     * @param pagingHelper
     * @param boardDto
     * @param categoryType notice / event / group / tts / community / complaint
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/{categoryType}/list/{categoryId}")
    public String findBoardPostList(HttpServletRequest req, Model model, PagingHelper pagingHelper, BoardDto boardDto, @PathVariable(value = "categoryType") String categoryType,
            @PathVariable(value = "categoryId") Long categoryId) {
        try {
            boardDto.setCategoryId(categoryId);

            // 게시글 목록 조회
            List<?> boardPostList = this.boardService.findBoardPostList(boardDto);

            model.addAttribute("category", boardDto.getBoardCategory());
            model.addAttribute("boardPostList", boardPostList);
        } catch (Exception e) {
            LOGGER.error("<<게시판 게시글 조회 중 오류>>", e);
        }

        String viewPath = "v2/api/board/" + categoryType + "/list";

        return viewPath;
    }

    /**
     * 게시글 글 등록 페이지 이동
     *
     * @param req
     * @param model
     * @param pagingHelper
     * @param boardDto
     * @param categoryType notice / event / group / tts / community / complaint
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/{categoryType}/create-form/{categoryId}", method = RequestMethod.GET)
    public String moveBoardPostForm(HttpServletRequest req, Model model, PagingHelper pagingHelper, BoardDto boardDto, @PathVariable(value = "categoryType") String categoryType,
            @PathVariable(value = "categoryId") Long categoryId) {
        try {
            boardDto.setCategoryId(categoryId);

            BoardCategoryVo boardCategory = this.boardCategoryService.findBoardCategory(categoryId);
            model.addAttribute("category", boardCategory);
        } catch (Exception e) {
            LOGGER.error("<<게시판 게시글 등록 페이지 이동 중 오류>>", e);
        }

        String viewPath = "v2/api/board/" + categoryType + "/create-form";

        return viewPath;
    }

    /**
     * 게시글 글 삭제
     *
     * @param req
     * @param boardDto
     * @param boardPost
     * @param categoryType notice / event / group / tts / community / complaint
     * @param categoryId
     * @param postId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/{categoryType}/remove/{categoryId}/{postId}")
    public String removeBoardPost(HttpServletRequest req, BoardDto boardDto, BoardPostVo boardPost, @PathVariable(value = "categoryType") String categoryType,
            @PathVariable(value = "categoryId") Long categoryId, @PathVariable(value = "postId") Long postId) {
        try {
            boardPost.setId(postId);

            // 게시글 삭제
            this.boardService.removeBoardPost(boardDto, boardPost);
        } catch (Exception e) {
            LOGGER.error("<<게시판 게시글 삭제 중 오류>>", e);
        }

        if (BoardType.EVENT.getCode().equalsIgnoreCase(categoryType)) {
            return "redirect:/v2/api/board/" + categoryType + "/list";
        } else {
            return "redirect:/v2/api/board/" + categoryType + "/list/" + categoryId;
        }
    }

    /**
     * 댓글 목록
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @param boardComment
     * @return
     */
    @RequestMapping(value = "/v2/api/board/comment/list", method = RequestMethod.GET)
    @JsonView({CommentView.CommentList.class})
    public ApiPostResponse<?> findBoardCommentList(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            // 댓글 목록 조회
            this.boardService.findBoardCommentList(boardDto);
        } catch (Exception e) {
            LOGGER.error("<<댓글 목록 조회 중 오류>>", e);

            boardDto.getApiResponse().setHeader(new ApiPostResponseHeader(ResponseCode.FAIL));
        }

        return boardDto.getApiResponse();
    }

    /**
     * 댓글 등록
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @param boardComment
     * @param categoryType notice / event / group / tts / community / complaint
     * @return
     */
    @RequestMapping(value = "/v2/api/board/comment/create", method = RequestMethod.POST)
    @JsonView({CommentView.Comment.class})
    public ApiPostResponse<?> createBoardComment(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto, BoardCommentVo boardComment) {
        boardDto.setApiResponse(new ApiPostResponse<>());

        try {
            // 댓글 등록
            this.boardService.regBoardComment(boardDto, boardComment);
            boardDto.setCommentId(boardComment.getId());
            boardService.findBoardComment(boardDto);
        } catch (Exception e) {
            LOGGER.error("<<댓글 등록 중 오류>>", e);
        }
        // boardDto.getApiResponse().setFooter(footer);
        return boardDto.getApiResponse();
    }

    /**
     * 댓글 수정
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @param boardComment
     * @param categoryType notice / event / group / tts / community / complaint
     * @param commentId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/comment/modify", method = RequestMethod.POST)
    @JsonView({CommentView.Comment.class})
    public ApiPostResponse<?> modifyBoardComment(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto, BoardCommentVo boardComment) {
        boardDto.setApiResponse(new ApiPostResponse<>());
        try {
            // 댓글 수정
            this.boardService.modifyBoardComment(boardDto, boardComment);
            boardService.findBoardComment(boardDto);

        } catch (Exception e) {
            LOGGER.error("<<댓글 수정 중 오류>>", e);
        }
        return boardDto.getApiResponse();
    }

    /**
     * 댓글 삭제
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @param boardComment
     * @param categoryType notice / event / group / tts / community / complaint
     * @param commentId
     * @return
     */
    @RequestMapping(value = "/v2/api/board/{categoryType}/comment/remove/{commentId}", method = RequestMethod.POST)
    public String removeBoardComment(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto, BoardCommentVo boardComment, @PathVariable(value = "categoryType") String categoryType,
            @PathVariable(value = "commentId") Long commentId) {
        try {
            // 댓글 삭제
            boardComment.setId(commentId);
            this.boardService.removeBoardComment(boardDto, boardComment);

        } catch (Exception e) {
            LOGGER.error("<<댓글 삭제 중 오류>>", e);
        }

        return null;
    }

    /**
     * 답글 등록
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @param boardCommentReply
     * @param categoryType notice / event / group / tts / community / complaint
     * @return
     */
    @RequestMapping(value = "/v2/api/board/comment/reply/create", method = RequestMethod.POST)
    @JsonView({CommentView.Comment.class})
    public ApiPostResponse<?> createBoardCommentReply(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto, BoardCommentReplyVo boardCommentReply) {
        boardDto.setApiResponse(new ApiPostResponse<>());
        try {
            // 답글 등록
            this.boardService.regBoardCommentReply(boardDto, boardCommentReply);
            boardDto.setReplyId(boardCommentReply.getId());
            boardService.findBoardCommentReply(boardDto);


        } catch (Exception e) {
            LOGGER.error("<<답글 등록 중 오류>>", e);
        }

        return boardDto.getApiResponse();
    }

    /**
     * 답글 수정
     *
     * @param req
     * @param pagingHelper
     * @param boardDto
     * @param boardComment
     * @return
     */
    @RequestMapping(value = "/v2/api/board/comment/reply/modify", method = RequestMethod.POST)
    @JsonView({CommentView.Comment.class})
    public ApiPostResponse<?> modifyBoardReplyComment(HttpServletRequest req, PagingHelper pagingHelper, BoardDto boardDto, BoardCommentReplyVo boardCommentReply) {
        boardDto.setApiResponse(new ApiPostResponse<>());
        try {
            // 답글 수정
            this.boardService.modifyBoardCommentReply(boardDto, boardCommentReply);
            boardService.findBoardCommentReply(boardDto);

        } catch (Exception e) {
            LOGGER.error("<<댓글 수정 중 오류>>", e);
        }
        return boardDto.getApiResponse();
    }


    /**
     * 답글 삭제
     *
     * @param req
     * @param model
     * @param redirectAttr
     * @param pagingHelper
     * @param boardDto
     * @param boardCommentReply
     * @param categoryType notice / event / group / tts / community / complaint
     * @param replyId
     * @return
     */
    @RequestMapping(value = "/v2/{userAuthType}/board/{categoryType}/comment/reply/remove/{replyId}", method = RequestMethod.POST)
    public String removeBoardCommentReply(HttpServletRequest req, Model model, RedirectAttributes redirectAttr, PagingHelper pagingHelper, BoardDto boardDto, BoardCommentReplyVo boardCommentReply,
            @PathVariable(value = "categoryType") String categoryType, @PathVariable(value = "replyId") Long replyId) {
        try {
            // 답글 삭제
            boardCommentReply.setId(replyId);
            this.boardService.removeBoardCommentReply(boardDto, boardCommentReply);

            // 해당 댓글로 이동
            redirectAttr.addFlashAttribute("commentIdForAnchor", boardDto.getCommentId());
            // 답글 노출 여부
            redirectAttr.addFlashAttribute("replyAreaDisplayYn", "Y");

            if (BoardType.EVENT.getCode().equalsIgnoreCase(categoryType) || BoardType.FAQ.getCode().equalsIgnoreCase(categoryType)) {
                return "redirect:/v2/api/board/" + categoryType + "/read/" + boardDto.getPostId();
            } else {
                return "redirect:/v2/api/board/" + categoryType + "/read/" + boardDto.getCategoryId() + "/" + boardDto.getPostId();
            }
        } catch (Exception e) {
            LOGGER.error("<<답글 삭제 중 오류>>", e);
        }

        return "redirect:/";
    }

    /**
     * 이미지 파일 뷰
     *
     * @param postId
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/v2/api/board/common/post/image/{postId}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleImageRequest(@PathVariable("postId") Long postId, @PathVariable("fileName") String fileName) {
        File imageFile = new File(String.format(this.filePathBoardImage, postId / 1000L, postId), fileName);
        return Responses.getFileEntity(imageFile, postId + "-" + fileName);
    }

    /**
     * 이미지 다운로드 URL 웹 동기화
     *
     * @param postId
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/v2/board/common/post/image/{postId}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleImageRequestNew(@PathVariable("postId") Long postId, @PathVariable("fileName") String fileName) {
        File imageFile = new File(String.format(this.filePathBoardImage, postId / 1000L, postId), fileName);
        return Responses.getFileEntity(imageFile, postId + "-" + fileName);
    }

    /**
     * 첨부파일 다운로드
     *
     * @param postId
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/v2/api/board/common/post/file/{postId}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleFileRequest(@PathVariable("postId") Long postId, @PathVariable("fileName") String fileName) {
        File attachFile = new File(String.format(this.filePathBoardAttach, postId / 1000L, postId), fileName);
        return Responses.getFileEntity(attachFile, fileName);
    }

    /**
     * 첨부파일 다운로드 URL 웹 동기화
     *
     * @param postId
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/v2/board/common/post/file/{postId}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleFileRequestNew(@PathVariable("postId") Long postId, @PathVariable("fileName") String fileName) {
        File attachFile = new File(String.format(this.filePathBoardAttach, postId / 1000L, postId), fileName);
        return Responses.getFileEntity(attachFile, fileName);
    }

    /**
     * 아파트별로 이미지와 파일을 리턴한다.<br />
     * 서수원자이 홈페이지에서 이관한 데이터, 이미지, 파일을 처리하기 위해서... (2016.07.04)
     *
     * @author PNS
     * @param aptId
     * @param type
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/v2/api/public/files/{aptId}/{type}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleFileRequestByApt(@PathVariable("aptId") String aptId, @PathVariable("type") String type, @PathVariable("fileName") String fileName) {
        try {
            String decFilename = URLDecoder.decode(fileName, "utf-8");
            File file = new File("/nas/EMaul/files", String.format("/%s/%s/%s", aptId, type, decFilename));

            return Responses.getFileEntity(file, aptId + "-" + fileName);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("<<파일 및 이미지 다운로드 중 오류>>", e);
        }

        return null;
    }

    /**
     * 파일을 임시 저장 폴더에 업로드한다.<br />
     *
     * @param img
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/v2/api/board/common/file/temp/upload", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadBoardTempFile(@RequestParam(value = "attachFile", required = false) MultipartFile attachFile) throws JsonProcessingException {
        this.deleteOldTempFiles();

        String originalFileName = attachFile.getOriginalFilename();
        String ext = FilenameUtils.getExtension(originalFileName);

        ObjectMapper mapper = new ObjectMapper();
        FileInfo fileInfo = null;

        try {
            File dir = new File(this.tempFilePath);
            if (!dir.exists()) {
                dir.mkdirs();
                dir.setReadable(true, false);
                dir.setWritable(true, false);
            }

            String onlyFileName = FilenameUtils.getName(originalFileName);
            String fileName = FilenameUtils.removeExtension(onlyFileName) + "-" + new SimpleDateFormat("HHmmss").format(new Date()) + "." + ext;
            File dest = new File(dir, fileName);

            while (dest.exists()) {
                dest = new File(dir, fileName);
            }

            dest.createNewFile();
            dest.setReadable(true, false);
            dest.setWritable(true, false);

            attachFile.transferTo(dest);

            fileInfo = new FileInfo();
            fileInfo.filePath = this.tempFilePath;
            fileInfo.fileName = dest.getName();
            fileInfo.fileOriginName = fileName; // FilenameUtils.getName(originalFileName);
            fileInfo.ext = ext;
            fileInfo.size = attachFile.getSize() / 1024; // kb 단위로 저장
        } catch (Exception e) {
            LOGGER.error("<<파일업로드 오류>> {}", e.getMessage());
        }

        return mapper.writeValueAsString(fileInfo);
    }

    /**
     * 임시 저장소의 오래된 파일 삭제
     */
    private void deleteOldTempFiles() {
        File dir = new File(this.tempFilePath);
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return System.currentTimeMillis() - pathname.lastModified() > 24 * 60 * 60 * 1000;
            }
        });
        if (files != null && files.length != 0) {
            for (File file : files) {
                file.delete();
            }
        }
    }

}
