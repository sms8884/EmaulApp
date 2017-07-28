/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 9. 22.
 */
package com.jaha.server.emaul.v2.service.app;

import java.util.List;
import java.util.Map;

import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.v2.model.app.AppCategoryVo;
import com.jaha.server.emaul.v2.model.app.AppVersionV2Vo;
import com.jaha.server.emaul.v2.model.common.PushLogVo;;

/**
 * <pre>
 * Class Name : AppAdminService.java
 * Description : App관련 서비스
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 10. 22.     조영태      Generation
 * </pre>
 *
 * @author 조영태
 * @since 2016. 10. 22.
 * @version 1.0
 */
public interface AppService {

    /**
     * App 버전 목록
     *
     * @param appVersionV2Vo
     * @return
     * @throws Exception
     */
    List<AppVersionV2Vo> selectAppVersionV2List(AppVersionV2Vo appVersionV2Vo) throws Exception;


    /**
     * App용 카테고리 목록 조회
     *
     * @param appCategoryVo
     * @return
     * @throws Exception
     */
    List<AppCategoryVo> selectCategoryList(AppCategoryVo appCategoryVo) throws Exception;

    /**
     * 카테고리 조회
     *
     * @param id
     * @return
     * @throws Exception
     */
    AppCategoryVo getCategory(Long id) throws Exception;


    /**
     * App용 카테고리 별 푸시리스트 조회
     *
     * @param appCategoryVo
     * @return
     * @throws Exception
     */
    List<PushLogVo> selectCategoryPushList(AppCategoryVo appCategoryVo) throws Exception;


    /**
     * 게시판 new 아이콘 표시용
     *
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> selectBoardSum(List<Map<String, Object>> paramList) throws Exception;


    /**
     * 전체 카테고리 new 아이콘 표시용
     *
     * @param aptId
     * @return
     * @throws Exception
     */
    Map<String, Object> selectBoardCategorySum(User user) throws Exception;



    /**
     * SMS 인증번호 발송
     *
     * @param destNumber
     * @param sendNumber
     * @param msg
     * @param code
     * @param key
     * @return
     * @throws Exception
     */
    int sendPhoneSms(Map<String, Object> map) throws Exception;

    /**
     * SMS 인증번호 체크 및 사용자 정보 수정
     *
     * @param map
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> checkPhoneSms(Map<String, Object> map) throws Exception;


}

