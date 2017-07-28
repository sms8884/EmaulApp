/**
 *
 */
package com.jaha.server.emaul.v2.mapper.app;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.v2.model.app.AppCategoryVo;
import com.jaha.server.emaul.v2.model.app.AppVersionV2Vo;
import com.jaha.server.emaul.v2.model.common.PushLogVo;

/**
 * @author 조영태(cyt@jahasmart.com) <br />
 *         This Mapper class mapped db-table called app_version_v2, app_category, push_log
 */
@Mapper
public interface AppMapper {

    /**
     * App 버전정보를 조회한다.
     *
     * @param voteDto
     * @return
     */
    public List<AppVersionV2Vo> selectAppVersionV2List(AppVersionV2Vo appVersionV2Vo);

    /**
     * App 용 카테고리 정보 조회
     *
     * @param appCategoryVo
     * @return
     */
    public List<AppCategoryVo> selectCategoryList(AppCategoryVo appCategoryVo);

    /**
     * 카테고리 정보 조회
     *
     * @param id
     * @return
     */
    public AppCategoryVo getCategory(Long id);

    /**
     * 카테고리 별 푸시목록 조회
     *
     * @param appCategoryVo
     * @return
     */
    public List<PushLogVo> selectCategoryPushList(AppCategoryVo appCategoryVo);


    /**
     * SMS 발송
     *
     * @param map
     * @return
     */
    public int sendPhoneSms(Map<String, Object> map);

    /**
     * SMS 번호 인증 체크
     *
     * @param map
     * @return
     */
    public List<Map<String, Object>> checkPhoneSms(Map<String, Object> map);


    /**
     * 아파트 일반 게시판 등록일 조회
     *
     * @param aptId
     * @return
     */
    public List<Map<String, Object>> selectCommunityBoardSum(Long aptId);

    /**
     * 아파트 일반 게시판 전체 등록일 조회
     *
     * @param aptId
     * @return
     */
    public List<Map<String, Object>> selectCommunityTotalBoardSum(Long aptId);

    /**
     * FAQ 게시판 등록일 조회
     *
     * @param codeGroup
     * @return
     */
    public List<Map<String, Object>> selectFaqBoardSum(String codeGroup);

    /**
     * 마을 뉴스 등록일 조회
     *
     * @param userId
     * @return
     */
    public List<Map<String, Object>> selectMaulNewsBoardSum(Long userId);

    /**
     * 마을뉴스 전체 등록일 조회
     *
     * @param userId
     * @return
     */
    public List<Map<String, Object>> selectMaulNewsTotalBoardSum(Long userId);

    /**
     * 투표 뉴스 등록일 조회
     *
     * @param aptId
     * @return
     */
    public List<Map<String, Object>> selectVoteSum(Long aptId);

    /**
     * 설문 등록일 조회
     *
     * @param aptId
     * @return
     */
    public List<Map<String, Object>> selectPollSum(Long aptId);

    /**
     * 관리비 최종 등록일 조회
     *
     * @param aptId
     * @return
     */
    public List<Map<String, Object>> selectFeeSum(Long aptId);


    /**
     * 방문주차 최종 등록일 조회
     *
     * @return
     */
    public List<Map<String, Object>> selectVisitSum(Map<String, Object> userMap);


}
