/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 10. 6.
 */
package com.jaha.server.emaul.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.jaha.server.emaul.common.code.ErrorCode;
import com.jaha.server.emaul.common.exception.EmaulException;
import com.jaha.server.emaul.constants.Constants;
import com.jaha.server.emaul.mapper.AppMainTemplateMapper;
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.repo.UserRepository;
import com.jaha.server.emaul.util.HtmlUtil;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.v2.constants.CommonConstants.AppMainAlarm;
import com.jaha.server.emaul.v2.mapper.board.BoardCategoryMapper;
import com.jaha.server.emaul.v2.mapper.board.BoardPostMapper;
import com.jaha.server.emaul.v2.mapper.cache.MetroNewsTodayCacheMapper;
import com.jaha.server.emaul.v2.model.app.AppMainTemplateDetailVo;
import com.jaha.server.emaul.v2.model.board.BoardCategoryVo;
import com.jaha.server.emaul.v2.model.board.BoardDto;
import com.jaha.server.emaul.v2.model.cache.MetroNewsTodayCacheVo;

/**
 * <pre>
 * Class Name : AppMainTemplateServiceImpl.java
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
@Service
public class AppMainTemplateServiceImpl implements AppMainTemplateService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Environment env;

    @Autowired
    private AppMainTemplateMapper appMainTemplateMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BoardCategoryMapper boardCategoryMapper;

    @Autowired
    private BoardPostMapper boardPostMapper;

    @Autowired
    private MetroNewsTodayCacheMapper metroNewsTodayCacheMapper;

    static final String APP_MAIN_IMAGE_URL = "/api/public/image-view/%s";

    @SuppressWarnings("unused")
    @Override
    @Transactional
    public Map<String, Object> getMetroNewsList() {
        Map<String, Object> result = new HashMap<String, Object>(); // 결과를 담을 map

        try {
            MetroNewsTodayCacheVo metroNewsTodayCache = this.metroNewsTodayCacheMapper.selectMetroNewsTodayCacheBefore30Minutes();

            String responseString = null;

            if (metroNewsTodayCache == null) {
                RestTemplate restTemplate = new RestTemplate();
                String apiUrl = this.env.getProperty("openapi.data.metro.data.url"); // 메트로주소
                responseString = restTemplate.getForObject(apiUrl, String.class);
                logger.debug("<<API 조회된 매트로 오늘뉴스 정보>> {}", responseString);

                metroNewsTodayCache = new MetroNewsTodayCacheVo();
                metroNewsTodayCache.setTodayNewsJson(responseString);
                this.metroNewsTodayCacheMapper.insertMetroNewsTodayCache(metroNewsTodayCache);
            } else {
                responseString = metroNewsTodayCache.getTodayNewsJson();
                logger.debug("<<DB 조회된 매트로 오늘뉴스 정보>> {}", responseString);
            }

            JSONObject jsonObj = new JSONObject(responseString);
            if (jsonObj == null) {
                logger.info("<<신문정보 없음>> {}", responseString);
                throw new EmaulException(ErrorCode.COMMON_FAIL);
            }

            JSONObject item = null;

            String icon_url = "";
            String reg_date = "";
            String link_url = "";
            String category_name = "";
            String id = "";
            String title = "";
            String content = "";
            String main_image_url = "";

            List<HashMap<String, Object>> main_news = new ArrayList<HashMap<String, Object>>();
            List<HashMap<String, Object>> best_news = new ArrayList<HashMap<String, Object>>();

            // 메인뉴스
            JSONArray mainListJson = jsonObj.getJSONArray("main_news");
            String linkUrl = this.env.getProperty("metro.new.view.url");

            if (mainListJson != null) {
                int mainListLength = mainListJson.length();
                logger.debug("<<1. 메인뉴스 개수 {}>>", mainListLength);
                if (mainListLength > 5) {
                    mainListLength = 5;
                }
                logger.debug("<<2. 메인뉴스 개수 {}>>", mainListLength);

                for (int i = 0; i < mainListLength; i++) {
                    item = mainListJson.getJSONObject(i);
                    main_image_url = String.valueOf(item.get("main_image_url"));
                    reg_date = String.valueOf(item.get("news_app_ndt"));
                    link_url = linkUrl + String.valueOf(item.get("news_cd"));
                    category_name = String.valueOf(item.get("news_cate_nm"));
                    id = String.valueOf(item.get("news_cd"));
                    title = String.valueOf(item.get("news_title"));
                    content = String.valueOf(item.get("news_content"));

                    HashMap<String, Object> mainItem = new HashMap<String, Object>();
                    mainItem.put("icon_url", icon_url);
                    mainItem.put("reg_date", reg_date);
                    mainItem.put("link_url", link_url);
                    mainItem.put("category_name", category_name);
                    mainItem.put("id", id);
                    mainItem.put("title", title);
                    mainItem.put("content", content.length() > 50 ? content.substring(0, 50) : content);
                    mainItem.put("main_image_url", main_image_url);
                    main_news.add(mainItem);
                }
            }

            // 베스트뉴스
            JSONArray bestListJson = jsonObj.getJSONArray("best_news");

            if (bestListJson != null) {
                int bestListLength = bestListJson.length();
                logger.debug("<<1. 베스트뉴스 개수 {}>>", bestListLength);
                if (bestListLength > 2) {
                    bestListLength = 2;
                }
                logger.debug("<<2. 베스트뉴스 개수 {}>>", bestListLength);

                for (int i = 0; i < bestListLength; i++) {
                    item = bestListJson.getJSONObject(i);
                    main_image_url = String.valueOf(item.get("main_image_url"));
                    reg_date = String.valueOf(item.get("news_app_ndt"));
                    link_url = linkUrl + String.valueOf(item.get("news_cd"));
                    category_name = String.valueOf(item.get("news_cate_nm"));
                    id = String.valueOf(item.get("news_cd"));
                    title = String.valueOf(item.get("news_title"));
                    content = String.valueOf(item.get("news_content"));
                    HashMap<String, Object> bestItem = new HashMap<String, Object>();
                    bestItem.put("icon_url", icon_url);
                    bestItem.put("reg_date", reg_date);
                    bestItem.put("link_url", link_url);
                    bestItem.put("category_name", category_name);
                    bestItem.put("id", id);
                    bestItem.put("title", title);
                    bestItem.put("main_image_url", main_image_url);
                    bestItem.put("content", content.length() > 50 ? content.substring(0, 50) : content);
                    best_news.add(bestItem);
                }
            }

            result.put("best_news", best_news);
            result.put("main_news", main_news);
        } catch (Exception e) {
            // 나중에 연결오류, 맵핑 오류 등 정책 정해지면 에러코드, 메시지 등 세부적으로 나눠서 처리
            logger.error("<<메트로 뉴스 API 연동 실패>>", e.getMessage());
            throw new EmaulException(ErrorCode.COMMON_FAIL);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.service.AppMainTemplateService#getMainTemplateList(Long)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMainTemplateList(Long userId) {
        Map<String, Object> resultMap = new HashMap<String, Object>(); // 결과를 담을 map

        try {
            User user = this.userRepository.findOne(userId);
            Long aptId = user.house.apt.id;

            logger.debug("<<메인템플릿 목록 조회>>");
            List<Map<String, Object>> templateMapList = this.appMainTemplateMapper.selectMainTemplateList();
            int bcpIndex = 0;
            int removingIndex = -1;

            if (templateMapList != null && templateMapList.size() > 0) {
                for (Map<String, Object> templateMap : templateMapList) {
                    String codeGroup = (String) templateMap.get("codeGroup");
                    List<Map<String, Object>> templateDetailMapList = new ArrayList<Map<String, Object>>();

                    if ("APP_MAIN_AUTO".equals(codeGroup)) {
                        String code = (String) templateMap.get("code");

                        if ("BEST_COMMUNITY_POST".equals(code) && (user.type.anonymous || user.type.blocked)) {
                            removingIndex = bcpIndex;
                        }

                        templateMap.put("content", templateDetailMapList);
                    } else {
                        Integer mainTemplateId = (int) templateMap.get("id");

                        logger.debug("<<메인템플릿 상세 목록 조회>>");
                        templateDetailMapList = this.appMainTemplateMapper.selectMainTemplateDetailList(mainTemplateId);

                        if (templateDetailMapList != null && templateDetailMapList.size() > 0) {
                            for (Map<String, Object> templateDetailMap : templateDetailMapList) {
                                templateDetailMap.put("imageUrl", this.getAppMainImageUrl((int) templateDetailMap.get("id")));
                            }
                        }

                        templateMap.put("content", templateDetailMapList);
                    }

                    bcpIndex++;
                }

                if (removingIndex > -1) {
                    templateMapList.remove(removingIndex);
                }
            }

            resultMap.put("template", templateMapList);

            logger.debug("<<관리비 등록여부 조회>>");
            String feeRegMonth = this.appMainTemplateMapper.selectFeeRegMonth(userId);
            Map<String, Object> feeMap = new HashMap<String, Object>();
            if (StringUtils.isEmpty(feeRegMonth)) {
                feeMap.put("regYn", "N");
            } else {
                feeMap.put("regYn", "Y");
            }
            feeMap.put("regMonth", feeRegMonth);

            resultMap.put("fee", feeMap);

            Map<String, Object> communityMap = new HashMap<String, Object>();

            if (user.type.anonymous || user.type.blocked) {
                // List<Map<String, Object>> emptyList = new ArrayList<Map<String, Object>>();
                // communityMap.put("recentList", emptyList);
                // communityMap.put("hitList", emptyList);
                // resultMap.put("community", communityMap);
            } else {
                logger.debug("<<커뮤니티 최근순 목록 조회>>");
                List<Map<String, Object>> communityRecentMapList = this.appMainTemplateMapper.selectCommunityRecentList(aptId);
                if (communityRecentMapList != null && communityRecentMapList.size() > 0) {
                    for (Map<String, Object> communityRecentMap : communityRecentMapList) {
                        String title = (String) communityRecentMap.get("title");
                        communityRecentMap.put("title", this.getOnlyText(title));
                    }
                }
                communityMap.put("recentList", communityRecentMapList);

                logger.debug("<<커뮤니티 히트순 목록 조회>>");
                List<Map<String, Object>> communityHitMapList = this.appMainTemplateMapper.selectCommunityHitList(aptId);
                if (communityHitMapList != null && communityHitMapList.size() > 0) {
                    for (Map<String, Object> communityHitMap : communityHitMapList) {
                        String title = (String) communityHitMap.get("title");
                        communityHitMap.put("title", this.getOnlyText(title));
                    }
                }
                communityMap.put("hitList", communityHitMapList);

                resultMap.put("community", communityMap);
            }

        } catch (Exception e) {
            // 나중에 연결오류, 맵핑 오류 등 정책 정해지면 에러코드, 메시지 등 세부적으로 나눠서 처리
            logger.error("<<메인화면 목록 API 조회 오류>>", e);
            throw new EmaulException(ErrorCode.COMMON_FAIL);
        }

        return resultMap;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.service.AppMainTemplateService#getMainNoticeBanner(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMainNoticeBanner(User user, String os) {

        Map<String, Object> resultMap = new HashMap<String, Object>(); // 결과를 담을 map
        List<Map<String, Object>> bodyList = new ArrayList<Map<String, Object>>();

        BoardDto boardDto = new BoardDto();
        boardDto.setDisplayYn("Y");

        try {
            logger.debug("<<메인 알람 목록 조회>>");
            List<Map<String, Object>> mainNoticeTemplate = this.appMainTemplateMapper.selectMainTemplateAlarmList("APP_MENU_NOTI");
            if (mainNoticeTemplate != null && mainNoticeTemplate.size() > 0) {

                for (Map<String, Object> templateMap : mainNoticeTemplate) {

                    String code = (String) templateMap.get("code");
                    logger.debug(">>> code : " + code);

                    if (AppMainAlarm.TOP.getValue().equals(code)) {
                        try {
                            // 아파트 공지사항 TOP_FIX (notice 타입)
                            boardDto.setCategoryType("notice");
                            boardDto.setAptId(user.house.apt.id);
                            boardDto.setTopFix(true);
                            boardDto.setUser(null);
                            boardDto.setDisplayPlatform(null);

                            List<BoardCategoryVo> categoryList = boardCategoryMapper.selectBoardCategoryList(boardDto);
                            List<Long> ids = Lists.transform(categoryList, input -> input.getId());

                            logger.debug(">>> COMMUNITY_TOPFIX categorys : " + categoryList.size());
                            boardDto.setCategoryIds(ids);

                            if (categoryList != null && !categoryList.isEmpty()) {
                                bodyList.addAll(boardPostMapper.selectBoardCategoryPostList(boardDto));
                            }
                        } catch (Exception e) {
                            logger.error(">>> 앱 메인 템플릿 COMMUNITY_TOPFIX Exception", e);
                        }

                    } else if (AppMainAlarm.NOTICE.getValue().equals(code)) {
                        try {
                            // 시스템 전체공지
                            boardDto.setCategoryType("system-notice");
                            boardDto.setAptId(null);
                            boardDto.setTopFix(null);
                            boardDto.setUser(null);
                            boardDto.setDisplayPlatform(os);

                            List<BoardCategoryVo> categoryList = boardCategoryMapper.selectBoardCategoryList(boardDto);
                            List<Long> ids = Lists.transform(categoryList, input -> input.getId());

                            logger.debug(">>> SYSTEM-NOTICE categorys : " + categoryList.size());
                            boardDto.setCategoryIds(ids);

                            if (categoryList != null && !categoryList.isEmpty()) {
                                bodyList.addAll(boardPostMapper.selectBoardCategoryPostList(boardDto));
                            }
                        } catch (Exception e) {
                            logger.error(">>> 앱 메인 템플릿 SYSTEM-NOTICE Exception", e);
                        }

                    } else if (AppMainAlarm.EVENT.getValue().equals(code)) {
                        try {
                            // 이벤트
                            boardDto.setCategoryType("event");
                            boardDto.setAptId(null);
                            boardDto.setTopFix(null);
                            boardDto.setUser(null);
                            boardDto.setDisplayPlatform(os);

                            List<BoardCategoryVo> categoryList = boardCategoryMapper.selectBoardCategoryList(boardDto);
                            List<Long> ids = Lists.transform(categoryList, input -> input.getId());

                            logger.debug(">>> EVENT categorys : " + categoryList.size());
                            boardDto.setCategoryIds(ids);

                            if (categoryList != null && !categoryList.isEmpty()) {
                                bodyList.addAll(boardPostMapper.selectBoardCategoryPostList(boardDto));
                            }
                        } catch (Exception e) {
                            logger.error(">>> 앱 메인 템플릿 EVENT Exception", e);
                        }


                    } else if (AppMainAlarm.GROUP.getValue().equals(code)) {

                        try {
                            // 단체게시판
                            boardDto.setCategoryType("group");
                            boardDto.setAptId(null);
                            boardDto.setTopFix(null);
                            boardDto.setUser(null);
                            boardDto.setDisplayPlatform(null);
                            List<BoardCategoryVo> categoryList = boardCategoryMapper.selectBoardCategoryList(boardDto);
                            List<Long> ids = Lists.transform(categoryList, input -> input.getId());

                            logger.debug(">>> GROUP categorys : " + categoryList.size());
                            boardDto.setCategoryIds(ids);
                            boardDto.setUser(user);
                            logger.debug(">>> 지역 : " + user.house.apt.address.시도명 + " / " + user.house.apt.address.시군구명);
                            if (categoryList != null && !categoryList.isEmpty()) {
                                bodyList.addAll(boardPostMapper.selectBoardCategoryPostList(boardDto));
                            }
                        } catch (Exception e) {
                            logger.error(">>> 앱 메인 템플릿 GROUP Exception", e);
                        }
                    }
                }

                resultMap.put("noti", bodyList);
            } else {
                resultMap.put("noti", null);
            }

            logger.debug("<<메인 배너 목록 조회>>");
            List<Map<String, Object>> mainBannerTemplate = this.appMainTemplateMapper.selectMainTemplateAlarmList("APP_MENU_BANNER");
            if (mainBannerTemplate != null && mainBannerTemplate.size() > 0) {

                String bannerType = "A";
                if ("B_TYPE_BANNER".equals(mainBannerTemplate.get(0).get("code"))) {
                    bannerType = "B";
                }
                resultMap.put("bannerType", bannerType);

                List<String> ids = Lists.transform(mainBannerTemplate, input -> (String) input.get("code"));
                AppMainTemplateDetailVo appMainTemplate = new AppMainTemplateDetailVo();
                appMainTemplate.setCodes(ids);
                resultMap.put("banner", appMainTemplateMapper.selectMainTemplateDetailBannerList(appMainTemplate));

            } else {
                resultMap.put("bannerType", null);
                resultMap.put("banner", null);
            }

        } catch (Exception e) {
            // 나중에 연결오류, 맵핑 오류 등 정책 정해지면 에러코드, 메시지 등 세부적으로 나눠서 처리
            logger.error("<<메인화면 알람 목록 API 조회 오류>>", e);
            throw new EmaulException(ErrorCode.COMMON_FAIL);
        }

        return resultMap;
    }

    /**
     * 메인 이미지 URL을 반환한다.
     *
     * @param fileGroupKey
     * @return
     */
    private String getAppMainImageUrl(int fileGroupKey) {
        if (StringUtil.nvlInt(fileGroupKey) == 0) {
            return StringUtils.EMPTY;
        } else {
            List<FileInfo> fileInfoList = this.commonService.getFileGroup(Constants.FILE_CATEGORY_MAIN_TEMPLATE, fileGroupKey);

            if (fileInfoList == null || fileInfoList.isEmpty()) {
                return StringUtils.EMPTY;
            } else {
                return String.format(APP_MAIN_IMAGE_URL, fileInfoList.get(0).fileKey);
            }
        }
    }

    /**
     * 제목에 html이 있을 경우 텍스트만 골라내 30자까지만 반환한다.
     *
     * @param html
     * @return
     */
    private String getOnlyText(String html) {
        if (StringUtils.isBlank(html)) {
            return "무제";
        }

        html = StringUtils.remove(html, "<!DOCTYPE html>");
        html = HtmlUtil.removeTagAndEntity(html);
        html = StringUtils.replace(html, StringUtils.CR + StringUtils.LF, " ");
        html = StringUtils.abbreviate(html, 30);

        return html;
    }

}
