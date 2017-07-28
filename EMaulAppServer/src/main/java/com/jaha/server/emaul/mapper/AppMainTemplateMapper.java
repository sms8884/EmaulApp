/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 10. 6.
 */
package com.jaha.server.emaul.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.v2.model.app.AppMainTemplateDetailVo;

/**
 * <pre>
 * Class Name : AppMainTemplateMapper.java
 * Description : 앱 메인화면 조회
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 10. 6.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 10. 6.
 * @version 1.0
 */
@Mapper
public interface AppMainTemplateMapper {

    /**
     * 메인 템플릿 목록 조회
     *
     * @return
     */
    List<Map<String, Object>> selectMainTemplateList();

    /**
     * 메인 템플릿 상세 목록 조회
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> selectMainTemplateDetailList(Integer mainTemplateId);

    /**
     * 관리비 등록월 조회
     *
     * @param param
     * @return
     */
    String selectFeeRegMonth(Long param);

    /**
     * 커뮤니티 최근등록 순 조회
     *
     * @param param
     * @return
     */
    List<Map<String, Object>> selectCommunityRecentList(Long param);

    /**
     * 커뮤니티 인기(hit) 순 조회
     *
     * @param param
     * @return
     */
    List<Map<String, Object>> selectCommunityHitList(Long param);


    /**
     * 메인 알람 템플릿 조회
     *
     * @return
     */
    List<Map<String, Object>> selectMainTemplateAlarmList(String type);

    /**
     * 메인 배너 상세 조회
     *
     * @param codes
     * @return
     */
    List<Map<String, Object>> selectMainTemplateDetailBannerList(AppMainTemplateDetailVo appMainTemplate);

}
