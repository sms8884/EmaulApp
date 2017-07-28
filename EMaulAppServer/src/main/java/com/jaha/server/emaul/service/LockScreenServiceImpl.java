package com.jaha.server.emaul.service;

import java.net.URI;
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
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
import com.jaha.server.emaul.common.code.ErrorCode;
import com.jaha.server.emaul.common.exception.EmaulException;
import com.jaha.server.emaul.constants.Gu;
import com.jaha.server.emaul.mapper.NewsMapper;
import com.jaha.server.emaul.util.HtmlUtil;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.v2.mapper.cache.MetroNewsTodayCacheMapper;
import com.jaha.server.emaul.v2.model.cache.MetroNewsTodayCacheVo;

@Service
public class LockScreenServiceImpl implements LockScreenService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private Environment env;

    @Autowired
    private MetroNewsTodayCacheMapper metroNewsTodayCacheMapper;

    @Override
    public Map<String, Object> getLockScreenNews(Map<String, Object> params) {

        String pageType = StringUtil.nvl(params.get("pageType"));// main, list 현재는 두가지

        if ("main".equals(pageType)) {
            params.put("size", 4);
        } else if ("list".equals(pageType)) {
            params.put("size", 10);
        }

        // 마을뉴스 and 메트로뉴스
        List<Map<String, Object>> newsList = newsMapper.selectLockScreenNewsList(params);

        Map<String, Object> result = new HashMap<String, Object>();// 결과를 담을 map

        if (newsList != null && newsList.size() > 0) {

            String iconUrl = StringUtil.nvl(params.get("iconUrl")) + "/img/news_icon";
            for (int i = 0; i < newsList.size(); i++) {
                Map<String, Object> item = newsList.get(i);
                String new_type = StringUtil.nvl(item.get("news_type"));
                if ("today".equals(new_type)) {
                    item.put("icon_url", iconUrl + "/icon_emaul.png");

                    String content = StringUtil.nvl(item.get("content"));
                    content = HtmlUtil.removeTagAndEntity(content);
                    item.put("content", content);
                } else if ("metro".equals(new_type)) {
                    item.put("icon_url", iconUrl + "/icon_metro.png");
                    item.put("link_url", env.getProperty("metro.new.view.url") + StringUtil.nvl(item.get("id")));

                    // 메트로뉴스 content안의 [! ~~~~ !] 사이의 text는 제거한다. ( 내용에 포함된 이미지 정보 )
                    String content = StringUtil.nvl(item.get("content"));
                    String removeStr1 = "[!";
                    String removeStr2 = "!]";
                    int cnt = StringUtil.countMatches(content, removeStr1);
                    for (int j = 0; j < cnt; j++) {
                        if (content.indexOf(removeStr2) > 0) {
                            // 메트로뉴스에 이미지 json 영역을 잘못표기한곳이 있기도하여 자르는 부분의 index가 있는지 확인후 자름
                            String removeStr = content.substring(content.indexOf(removeStr1), content.indexOf(removeStr2) + removeStr2.length());
                            content = StringUtil.trim(content.replace(removeStr, ""));
                        }
                    }
                    item.put("content", content);
                }

                // content 글자제한둔다
                String content = StringUtil.nvl(item.get("content"));
                int length = 100;
                if (content.length() < length) {
                    length = content.length() - 1;
                    length = length < 0 ? 0 : length;
                }
                content = content.substring(0, length);
                item.put("content", content);
            }

            // pageType이 list면 last token을 결과에 포함
            if ("list".equals(pageType)) {
                Map<String, Object> lastItem = newsList.get(newsList.size() - 1);
                result.put("lastDate", StringUtil.nvl(lastItem.get("reg_date")));// last token
            }
        }

        result.put("newsList", newsList);

        return result;
    }

    @SuppressWarnings("unused")
    @Override
    public Map<String, Object> getMetroNews(Map<String, Object> params) {
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
            List<HashMap<String, Object>> main_news = new ArrayList<HashMap<String, Object>>();
            List<HashMap<String, Object>> best_news = new ArrayList<HashMap<String, Object>>();
            // 메인뉴스
            JSONArray mainListJson = jsonObj.getJSONArray("main_news");
            String linkUrl = env.getProperty("metro.new.view.url");
            if (mainListJson != null) {
                for (int i = 0; i < 4; i++) {
                    item = mainListJson.getJSONObject(i);
                    icon_url = String.valueOf(item.get("main_image_url"));
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
                    main_news.add(mainItem);
                }
            }
            // 베스트뉴스
            JSONArray bestListJson = jsonObj.getJSONArray("best_news");
            if (bestListJson != null) {
                for (int i = 0; i < 2; i++) {
                    item = bestListJson.getJSONObject(i);
                    icon_url = String.valueOf(item.get("main_image_url"));
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

    @Override
    public Map<String, Object> getRiverLevelInformation(Gu gu) {

        URI url = UriComponentsBuilder.newInstance().scheme("http").host(env.getProperty("openapi.seoul.host")).port(8088).path(env.getProperty("openapi.seoul.authkey")).path("/json")
                .path("/ListRiverStageService/1/10/{gu}").build().expand(gu.getCode()).toUri();

        RestTemplate restTemplate = new RestTemplate();
        String json = restTemplate.getForObject(url, String.class);

        return parsingRiverLevelInformation(json);
    }

    private Map<String, Object> parsingRiverLevelInformation(String json) {

        JSONObject root = new JSONObject(json);

        if (root.isNull("ListRiverStageService"))
            throw new EmaulException(ErrorCode.RIVER_LVL_ERROR_82.getCode(), ErrorCode.RIVER_LVL_ERROR_82.getMessage(), new HashMap<String, Object>());

        JSONArray rows = root.getJSONObject("ListRiverStageService").getJSONArray("row");

        List<Map<String, Object>> _rows = Lists.newArrayList();

        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = (JSONObject) rows.get(i);

            Map<String, Object> _row = Maps.newHashMap();
            String riverName = row.getString("RIVER_NAME").replaceAll(" ", StringUtils.EMPTY);
            String warningLevel = row.getString("WARNING_LEVEL");
            String currentLevel = row.getString("CURRENT_LEVEL");
            String planfloodLevel = row.getString("PLANFLOOD_LEVEL");
            String dangerousLevel = row.getString("DANGEROUS_LEVEL");

            _row.put("RIVER_NAME", riverName);
            _row.put("WARNING_LEVEL", warningLevel);
            _row.put("CURRENT_LEVEL", currentLevel);
            _row.put("PLANFLOOD_LEVEL", planfloodLevel);
            _row.put("DANGEROUS_LEVEL", dangerousLevel);
            _row.put("LVL", getLvl(riverName, Doubles.tryParse(warningLevel), Doubles.tryParse(currentLevel), Doubles.tryParse(planfloodLevel), Doubles.tryParse(dangerousLevel)));

            _rows.add(_row);
        }

        Map<String, Object> response = Maps.newHashMap();
        response.put("rows", _rows);

        return response;
    }

    private Map<String, String> getLvl(String riverName, Double warningLvl, Double currentLvl, Double planfloodLvl, Double dangerousLvl) {
        verifyLevelParam(warningLvl, currentLvl, planfloodLvl, dangerousLvl);

        Map<String, String> lvl = Maps.newHashMap();

        if (warningLvl > currentLvl && currentLvl > planfloodLvl) {
            lvl.put("LVL", "ATTENTION");
            lvl.put("LVL_TEXT", String.format("%s 하천 수위 : 관심", riverName));
            lvl.put("LVL_MESSAGE", "비가 계속 올 시, 홍수주의보가 예상됩니다");
        } else if (dangerousLvl > currentLvl && currentLvl > warningLvl) {
            lvl.put("LVL", "CAUTION");
            lvl.put("LVL_TEXT", String.format("%s 하천 수위 : 주의", riverName));
            lvl.put("LVL_MESSAGE", "홍수주의보가 예상됩니다");
        } else if (currentLvl > dangerousLvl) {
            lvl.put("LVL", "CAUTION");
            lvl.put("LVL_TEXT", String.format("%s 하천 수위 : 위험", riverName));
            lvl.put("LVL_MESSAGE", "홍수경보가 예상됩니다");
        } else {
            lvl.put("LVL", "NORMAL");
            lvl.put("LVL_TEXT", StringUtils.EMPTY);
            lvl.put("LVL_MESSAGE", StringUtils.EMPTY);
        }

        return lvl;
    }

    private void verifyLevelParam(Double warningLvl, Double currentLvl, Double planfloodLvl, Double dangerousLvl) {
        Assert.notNull(warningLvl);
        Assert.notNull(currentLvl);
        Assert.notNull(planfloodLvl);
        Assert.notNull(dangerousLvl);
    }


    // 오브젝트를 스트링으로?
    private String jsonStringFromObject(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

}
