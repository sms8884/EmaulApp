/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 10. 6.
 */
package com.jaha.server.emaul.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.AppMainTemplateService;
import com.jaha.server.emaul.service.CommonService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.Util;

/**
 * <pre>
 * Class Name : AppMainTemplateController.java
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
@Controller
public class AppMainTemplateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppMainTemplateController.class);

    @Autowired
    private AppMainTemplateService appMainTemplateService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private UserService userService;

    /**
     * 메인화면
     *
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/app-main")
    @ResponseBody
    public ApiResponse<?> getMainTemplateList(HttpServletRequest req) {
        LOGGER.debug("<<메인화면 목록 조회>>");

        Long userId = SessionAttrs.getUserId(req.getSession());

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(this.appMainTemplateService.getMainTemplateList(userId));

        return apiResponse;
    }

    /**
     * 메인화면 > 자동 컨텐츠 > 오늘의 뉴스(메트로신문)
     *
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/app-main/today-news")
    @ResponseBody
    public ApiResponse<?> getTodayNewsList(HttpServletRequest req) {
        LOGGER.debug("<<메인화면 > 자동 컨텐츠 > 오늘의 뉴스(메트로신문) 조회>>");

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(this.appMainTemplateService.getMetroNewsList());

        return apiResponse;
    }

    /**
     * 이미지뷰 컨텐츠가 fileInfo 로저장되어질때 파일그룹키로 가져와야해서 따로 뺴놨습니다
     *
     * @param request
     * @param response
     * @param fileKey
     */
    @RequestMapping(value = "/api/public/app-main/image-view/{fileGroupKey}")
    public void findAndViewImage(HttpServletRequest request, HttpServletResponse response, @PathVariable Long fileGroupKey) {
        try {
            List<FileInfo> fileGroup = commonService.getFileGroup("main", fileGroupKey);
            FileInfo fileInfo = fileGroup.get(0);
            if (fileInfo == null) {
                LOGGER.info("<<공통 이미지 뷰 - FileInfo not retreived!>>");
            } else {
                Util.viewImage(fileInfo, request, response);
            }
        } catch (Exception e) {
            LOGGER.error("<<공통 이미지 뷰 에러>>", e.getMessage());
        }
    }



    /**
     * 알림메세지 카테고리 조회 <br/>
     * TODO: COMMON_CODE, CODE_GROUP을 활용
     *
     * @param request
     * @param os
     * @param category
     * @return
     */
    @RequestMapping(value = "/v2/api/menu/noti-banner", method = RequestMethod.POST)
    public @ResponseBody ApiResponse<?> appMainNoticeBanner(HttpServletRequest req, @RequestParam(value = "os", required = true) String os) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        if (StringUtils.isEmpty(os)) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("코드 구분이 입력되지 않았습니다.");
            return apiResponse;
        }

        apiResponse.setBody(this.appMainTemplateService.getMainNoticeBanner(user, os));

        return apiResponse;
    }

}
