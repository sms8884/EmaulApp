/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 10. 26.
 */
package com.jaha.server.emaul.v2.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.CommonService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.RandomKeys;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.v2.constants.CommonConstants.ResponseCode;
import com.jaha.server.emaul.v2.model.app.AppCategoryVo;
import com.jaha.server.emaul.v2.model.app.AppVersionV2Vo;
import com.jaha.server.emaul.v2.model.common.PushLogVo;
import com.jaha.server.emaul.v2.service.app.AppService;

/**
 * <pre>
 * Class Name : AppController.java
 * Description : App 관련 컨트롤러
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 11. 22.     cyt      Generation
 * </pre>
 *
 * @author cyt
 * @since 2016. 11. 22.
 * @version 1.0
 */
@Controller
public class AppController {

    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AppService appService;

    @Autowired
    private CommonService commonService;


    /**
     * 기기별 버전 리스트 조회 (V2 : app_version_v2)
     *
     *
     * @param req
     * @param os
     * @param versionCode
     * @param versionName
     * @param forceYn
     * @param useYn
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/v2/api/app/version/list", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> appVersionList(HttpServletRequest req, @RequestParam(value = "os") String os, @RequestParam(value = "versionCode", required = false) String versionCode,
            @RequestParam(value = "versionName", required = false) String versionName, @RequestParam(value = "forceYn", required = false) String forceYn,
            @RequestParam(value = "useYn", required = false) String useYn) throws JsonProcessingException {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {

            if (StringUtils.isEmpty(os)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("기기정보가 입력되지 않았습니다.");
                return apiResponse;
            } else {

                if (!("android".equalsIgnoreCase(os) || "ios".equalsIgnoreCase(os))) {
                    apiHeader.setResultCode("04");
                    apiHeader.setResultMessage("기기정보 오류 [" + os + "]");
                    return apiResponse;
                }
            }

            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            AppVersionV2Vo appVersion = new AppVersionV2Vo();
            appVersion.setOs(os.toLowerCase());
            appVersion.setVersionCode(versionCode);
            appVersion.setVersionName(versionName);
            appVersion.setForceYn(forceYn);
            appVersion.setUseYn(useYn);

            List<AppVersionV2Vo> appAllVersionList = appService.selectAppVersionV2List(appVersion);

            Map<String, Object> map = new HashMap<String, Object>();
            List<AppVersionV2Vo> topAppVersion = new ArrayList<AppVersionV2Vo>();
            List<AppVersionV2Vo> appVersionList = new ArrayList<AppVersionV2Vo>();

            if (appAllVersionList == null || appAllVersionList.isEmpty()) {
                map.put("topAppVersionList", topAppVersion);
                map.put("appVersionList", appVersionList);
            } else {
                List<AppVersionV2Vo> topVersion = new ArrayList<AppVersionV2Vo>();
                topVersion.add(appAllVersionList.get(0));

                map.put("topAppVersionList", topVersion);
                map.put("appVersionList", appAllVersionList);
            }

            apiResponse.setBody(map);

            apiHeader.setResultCode("00");
            apiHeader.setResultMessage("버전 정보를 조회하였습니다.");
            return apiResponse;

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            logger.debug(">>> App 버전정보 조회 오류 : " + e.getMessage());
            return apiResponse;
        }
    }



    /**
     * 알림메세지 카테고리 조회
     *
     * @param request
     * @param os
     * @param category
     * @return
     */
    @RequestMapping(value = "/v2/api/app/category", method = RequestMethod.POST)
    public @ResponseBody ApiResponse<?> userPushCategoryApi(HttpServletRequest request, @RequestParam(value = "os", required = false) String os,
            @RequestParam(value = "category", required = true) String category) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {
            if (StringUtils.isEmpty(category)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("카테고리 구분이 입력되지 않았습니다.");
                return apiResponse;
            }

            User user = userService.getUser(SessionAttrs.getUserId(request.getSession()));

            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            AppCategoryVo appCategory = new AppCategoryVo();
            appCategory.setOs(os);
            appCategory.setCategory(category);
            appCategory.setUserId(user.id);

            List<AppCategoryVo> categoryList = appService.selectCategoryList(appCategory);

            if (categoryList == null || categoryList.isEmpty()) {
                apiHeader.setResultCode("04");
                apiHeader.setResultMessage("카테고리 정보가 없습니다. [" + category + "]");
                return apiResponse;
            }


            HashMap<String, Object> ret = new HashMap<String, Object>();
            ret.put("categoryList", categoryList);
            apiResponse.setBody(ret);

            apiHeader.setResultCode("00");
            apiHeader.setResultMessage("App 카테고리 정보를 조회하였습니다.");
            return apiResponse;

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            logger.debug(">>> 알림 메세지 카테고리 목록 조회 오류 : " + e.getMessage());
            return apiResponse;
        }

    }


    /**
     * 알림메세지 카테고리별 푸시리스트 조회
     *
     * @param request
     * @param os
     * @param category
     * @return
     */
    @RequestMapping(value = "/v2/api/app/category-list/{id}", method = RequestMethod.POST)
    public @ResponseBody ApiResponse<?> userPushCategoryListApi(HttpServletRequest request, @RequestParam(value = "os", required = false) String os, @PathVariable(value = "id") Long id) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {
            if (id == null) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("카테고리 아이디가 입력되지 않았습니다.");
                return apiResponse;
            }

            User user = userService.getUser(SessionAttrs.getUserId(request.getSession()));
            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            AppCategoryVo appCategory = appService.getCategory(id);
            if (appCategory == null) {
                apiHeader.setResultCode("04");
                apiHeader.setResultMessage("카테고리 정보가 없습니다. [" + id + "]");
                return apiResponse;
            }
            appCategory.setUserId(user.id);

            List<PushLogVo> pushList = appService.selectCategoryPushList(appCategory);

            // 운영서버, 개발서버별 포멧이 상이함
            // DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
            SimpleDateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일 (E요일)", Locale.KOREAN);
            // 날자별 그룹 처리
            String viewGroup = ""; // 날자 그룹별 임시 스트링
            HashMap<String, Object> ret = new HashMap<String, Object>(); // 날자 그룹별 Map
            List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>(); // 날자 그룹별 Map 저장 List
            List<PushLogVo> pList = new ArrayList<PushLogVo>(); // push 리스트 저장용 임시 리스트

            for (int i = 0; i < pushList.size(); i++) {
                PushLogVo p = pushList.get(i);

                if (pushList.size() > 1) {

                    if (i == pushList.size() - 1) {
                        // 마지막
                        if (viewGroup.equals(df.format(p.getRegDate()).toString())) {
                            pList.add(p);
                            ret.put("dateGroup", viewGroup);
                            ret.put("dateList", pList);
                            listMap.add(ret);
                        } else {

                            ret.put("dateGroup", viewGroup);
                            ret.put("dateList", pList);
                            listMap.add(ret);
                            // 객체 초기화
                            viewGroup = df.format(p.getRegDate()).toString();
                            pList = new ArrayList<PushLogVo>();
                            ret = new HashMap<String, Object>();
                            pList.add(p);
                            ret.put("dateGroup", viewGroup);
                            ret.put("dateList", pList);
                            listMap.add(ret);
                        }

                    } else if (i == 0) {
                        // 처음
                        viewGroup = df.format(p.getRegDate()).toString();
                        pList.add(p);
                    } else {
                        if (viewGroup.equals(df.format(p.getRegDate()).toString())) {
                            pList.add(p);
                        } else {

                            ret.put("dateGroup", viewGroup);
                            ret.put("dateList", pList);
                            listMap.add(ret);
                            // 객체 초기화
                            viewGroup = df.format(p.getRegDate()).toString();
                            pList = new ArrayList<PushLogVo>();
                            ret = new HashMap<String, Object>();
                            pList.add(p);
                        }
                    }
                } else {
                    // 1건
                    ret.put("dateGroup", df.format(p.getRegDate()).toString());
                    ret.put("dateList", pushList);
                    listMap.add(ret);
                }

            }

            HashMap<String, Object> body = new HashMap<String, Object>();
            body.put("pushList", listMap);

            apiResponse.setBody(body);
            apiHeader.setResultCode("00");
            apiHeader.setResultMessage("App 카테고리별 푸시 정보를 조회하였습니다.");
            return apiResponse;

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            logger.debug(">>> 알림 메세지 카테고리별 푸시 정보 조회 오류 : " + e.getMessage());
            return apiResponse;
        }

    }



    /**
     * 게시판 New Icon 조회용 게시판 카테고리 공통 코드 조회 <br/>
     * Code_group :
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/v2/api/app/board-new-category", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> findAppBoardNewIconCategory(HttpServletRequest req) {
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();

        try {

            // 2016.11.28 운영 반영 전 APP_NEW_CATEGORY 기초 데이터 입력 필요
            Map<String, Object> bodyList = new HashMap<String, Object>();
            CommonCode code = new CommonCode();
            code.setCodeGroup("APP_NEW_CATEGORY");
            code.setUseYn("Y");
            bodyList.put("category", commonService.getCodeList(code));
            apiResponse.setHeader(new ApiResponseHeader(ResponseCode.SUCCESS));
            apiResponse.setBody(bodyList);

        } catch (Exception e) {
            logger.error("<<게시판 카테고리 목록 조회 중 오류발생 (new icon표기용) >>", e);
            apiResponse.setHeader(new ApiResponseHeader(ResponseCode.FAIL));
        }

        return apiResponse;
    }


    /**
     * 게시판 최종 등록일 조회 [new icon용] <br/>
     * { "categoryType": "event", "lastRegDate": "2016-11-24 13:08:48" }, { "categoryType": "faq", "lastRegDate": "2016-11-24 13:08:48" } <br />
     * App의 카테고리 정보가 어차피 하드코딩으로 분기되야 하는 부분들이 있어서 <br/>
     * 데이터를 수신하지 않고 전체 데이터를 내리도록 변경 <br/>
     * 데이터 소비가 큰 작업인 관게로 App이 올라올때 1번만 호출하도록 App 작업 필요
     *
     * @param req
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/api/app/board-regdate", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> appBoardNewIcon(HttpServletRequest req, @RequestParam(value = "json", required = false) String json) throws IOException {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        Map<String, Object> bodyList = new HashMap<String, Object>();

        try {

            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            if (user.type.deactivated) {
                apiHeader.setResultCode("06");
                apiHeader.setResultMessage("탈퇴한 회원입니다.");
                return apiResponse;
            }

            if (user.type.blocked) {
                apiHeader.setResultCode("07");
                apiHeader.setResultMessage("차단된 회원입니다.");
                return apiResponse;
            }

            /**
             * 개발 적용 방식이 변경되면서 service가 나뉘어져 생성됨.
             */


            // faq, event, group, community 전체, maulnews 전체
            List<Map<String, Object>> paramList = new ArrayList<Map<String, Object>>();
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("categoryType", "event");
            paramList.add(map);
            map = new HashMap<String, Object>();
            map.put("categoryType", "group");
            paramList.add(map);
            map = new HashMap<String, Object>();
            map.put("categoryType", "system-notice");
            paramList.add(map);
            map = new HashMap<String, Object>();
            map.put("categoryType", "faq");
            paramList.add(map);

            bodyList.put("board_sum", appService.selectBoardSum(paramList));
            // 투표, 설문, faq 서브카테고리, community board category별, 마을뉴스 subcategory별 조회
            bodyList.putAll(appService.selectBoardCategorySum(user));

            apiResponse.setBody(bodyList);
            apiHeader.setResultCode("00");
            apiHeader.setResultMessage("조회 성공.");
            return apiResponse;

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            logger.debug(">>> 게시판 등록일 조회 중 오류 : " + e.getMessage());
            return apiResponse;
        }
    }



    /**
     * 공통 코드 조회<br/>
     * #################################<br/>
     * 로그인 관련 로직이 필요한 경우 사용하면 안된다. <br/>
     * #################################<br/>
     * 앱실드 미실행 폰 리스트
     *
     * @param request
     * @param os
     * @param category
     * @return
     */
    @RequestMapping(value = "/v2/api/public/code-list", method = RequestMethod.POST)
    public @ResponseBody ApiResponse<?> commonCodeApi(HttpServletRequest request, @RequestParam(value = "codeGroup", required = true) String codeGroup) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {
            if (StringUtils.isEmpty(codeGroup)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("코드 구분이 입력되지 않았습니다.");
                return apiResponse;
            }

            HashMap<String, Object> ret = new HashMap<String, Object>();

            CommonCode code = new CommonCode();
            code.setCodeGroup(codeGroup);
            code.setUseYn("Y");
            ret.put("codeList", commonService.getCodeList(code));

            apiResponse.setBody(ret);
            apiHeader.setResultCode("00");
            apiHeader.setResultMessage("App 카테고리 정보를 조회하였습니다.");
            return apiResponse;

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            logger.debug(">>> 코드 목록 조회 오류 : " + e.getMessage());
            return apiResponse;
        }

    }



    /**
     * 인증번호 요청
     *
     * @param json
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/v2/api/public/phone-auth/req", method = RequestMethod.POST)
    public @ResponseBody ApiResponse<?> phoneAuthReqV2(HttpServletRequest request, @RequestParam(value = "phoneNumber", required = true) String phoneNumber) throws Exception {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {
            if (StringUtils.isEmpty(phoneNumber)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("휴대폰 번호가 입력되지 않았습니다.");
                return apiResponse;
            }

            if (!phoneNumber.matches("(01[016789])(\\d{3,4})(\\d{4})")) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("휴대폰 데이터 오류 [" + phoneNumber + "]");
                return apiResponse;
            }

            String code = String.format("%06d", (int) (Math.random() * 1000000));
            String key = RandomKeys.make(32);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uniqueKey", System.currentTimeMillis() + RandomKeys.make(6));
            map.put("destNumber", phoneNumber);
            // 발신자번호는 비즈뿌리오에 사전 등록된 번호만 문자 전송이 가능함
            map.put("sendNumber", "028670816");
            map.put("msg", String.format("e마을 인증번호 [%s]를 입력해주세요.", code));
            map.put("code", code);
            map.put("key", key);

            appService.sendPhoneSms(map);

            apiResponse.setBody(map);
            apiHeader.setResultCode("00");
            apiHeader.setResultMessage("SMS가 발송되었습니다.");
            return apiResponse;

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            logger.debug(">>> 휴대폰 SMS 인증번호 발송 오류 : " + e.getMessage());
            return apiResponse;
        }
    }

    /**
     * 휴대폰 인증번호 체크 및 사용자 생년, 성별 정보 수정
     *
     * @param req
     * @param code
     * @param key
     * @param birth
     * @param gender
     * @return
     */
    @RequestMapping(value = "/v2/api/public/phone-auth/check", method = RequestMethod.POST)
    public @ResponseBody ApiResponse<?> phoneAuthCheckV2(HttpServletRequest req, @RequestParam(value = "code", required = true) String code, @RequestParam(value = "key", required = true) String key,
            @RequestParam(value = "birth", required = false) String birth, @RequestParam(value = "gender", required = false) String gender) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();
        try {
            if (StringUtils.isEmpty(code)) {
                apiHeader.setResultCode("51");
                apiHeader.setResultMessage("코드 정보 입력 오류");
                logger.debug(">>> 코드 정보 오류");
                return apiResponse;
            }

            if (StringUtils.isEmpty(key)) {
                apiHeader.setResultCode("52");
                apiHeader.setResultMessage("키 정보 입력 오류");
                logger.debug(">>> 키 정보 오류");
                return apiResponse;
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", code);
            map.put("key", key);

            List<Map<String, Object>> result = appService.checkPhoneSms(map);

            if (result == null || result.isEmpty()) {
                apiHeader.setResultCode("53");
                apiHeader.setResultMessage("인증번호 검증 실패");
                logger.debug(">>> 인증번호 검증 실패");
                return apiResponse;
            } else {
                apiHeader.setResultCode("00");
                apiHeader.setResultMessage("인증번호 검증 성공");
                logger.debug(">>> 인증번호 검증 성공");
            }

            if (StringUtils.isNotEmpty(birth) && StringUtils.isNotEmpty(gender)) {
                // 생년 / 성별이 입력되지 않으면 휴대폰 인증 체크만 수행한다.

                if (birth == null || birth.trim().length() != 4) {
                    apiHeader.setResultCode("02");
                    apiHeader.setResultMessage("년도 정보 입력 오류");
                    logger.debug(">>> 입력데이터 오류 : birth" + birth == null ? "" : " : [" + birth.trim() + "]");
                    return apiResponse;
                }

                try {
                    Long.parseLong(birth.trim());
                } catch (Exception e) {
                    // 숫자형 데이터 오류
                    apiHeader.setResultCode("03");
                    apiHeader.setResultMessage("년도 정보 입력 오류 : [" + birth + "]");
                    logger.debug(">>> 입력데이터 오류 : " + birth);
                    return apiResponse;
                }

                if (gender == null) {
                    apiHeader.setResultCode("04");
                    apiHeader.setResultMessage("성별 정보 입력 오류");
                    logger.debug(">>> 입력데이터 오류 : gender");
                    return apiResponse;
                }


                if (!("female".equalsIgnoreCase(gender) || "male".equalsIgnoreCase(gender))) {
                    apiHeader.setResultCode("05");
                    apiHeader.setResultMessage("성별 정보 입력 오류 [" + gender + "]");
                    logger.debug(">>> 입력데이터 오류 : " + gender);
                    return apiResponse;
                }

                try {
                    User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

                    if (user == null || user.id == null) {
                        apiHeader.setResultCode("06");
                        apiHeader.setResultMessage("사용자가 존재하지 않습니다.");
                        logger.debug(">>> 사용자가 존재하지 않습니다.");
                        return apiResponse;
                    } else {
                        if (user.type.deactivated) {
                            apiHeader.setResultCode("07");
                            apiHeader.setResultMessage("탈퇴한 사용자.");
                            logger.debug(">>> 탈퇴한 사용자.");
                            return apiResponse;
                        } else {

                            logger.debug(">>> 생년월일 : " + birth + " / 성별 : " + gender);
                            this.userService.modifyUserAddInfo(user.id, birth, gender.toLowerCase());
                            apiHeader.setResultCode("00");
                            apiHeader.setResultMessage("고객 생년, 성별 정보 수정 포함하여 인증번호 검증 성공.");
                            return apiResponse;
                        }
                    }
                } catch (Exception e) {
                    apiHeader.setResultCode("09");
                    apiHeader.setResultMessage("사용자 정보 조회 오류 (로그인 여부 확인)");
                    logger.debug(">>> 사용자 정보 조회 오류 : " + e.getMessage());
                    return apiResponse;
                }
            } else {
                return apiResponse;
            }

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            logger.debug(">>> 휴대폰 SMS 인증번호 검증 오류 : " + e.getMessage());
            return apiResponse;
        }

    }


}
