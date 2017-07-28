/**
 *
 */
package com.jaha.server.emaul.v2.mapper.board;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.v2.model.board.BoardDto;
import com.jaha.server.emaul.v2.model.board.BoardPostVo;

/**
 * @author 전강욱(realsnake@jahasmart.com) <br />
 *         This Mapper class mapped db-table called board_post
 */
@Mapper
public interface BoardPostMapper {

    /**
     * board_post에 데이타를 입력한다.
     */
    public void insertBoardPost(BoardPostVo param);

    /**
     * board_post의 데이타를 수정한다.
     */
    public void updateBoardPost(BoardPostVo param);

    /**
     * board_post 노출여부 수정
     */
    public void updateDisplayYn(BoardPostVo param);

    /**
     * board_post의 상세 데이타를 조회한다.
     */
    public BoardPostVo selectBoardPost(Long param);

    /**
     * board_post의 총 레코드 수를 검색조건에 맞게 조회한다.
     */
    public int selectBoardPostListCount(BoardDto param);

    /**
     * board_post의 목록을 검색조건에 맞게 조회한다.
     */
    public List<BoardPostVo> selectBoardPostList(BoardDto param);

    /**
     * 앱 메인 템플릿용 IN 카테고리 별 게시물 목록
     *
     */
    public List<Map<String, Object>> selectBoardCategoryPostList(BoardDto param);

    /**
     * board_post 조회수 증가
     */
    public void updateViewCount(Long postId);

    /**
     * board_post 상단 고정 수정
     */
    public void updateTopFix(BoardPostVo param);

    /**
     * board_post 차단
     */
    public void updateBlocked(BoardPostVo param);

    /**
     * 댓글수 감소
     */
    public void updateCommentCount(Long param);

    /**
     * 이미지 삭제 시 이미지 카운트 감소
     */
    public void updateImageCount(BoardPostVo param);

    /**
     * 선택한 첨부파일 삭제
     */
    public void updateAttachFileNull(BoardPostVo param);

    /**
     * board_post의 마지막 게시글 아이디를 검색조건에 맞게 조회한다.
     */
    public Long selectBoardPostNextPageToken(BoardDto param);

    /**
     * 게시판 new icon 표기용
     *
     * @return
     */
    public List<Map<String, Object>> selectBoardSum(List<Map<String, Object>> paramList);

}
