/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 10. 6.
 */
package com.jaha.server.emaul.service;

import java.util.Map;

import com.jaha.server.emaul.model.User;

/**
 * <pre>
 * Class Name : AppMainTemplateService.java
 * Description : Description
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
public interface AppMainTemplateService {

    /**
     * 앱 메인화면 조회
     *
     * @param userId
     * @return
     */
    Map<String, Object> getMainTemplateList(Long userId);

    /**
     * 메인화면 > 자동컨텐츠 > 오늘의 뉴스
     *
     * @return
     */
    Map<String, Object> getMetroNewsList();


    /**
     * 앱 메인 알람영역 / 배너
     *
     * @param user
     * @return
     */
    Map<String, Object> getMainNoticeBanner(User user, String os);

}
