/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 9. 22.
 */
package com.jaha.server.emaul.v2.service.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.v2.mapper.app.AppMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostMapper;
import com.jaha.server.emaul.v2.model.app.AppCategoryVo;
import com.jaha.server.emaul.v2.model.app.AppVersionV2Vo;
import com.jaha.server.emaul.v2.model.common.PushLogVo;

/**
 * <pre>
 * Class Name : AppAdminServiceImpl.java
 * Description : App 관련 어드민 서비스 구현
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
@Service
public class AppServiceImpl implements AppService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private AppMapper appMapper;

    @Autowired
    private BoardPostMapper boardPostMapper;

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.v2.service.app.AppService#selectAppVersionV2List(com.jaha.server.emaul.v2.model.app.AppVersionV2Vo)
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppVersionV2Vo> selectAppVersionV2List(AppVersionV2Vo appVersionV2Vo) throws Exception {

        return this.appMapper.selectAppVersionV2List(appVersionV2Vo);
    }


    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.v2.service.app.AppService#selectCategoryList(com.jaha.server.emaul.v2.model.app.AppCategoryVo)
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppCategoryVo> selectCategoryList(AppCategoryVo appCategoryVo) throws Exception {

        return this.appMapper.selectCategoryList(appCategoryVo);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.v2.service.app.AppService#getCategory(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public AppCategoryVo getCategory(Long id) throws Exception {
        return this.appMapper.getCategory(id);
    }



    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.v2.service.app.AppService#selectCategoryPushList(com.jaha.server.emaul.v2.model.app.AppCategoryVo)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PushLogVo> selectCategoryPushList(AppCategoryVo appCategoryVo) throws Exception {

        return this.appMapper.selectCategoryPushList(appCategoryVo);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.v2.service.app.AppService#selectBoardSum()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> selectBoardSum(List<Map<String, Object>> paramList) throws Exception {
        return this.boardPostMapper.selectBoardSum(paramList);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.v2.service.app.AppService#selectBoardCategorySum(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> selectBoardCategorySum(User user) throws Exception {
        Map<String, Object> bodyList = new HashMap<String, Object>();
        // 일반 게시판 조회 (아파트 기준)
        bodyList.put("community_sum", this.appMapper.selectCommunityBoardSum(user.house.apt.id));
        // 전체 게시판 조회 (아파트 기준)
        bodyList.put("community_total_sum", this.appMapper.selectCommunityTotalBoardSum(user.house.apt.id));
        // FAQ - sub 카테고리 포함
        bodyList.put("faq_sum", this.appMapper.selectFaqBoardSum("FAQ_SUBCATEGORY"));
        // 마을뉴스 - sub 카테고리 포함
        bodyList.put("maulnews_sum", this.appMapper.selectMaulNewsBoardSum(user.id));
        // 마을뉴스 전체
        bodyList.put("maulnews_total_sum", this.appMapper.selectMaulNewsTotalBoardSum(user.id));
        // 투표
        bodyList.put("vote_sum", this.appMapper.selectVoteSum(user.house.apt.id));
        // 설문
        bodyList.put("poll_sum", this.appMapper.selectPollSum(user.house.apt.id));
        // 관리비
        bodyList.put("fee_sum", this.appMapper.selectFeeSum(user.house.apt.id));
        // 방문주차
        Map<String, Object> userMap = new HashMap<String, Object>();
        userMap.put("aptId", user.house.apt.id);
        userMap.put("dong", user.house.dong);
        userMap.put("ho", user.house.ho);
        bodyList.put("visit_sum", this.appMapper.selectVisitSum(userMap));
        return bodyList;
    }



    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.v2.service.app.AppService#sendPhoneSms(java.util.Map)
     */
    @Override
    @Transactional
    public int sendPhoneSms(Map<String, Object> map) throws Exception {
        return this.appMapper.sendPhoneSms(map);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> checkPhoneSms(Map<String, Object> map) throws Exception {
        return this.appMapper.checkPhoneSms(map);
    }

}
