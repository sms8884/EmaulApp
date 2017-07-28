package com.jaha.server.emaul.service;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Maps;
import com.jaha.server.emaul.common.code.ErrorCode;
import com.jaha.server.emaul.common.exception.EmaulException;
import com.jaha.server.emaul.mapper.WeatherMapper;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.Pollution;
import com.jaha.server.emaul.model.SimpleAddress;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.Weather;
import com.jaha.server.emaul.repo.PollutionRepository;
import com.jaha.server.emaul.repo.SimpleAddressRepository;
import com.jaha.server.emaul.util.GeoPoint;
import com.jaha.server.emaul.util.GeoTrans;
import com.jaha.server.emaul.util.Locations;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.util.Util;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAction;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAlarmSetting;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushGubun;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushMessage;
import com.jaha.server.emaul.v2.util.PushUtils;

@Service
public class WeatherServiceImpl implements WeatherService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    // @Autowired
    // private GcmService gcmService;

    @Autowired
    private WeatherMapper weatherMapper;

    @Autowired
    private PollutionRepository pollutionRepository;

    @Autowired
    private Environment env;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private SimpleAddressRepository simpleAddressRepository;

    // [START] 광고 푸시 추가 by realsnake 2016.10.28
    @Autowired
    private PushUtils pushUtils;
    // [END]

    // =========================================================================
    // 대기오염정보
    // =========================================================================

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.service.WeatherService#getPollution(java.lang.String)
     */
    @Override
    public Pollution getPollution(String sido) {

        // 기준일자
        String baseDate = getDate("yyyy-MM-dd");

        // 기준일자 데이터 조회
        Pollution pollution = pollutionRepository.findByBaseDate(baseDate);
        if (pollution == null) {
            logger.info("대기오염 정보 없음 [{}]", baseDate);
            return null;
        }

        String strInformGrade = "";

        String[] arrInformGrade = pollution.getInformGrade().replace(" ", "").split(",");
        String[] arrSido = convertSidoName(sido, false);

        if (arrSido == null || arrSido[0].isEmpty()) {
            logger.info("시도 정보 없음 [{}]", sido);
            return null;
        }

        for (String informGrade : arrInformGrade) {
            for (String tmpSido : arrSido) {
                if (informGrade.startsWith(tmpSido)) {
                    if (!strInformGrade.isEmpty()) {
                        strInformGrade += ", ";
                    }
                    strInformGrade += informGrade.replace(":", " ").trim();
                    if (arrSido.length == 1) {
                        strInformGrade = strInformGrade.replace(tmpSido, "");
                    }
                }

            }
        }
        pollution.setInformGrade("미세먼지 " + sido + " " + strInformGrade);

        return pollution;
    }

    private String getNearStation(String addr) {

        Locations.LatLng latLng = Locations.getLocationFromAddress(addr);

        GeoPoint addrGeoPoint = new GeoPoint(latLng.lng, latLng.lat);
        GeoPoint convertedGeoPoint = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, addrGeoPoint);

        // 서비스키
        String serviceKey = env.getProperty("openapi.data.service.key.general");

        // 실시간 대기오염 조회 URL
        String currentPollutionServiceUrl = env.getProperty("openapi.data.pollution.station.service.url");

        String url = currentPollutionServiceUrl + "?ServiceKey=" + getDecString(serviceKey) + "&tmX=" + convertedGeoPoint.getX() + "&tmY=" + convertedGeoPoint.getY() + "&page=1" + "&numOfRows=10"
                + "&ver=1.3";

        JSONObject jsonObject = callAPI(url, MediaType.APPLICATION_XML);
        JSONArray items = getItems(jsonObject, POLLUTION_SUCCESS_CODE);

        String stationName = "";

        if (items != null) {
            JSONObject item = items.getJSONObject(0);
            stationName = item.getString("stationName");
        }

        return stationName;
    }

    @Override
    public Map<String, String> getCurrentPollution(String addr) {

        Map<String, String> pollutionCache = getCurrentPollutionCache(addr);

        if (pollutionCache != null)
            return pollutionCache;

        String stationName = getNearStation(addr);

        // 서비스키
        String serviceKey = env.getProperty("openapi.data.service.key.general");

        // 실시간 대기오염 조회 URL
        String currentPollutionServiceUrl = env.getProperty("openapi.data.current.pollution.service.url");

        String url = currentPollutionServiceUrl + "?ServiceKey=" + getDecString(serviceKey) + "&stationName=" + stationName + "&dataTerm=daily" + "&page=1" + "&numOfRows=10" + "&ver=1.3";


        JSONObject jsonObject = callAPI(url, MediaType.APPLICATION_XML);
        JSONArray items = getItems(jsonObject, POLLUTION_SUCCESS_CODE);

        Map<String, String> pollution = Maps.newHashMap();

        if (items != null) {
            JSONObject item = items.getJSONObject(0);

            pollution.put("dataTime", item.getString("dataTime"));
            pollution.put("pm10Value", String.valueOf(item.get("pm10Value")));
            pollution.put("pm10Value24", String.valueOf(item.get("pm10Value24")));
            pollution.put("pm25Value", String.valueOf(item.get("pm25Value")));
            pollution.put("pm25Value24", String.valueOf(item.get("pm25Value24")));
            pollution.put("pm10Grade", String.valueOf(item.get("pm10Grade")));
            pollution.put("pm25Grade", String.valueOf(item.get("pm25Grade")));
        }

        Map<String, String> _pollution = Maps.newHashMap(pollution);
        _pollution.put("addr", addr);

        // cache 테이블 저장
        weatherMapper.insertPollutionCache(_pollution);

        return pollution;
    }

    private Map<String, String> getCurrentPollutionCache(String addr) {
        Map<String, String> previousDataTimeParam = Maps.newHashMap();
        previousDataTimeParam.put("addr", addr);
        previousDataTimeParam.put("dataTime", getPreviousDate());

        Map<String, String> previousPollutionCache = weatherMapper.getPollutionCache(previousDataTimeParam);
        if (previousPollutionCache != null && !previousPollutionCache.isEmpty()) {
            previousPollutionCache.put("dataTime", previousPollutionCache.get("data_time"));
            previousPollutionCache.put("pm10Value", previousPollutionCache.get("pm10_value"));
            previousPollutionCache.put("pm10Value24", previousPollutionCache.get("pm10_value24"));
            previousPollutionCache.put("pm25Value", previousPollutionCache.get("pm25_value"));
            previousPollutionCache.put("pm25Value24", previousPollutionCache.get("pm25_value24"));
            previousPollutionCache.put("pm10Grade", previousPollutionCache.get("pm10_grade"));
            previousPollutionCache.put("pm25Grade", previousPollutionCache.get("pm25_grade"));

            previousPollutionCache.remove("data_time");
            previousPollutionCache.remove("pm10_value");
            previousPollutionCache.remove("pm10_value24");
            previousPollutionCache.remove("pm25_value");
            previousPollutionCache.remove("pm25_value24");
            previousPollutionCache.remove("pm10_grade");
            previousPollutionCache.remove("pm25_grade");
        }

        Map<String, String> dataTimeParam = Maps.newHashMap();
        dataTimeParam.put("addr", addr);
        dataTimeParam.put("dataTime", getCurrentDate());

        Map<String, String> pollutionCache = weatherMapper.getPollutionCache(dataTimeParam);
        if (pollutionCache != null && !pollutionCache.isEmpty()) {
            pollutionCache.put("dataTime", pollutionCache.get("data_time"));
            pollutionCache.put("pm10Value", pollutionCache.get("pm10_value"));
            pollutionCache.put("pm10Value24", pollutionCache.get("pm10_value24"));
            pollutionCache.put("pm25Value", pollutionCache.get("pm25_value"));
            pollutionCache.put("pm25Value24", pollutionCache.get("pm25_value24"));
            pollutionCache.put("pm10Grade", pollutionCache.get("pm10_grade"));
            pollutionCache.put("pm25Grade", pollutionCache.get("pm25_grade"));

            pollutionCache.remove("data_time");
            pollutionCache.remove("pm10_value");
            pollutionCache.remove("pm10_value24");
            pollutionCache.remove("pm25_value");
            pollutionCache.remove("pm25_value24");
            pollutionCache.remove("pm10_grade");
            pollutionCache.remove("pm25_grade");
        }

        if (previousPollutionCache == null && pollutionCache == null)
            return null;
        else if (previousPollutionCache != null && pollutionCache == null)
            return previousPollutionCache;
        else if (previousPollutionCache == null && pollutionCache != null)
            return pollutionCache;
        else
            return pollutionCache;
    }

    private String getPreviousDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00");

        Date currentDate = new Date();
        Date previousHourDate = new Date(currentDate.getTime() - 1000 * 60 * 60);
        return sdf.format(previousHourDate);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00");
        Date currentDate = new Date();
        return sdf.format(currentDate);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.service.WeatherService#callPollution()
     */
    @Override
    @Transactional
    public void callPollution() {

        // 서비스키
        String serviceKey = env.getProperty("openapi.data.service.key.general");

        // 대기오염 조회 URL
        String pollutionServiceUrl = env.getProperty("openapi.data.pollution.service.url");

        // 기준일자
        String baseDate = getDate("yyyy-MM-dd");

        Pollution tmpPollution = pollutionRepository.findByBaseDate(baseDate);

        // 기준일자 데이터가 있을 경우 API 호출하지 않음
        if (tmpPollution == null) {

            String url = pollutionServiceUrl + "?ServiceKey=" + getDecString(serviceKey) + "&searchDate=" + baseDate + "&InformCode=PM10";

            JSONObject jsonObject = callAPI(url, MediaType.APPLICATION_XML);
            JSONArray items = getItems(jsonObject, POLLUTION_SUCCESS_CODE);

            if (items != null) {

                JSONObject item = items.getJSONObject(0);

                Pollution pollution = new Pollution();
                pollution.setBaseDate(baseDate);
                pollution.setDataTime(item.getString("dataTime"));
                pollution.setInformCode(item.getString("informCode"));
                pollution.setInformOverall(item.getString("informOverall"));
                pollution.setInformCause(item.getString("informCause"));
                pollution.setInformGrade(item.getString("informGrade"));
                pollution.setInformData(item.getString("informData"));
                pollution.setPushYn("N");
                pollution.setRegDt(new Date());
                pollutionRepository.save(pollution);
                logger.info("대기오염 정보 등록 [{}]", baseDate);

            }

        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.service.WeatherService#pushPollution()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> pushPollution() {

        // 기준일자
        String baseDate = getDate("yyyy-MM-dd");

        // 대기오염정보 조회
        Pollution pollution = pollutionRepository.findByBaseDateAndPushYn(baseDate, "N");

        if (pollution != null) {
            // 대기오염 예보 등급 중 나쁨 포함 여부
            if (pollution.getInformGrade().indexOf("나쁨") > -1) {

                List<String> badList = new ArrayList<>();
                String[] arrInformGrade = pollution.getInformGrade().replace(" ", "").split(",");

                for (String informGrade : arrInformGrade) {
                    if (informGrade.indexOf("나쁨") > -1) {
                        String shotSidoName = informGrade.split(":")[0].trim(); // :: 서울
                        String[] fullSidoName = convertSidoName(shotSidoName, true); // :: 서울특별시
                        badList.add(fullSidoName[0]);
                    }
                }

                List<Map<String, Object>> list = new ArrayList<>();
                Map<String, Object> map = null;

                String title = "";
                String message = "";

                for (String sido : badList) {

                    title = getInformGradeText(arrInformGrade, sido);
                    // message = "e마을\n" + title + "\n\n" + pollution.getInformOverall() + "\n\n" + pollution.getDataTime();
                    message = String.format(PushMessage.WEATHER_ALERT_BODY.getValue(), pollution.getInformOverall(), pollution.getDataTime());

                    List<SimpleUser> targetUserList = this.pushUtils.findPushTargetUserList(PushAlarmSetting.AIR_POLLUTION, sido, null, null, null);

                    map = new HashMap<>();
                    map.put("sido", sido);
                    map.put("title", StringUtil.nvl(title));
                    // map.put("userIds", weatherMapper.selectPollutionPushList(sido));
                    map.put("targetUserList", targetUserList);
                    map.put("message", message);
                    list.add(map);
                }

                // PUSH 메시지 발송
                if (list != null && !list.isEmpty()) {

                    // Map<String, String> msg = null;
                    // GcmSendForm form = null;

                    // String title = PushMessage.WEATHER_ALERT_TITLE.getValue();
                    String action = PushAction.WEATHER_ALERT.getValue();

                    for (Map<String, Object> tmpMap : list) {

                        // msg = Maps.newHashMap();
                        // msg.put("type", "action");
                        // msg.put("titleResId", "push_title_res_id_fine_dust");
                        // msg.put("action", "emaul://weather-alert");
                        // msg.put("title", String.valueOf(tmpMap.get("title"))); // title
                        // msg.put("value", String.valueOf(tmpMap.get("message"))); // message
                        //
                        // form = new GcmSendForm();
                        // form.setMessage(msg);
                        // form.setUserIds((List<Long>) tmpMap.get("userIds"));
                        //
                        // gcmService.sendGcm(form);

                        title = StringUtil.nvl(tmpMap.get("title"));
                        String value = (String) tmpMap.get("message");

                        this.pushUtils.sendPush(PushGubun.AIR_POLLUTION, title, value, action, String.valueOf(pollution.getSeq()), false, (List<SimpleUser>) tmpMap.get("targetUserList"));
                    }

                    // UPDATE pollution
                    pollutionRepository.setPushYn(pollution.getSeq());

                }

            }
        }
        return null;
    }


    // =========================================================================
    // 날씨정보
    // =========================================================================

    @Override
    public Weather getWeather(Double lat, Double lng) {

        // 서비스키
        String serviceKey = env.getProperty("openapi.data.service.key.general");

        // 초단기실황조회 URL
        String forecastGribUrl = env.getProperty("openapi.data.forecast.grib.url");

        // 동네예보조회 URL
        String forecastSpaceDataUrl = env.getProperty("openapi.data.forecast.space.data.url");

        Map<String, Object> map = convertLatlngToNxy(lat, lng);
        String nx = String.valueOf(map.get("nx"));
        String ny = String.valueOf(map.get("ny"));

        Map<String, String> baseMap = getWeatherBaseTime();

        String baseDate = baseMap.get("baseDate"); // 기준일자(yyyyMMdd)
        String baseTime = baseMap.get("baseTime"); // 기준시간(HHmm)
        // String prevDate = baseMap.get("prevDate"); // 전날(yyyyMMdd)
        String nextDate = baseMap.get("nextDate"); // 다음날(yyyyMMdd)

        // 초단기실황조회 URL
        String gribUrl = forecastGribUrl + "?ServiceKey=" + getDecString(serviceKey) + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny + "&pageNo=1" + "&numOfRows=999";

        // 동네예보조회 URL
        String spaceDataUrl = forecastSpaceDataUrl + "?ServiceKey=" + getDecString(serviceKey) + "&base_date=" + baseDate + "&base_time=0200"
        // + "&base_time=" + baseTime
                + "&nx=" + nx + "&ny=" + ny + "&pageNo=1" + "&numOfRows=999";

        JSONObject jsonObject = null;
        JSONArray items = null;
        JSONObject item = null;

        Map<String, String> weatherMap = new HashMap<>();
        Map<String, String> rainfallMap = Maps.newHashMap();
        Map<String, String> snowfallMap = Maps.newHashMap();

        // 초단기실황조회
        jsonObject = callAPI(gribUrl, MediaType.APPLICATION_JSON);

        items = getItems(jsonObject, WEATHER_SUCCESS_CODE);

        if (items == null) {

            // 공공데이터포탈 API 통신 오류
            logger.error("### 초단기실황조회 > jsonObject: {}", jsonObject);
            throw new EmaulException(ErrorCode.WEATHER_ERROR_91, new Weather());

        } else {

            String itemBaseDate = "";
            String itemBaseTime = "";
            String itemCategory = "";
            String itemValue = "";
            String key = "";

            for (int i = 0; i < items.length(); i++) {
                item = items.getJSONObject(i);

                itemBaseDate = String.valueOf(item.get("baseDate"));
                itemBaseTime = String.valueOf(item.get("baseTime"));
                itemCategory = String.valueOf(item.get("category"));
                itemValue = String.valueOf(item.get("obsrValue"));

                key = FORECAST_GRIB_PREFIX + "_" + itemBaseDate + "_" + itemBaseTime + "_" + itemCategory;
                weatherMap.put(key, itemValue);

            }
        }

        // 동네예보조회
        jsonObject = callAPI(spaceDataUrl, MediaType.APPLICATION_JSON);
        // logger.debug("jsonData : {}", jsonObject.toString());

        items = getItems(jsonObject, WEATHER_SUCCESS_CODE);

        if (items == null) {

            // 공공데이터포탈 API 통신 오류
            logger.error("### 동네예보조회 > jsonObject: {}", jsonObject);
            throw new EmaulException(ErrorCode.WEATHER_ERROR_91, new Weather());

        } else {

            String itemFcstDate = "";
            String itemFcstTime = "";
            String itemCategory = "";
            String itemValue = "";
            String key = "";

            for (int i = 0; i < items.length(); i++) {

                item = items.getJSONObject(i);
                itemFcstDate = String.valueOf(item.get("fcstDate"));
                itemFcstTime = String.valueOf(item.get("fcstTime"));
                itemCategory = String.valueOf(item.get("category"));
                itemValue = String.valueOf(item.get("fcstValue"));

                key = FORECAST_SPACE_PREFIX + "_" + itemFcstDate + "_" + itemFcstTime + "_" + itemCategory;
                weatherMap.put(key, itemValue);

                if ("S06".equals(itemCategory))
                    snowfallMap.put(itemFcstDate + itemFcstTime, itemValue);
                else if ("R06".equals(itemCategory))
                    rainfallMap.put(itemFcstDate + itemFcstTime, itemValue);
            }

        }

        /*
         * KEY = FORECAST_GRIB_PREFIX + "_" + baseDate + "_" + baseTime + "_" + itemCategory
         *
         * CATEGORY CODE
         *
         * T1H - 기온 SKY - 하늘상태 PTY - 강수형태
         *
         */
        String current = weatherMap.get(FORECAST_GRIB_PREFIX + "_" + baseDate + "_" + baseTime + "_" + "T1H"); // 현재기온
        String tmpSky = weatherMap.get(FORECAST_GRIB_PREFIX + "_" + baseDate + "_" + baseTime + "_" + "SKY"); // 기상상태
        String tmpPty = weatherMap.get(FORECAST_GRIB_PREFIX + "_" + baseDate + "_" + baseTime + "_" + "PTY"); // 강수상태

        /*
         *
         * KEY = FORECAST_SPACE_PREFIX + "_" + xxxxDate + "_" + xxxxTime + "_" + category
         *
         * CATEGORY CODE
         *
         * TMN 일최저기온 : baseTime(0600) TMX 일최고기온 : baseTime(1500) T3H 3시간 기온 : baseTime 내일오전 : 0900 내일오후 : 1500 R06 - 강수량 S06 - 적설
         */


        String todayMin = weatherMap.get(FORECAST_SPACE_PREFIX + "_" + baseDate + "_" + "0600" + "_" + "TMN");
        String todayMax = weatherMap.get(FORECAST_SPACE_PREFIX + "_" + baseDate + "_" + "1500" + "_" + "TMX");

        // String nextMin = weatherMap.get(FORECAST_SPACE_PREFIX + "_" + nextDate + "_" + "0600" + "_" + "TMN"); // 내일 최저기온
        // String nextMax = weatherMap.get(FORECAST_SPACE_PREFIX + "_" + nextDate + "_" + "1500" + "_" + "TMX"); // 내일 최고기온
        String nextMin = weatherMap.get(FORECAST_SPACE_PREFIX + "_" + nextDate + "_" + "0900" + "_" + "T3H"); // 내일 오전(0900) 기온
        String nextMax = weatherMap.get(FORECAST_SPACE_PREFIX + "_" + nextDate + "_" + "1500" + "_" + "T3H"); // 내일 오후(1500) 기온
        String tmpr06 = rainfallMap.get(getNearDate(rainfallMap, baseDate + baseTime)); // 강수량
        String tmps06 = snowfallMap.get(getNearDate(snowfallMap, baseDate + baseTime)); // 적설

        String status = ""; // 날씨 아이콘
        String diff = ""; // 전날 대비 기온 차이

        /*
         * 날씨 아이콘 아이디 생성
         *
         * 하늘상태(SKY) 코드 : 맑음(1), 구름조금(2), 구름많음(3), 흐림(4) 강수형태(PTY) 코드 : 없음(0), 비(1), 비/눈(2), 눈(3)
         */
        if (!containsNull(tmpPty, tmpSky)) {
            if ("0".equals(tmpPty)) {
                status = tmpPty + tmpSky;
            } else if ("1".equals(tmpPty)) {
                status = "05";
            } else if ("2".equals(tmpPty)) {
                status = "06";
            } else if ("3".equals(tmpPty)) {
                status = "07";
            }
        }

        Weather weather = new Weather();

        weather.setBaseDate(baseDate);
        weather.setBaseTime(baseTime);

        weather.setStatus(nvl(status, "00"));
        weather.setCurrent(tempRound(current));
        weather.setTodayMin(tempRound(todayMin));
        weather.setTodayMax(tempRound(todayMax));
        weather.setNextMin(tempRound(nextMin));
        weather.setNextMax(tempRound(nextMax));

        weather.setDiff(diff);

        weather.setRainfall(Integer.parseInt(nvl(tmpr06, "-1")));
        weather.setSnowfall(NumberUtils.toInt(nvl(tmps06, "-1")));
        weather.setSky(Integer.parseInt(nvl(tmpSky, "-1")));
        weather.setPty(Integer.parseInt(nvl(tmpPty, "-1")));

        if (containsNull(tmpPty, tmpSky, current, todayMin, todayMax, nextMin, nextMax)) {
            logger.info("### 날씨 API 조회 오류 : PTY [{}], SKY [{}], current [{}], todayMin [{}], todayMax [{}], nextMin [{}], nextMax [{}], R06[{}], S06[{}]", tmpPty, tmpSky, current, todayMin, todayMax,
                    nextMin, nextMax, tmpr06, tmpr06);
            throw new EmaulException(ErrorCode.WEATHER_ERROR_92, weather);
        }

        return weather;
    }


    // =========================================================================
    // PRIVATE METHOD
    // =========================================================================

    /**
     * API 호출
     *
     * @param url
     * @param mediaType
     * @return
     */
    private JSONObject callAPI(String url, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        // headers.setAccept(Arrays.asList(mediaType));
        // headers.setContentType(mediaType);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();

        // if (mediaType.equals(MediaType.APPLICATION_JSON)) {
        // return new JSONObject(responseBody);
        // } else if (mediaType.equals(MediaType.APPLICATION_XML)) {
        // return XML.toJSONObject(responseBody);
        // }

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(responseBody);
        } catch (Exception e) {
            jsonObject = XML.toJSONObject(responseBody);
        }

        return jsonObject;
    }

    /**
     * API 호출 후 반환된 결과에서 item 목록 추출
     *
     * @param jsonObject
     * @param successCode
     * @return
     */
    private JSONArray getItems(JSONObject jsonObject, String successCode) {
        try {
            JSONObject response = jsonObject.getJSONObject("response");
            JSONObject header = response.getJSONObject("header");
            if (successCode.equals(String.valueOf(header.get("resultCode")))) {
                JSONObject body = response.getJSONObject("body");
                Object items = body.get("items");
                if (items instanceof JSONObject) {
                    return ((JSONObject) items).getJSONArray("item");
                } else {
                    logger.debug("[item] is not a JSONArray");
                }
            }
        } catch (Exception e) {
            logger.info("getItems ERROR: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 시도명 변환 ex) 서울 <-> 서울특별시
     *
     * @param orgSido
     * @param isFullName
     * @return
     */
    private String[] convertSidoName(String orgSido, boolean isFullName) {

        String[][] arrSido = {{"강원도", "영동"}, {"강원도", "영서"}, {"경기도", "경기남부"}, {"경기도", "경기북부"}, {"경상남도", "경남"}, {"경상북도", "경북"}, {"광주광역시", "광주"}, {"대구광역시", "대구"}, {"대전광역시", "대전"}, {"부산광역시", "부산"},
                {"서울특별시", "서울"}, {"세종특별자치시", "세종"}, {"울산광역시", "울산"}, {"인천광역시", "인천"}, {"전라남도", "전남"}, {"전라북도", "전북"}, {"제주특별자치도", "제주"}, {"충청남도", "충남"}, {"충청북도", "충북"}};

        int sourceIdx = isFullName ? 1 : 0;
        int targetIdx = isFullName ? 0 : 1;

        String tmpString = "";

        for (String[] sido : arrSido) {
            if (sido[sourceIdx].equals(orgSido)) {
                if (!tmpString.isEmpty()) {
                    tmpString += ",";
                }
                tmpString += sido[targetIdx];
            }
        }
        return tmpString.split(",");
    }

    /**
     * 지역별 미세먼지 알림 텍스트 생성
     *
     * @param arrInformGrade
     * @param sido
     * @return
     */
    private String getInformGradeText(String[] arrInformGrade, String sido) {
        String strInformGrade = "";
        String[] arrSido = convertSidoName(sido, false);
        for (String informGrade : arrInformGrade) {
            for (String tmpSido : arrSido) {
                if (informGrade.startsWith(tmpSido)) {
                    if (!strInformGrade.isEmpty()) {
                        strInformGrade += ", ";
                    }
                    strInformGrade += informGrade.replace(":", " ").trim();
                    if (arrSido.length == 1) {
                        strInformGrade = strInformGrade.replace(tmpSido, "");
                    }
                }
            }
        }
        return "미세먼지 " + sido + " " + strInformGrade;
    }

    /**
     * 위경도 -> 그리드 좌표 변환
     *
     * @param lat
     * @param lng
     * @return
     */
    private Map<String, Object> convertLatlngToNxy(double lat, double lng) {

        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        double DEGRAD = Math.PI / 180.0;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        Map<String, Object> rs = new HashMap<>();

        rs.put("lat", lat);
        rs.put("lng", lng);

        double ra = Math.tan(Math.PI * 0.25 + (lat) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lng * DEGRAD - olon;
        if (theta > Math.PI)
            theta -= 2.0 * Math.PI;
        if (theta < -Math.PI)
            theta += 2.0 * Math.PI;
        theta *= sn;

        rs.put("nx", (int) Math.floor(ra * Math.sin(theta) + XO + 0.5));
        rs.put("ny", (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5));

        return rs;
    }

    /**
     * 날씨 API 기준날짜 셋팅
     *
     * @return
     */
    private Map<String, String> getWeatherBaseTime() {

        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        String dateFormat = "yyyyMMdd";

        String baseDate = ""; // 초단기 관측 기준 일자
        String baseTime = ""; // 초단기 관측 기준 시간

        int tmpTime = 0;

        if (minutes > 40) {
            tmpTime = hours * 100;
        } else {
            tmpTime = (hours - 1) * 100;
        }

        if (tmpTime < 0) {
            tmpTime += 2400;
            baseDate = getNextDate(dateFormat, -1);
        } else {
            baseDate = getDate(dateFormat);
        }

        baseTime = String.format("%04d", tmpTime);

        Map<String, String> map = new HashMap<>();

        map.put("baseDate", baseDate);
        map.put("baseTime", baseTime);
        map.put("prevDate", getNextDate(dateFormat, -1));
        map.put("nextDate", getNextDate(dateFormat, 1));

        return map;
    }

    private String getDecString(String encString) {
        try {
            return URLDecoder.decode(encString, "UTF-8");
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    private String getDate(String dateFormat) {
        if (StringUtils.isEmpty(dateFormat)) {
            dateFormat = "yyyy-MM-dd";
        }
        return convertDateFormat(new Date(), new SimpleDateFormat(dateFormat));
    }

    private String getNextDate(String dateFormat, int nextDays) {
        if (StringUtils.isEmpty(dateFormat)) {
            dateFormat = "yyyy-MM-dd";
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, nextDays);
        return convertDateFormat(cal.getTime(), new SimpleDateFormat(dateFormat));
    }

    private String getNearDate(Map<String, String> dateMap, String baseDate) {
        if (dateMap == null || dateMap.isEmpty())
            return null;

        long tmpDate = Long.MAX_VALUE;
        long nearDate = 0l;

        for (String key : dateMap.keySet()) {
            long tmp = Math.abs(Long.parseLong(key) - Long.parseLong(baseDate));
            if (tmpDate > tmp) {
                tmpDate = tmp;
                nearDate = Long.parseLong(key);
            }

        }
        return String.valueOf(nearDate);
    }

    private String convertDateFormat(Date sourceDate, SimpleDateFormat dateForm) {
        return dateForm.format(sourceDate);
    }

    private String nvl(String s1, String s2) {
        if (s1 == null || "".equals(s1)) {
            return s2;
        }
        return s1;
    }

    private boolean containsNull(String... strings) {
        for (String s : strings) {
            if (s == null) {
                return true;
            }
        }
        return false;
    }

    private float tempRound(String strTemp) {
        try {
            float fltTemp = Float.parseFloat(strTemp);
            return Math.round(fltTemp * 10f) / 10.0f;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return -50.0f;
    }

    @Override
    public ApiResponse<Map<String, Object>> getLifeIndex(String addr) throws Exception {
        ApiResponseHeader header = new ApiResponseHeader();
        Map<String, Object> body = Maps.newHashMap();
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();

        if (StringUtils.isEmpty(addr)) {
            header.setResultCode("91");
            header.setResultMessage("Addr is blank!");

            apiResponse.setHeader(header);
            apiResponse.setBody(body);

            return apiResponse;
        }

        String areaNo = null;
        // 1. 한글 시군구에 대한 지역코드 조회
        try {
            String[] addrs = addr.split("[ ]", -1);

            List<SimpleAddress> simpleAddrList = null;

            if (addrs.length == 1) {
                simpleAddrList = this.simpleAddressRepository.findBySido(addrs[0]);
            } else if (addrs.length == 2) {
                simpleAddrList = this.simpleAddressRepository.findBySigungu(addrs[0], addrs[1]);
            } else if (addrs.length == 3) {
                simpleAddrList = this.simpleAddressRepository.findByDong(addrs[0], addrs[1], addrs[2]);
            }

            if (simpleAddrList == null || simpleAddrList.isEmpty()) {
                header.setResultCode("92");
                header.setResultMessage("Addr is wrong!");

                apiResponse.setHeader(header);
                apiResponse.setBody(body);

                return apiResponse;
            }

            areaNo = simpleAddrList.get(0).getRegionCd();
        } catch (Exception e) {
            logger.error("<<생활기상지수 API 처리 중 오류 발생>>", e.getMessage());

            header.setResultCode("99");
            header.setResultMessage("Server runtime fails!");

            apiResponse.setHeader(header);
            apiResponse.setBody(body);

            return apiResponse;
        }

        // 서비스키
        String serviceKey = this.getDecString(env.getProperty("openapi.data.service.key.general"));
        Map<String, Object> lifeIndexMap = Maps.newHashMap();

        String nowDate = Util.getDateString();
        String dates[] = nowDate.split("[-]", -1);
        int nowMonth = Integer.valueOf(dates[1]);

        String apiUrl = String.format(env.getProperty("openapi.data.lifeIndex.fsnLife.service.url"), serviceKey, areaNo); // 식중독지수
        lifeIndexMap.put("fsn_life", this.getTextPerLifeIndex(apiUrl));

        if (nowMonth >= 3 && nowMonth <= 11) { // 자외선지수
            apiUrl = String.format(env.getProperty("openapi.data.lifeIndex.ultrvLife.service.url"), serviceKey, areaNo);
            lifeIndexMap.put("ultrv_life", this.getTextPerLifeIndex(apiUrl));
        }
        if (nowMonth >= 6 && nowMonth <= 9) { // 불쾌지수
            apiUrl = String.format(env.getProperty("openapi.data.lifeIndex.dsplsLife.service.url"), serviceKey, areaNo);
            lifeIndexMap.put("dspls_life", this.getTextPerLifeIndex(apiUrl));
        }
        if (nowMonth == 11 || nowMonth == 12 || nowMonth == 1 || nowMonth == 2 || nowMonth == 3) { // 체감온도
            apiUrl = String.format(env.getProperty("openapi.data.lifeIndex.sensorytemLife.service.url"), serviceKey, areaNo);
            lifeIndexMap.put("sensorytem_life", this.getTextPerLifeIndex(apiUrl));
        }
        if (nowMonth == 12 || nowMonth == 1 || nowMonth == 2) { // 동상가능지수, 동파가능지수
            // apiUrl = String.format(env.getProperty("openapi.data.lifeIndex.frostbiteLife.service.url"), serviceKey, areaNo);
            // lifeIndexMap.put("frostbite_life", this.getTextPerLifeIndex(apiUrl));

            apiUrl = String.format(env.getProperty("openapi.data.lifeIndex.winterLife.service.url"), serviceKey, areaNo);
            lifeIndexMap.put("winter_life", this.getTextPerLifeIndex(apiUrl));
        }

        apiResponse.setBody(lifeIndexMap);

        return apiResponse;
    }

    /**
     * API 호출 후 반환된 결과에서 item 목록 추출
     *
     * @param jsonObject
     * @param successCode
     * @return
     */
    private JSONArray getLifeIndexItems(JSONObject jsonObject, String successCode) {
        try {
            JSONObject response = jsonObject.getJSONObject("Response");
            JSONObject header = response.getJSONObject("Header");

            if (successCode.equals(String.valueOf(header.get("ReturnCode")))) {
                JSONObject body = response.getJSONObject("Body");
                Object items = body.get("IndexModel");

                if (items instanceof JSONObject) {
                    JSONObject jobj = (JSONObject) items; // ((JSONObject) items).getJSONArray("IndexModel");
                    JSONArray jarr = new JSONArray();
                    jarr.put(jobj);
                    return jarr;
                } else {
                    logger.debug("[item] is not a JSONArray");
                }
            }
        } catch (Exception e) {
            logger.info("getItems ERROR: {}", e.getMessage());
        }

        return null;
    }

    @SuppressWarnings("unused")
    private Map<String, String> getTextPerLifeIndex(String apiUrl) {
        Map<String, String> lifeIndexMap = Maps.newHashMap();

        JSONObject jsonObject = null;

        try {
            jsonObject = this.callAPI(apiUrl, MediaType.APPLICATION_JSON, true);
        } catch (Exception e) {
            logger.info("<<생활기상지수 API 연동 오류>> {}", e.getMessage());

            Map<String, String> emptyMap = Maps.newConcurrentMap();
            return emptyMap;
        }

        JSONArray items = this.getLifeIndexItems(jsonObject, "00");
        JSONObject item = null;

        String code = "";
        String date = "";
        String today = "";
        String tomorrow = "";
        String theDayAfterTomorrow = "";
        String h3 = "";

        logger.debug("<<생활기상지수 API URL>> {}", apiUrl);

        if (apiUrl.contains("getFsnLifeList")) { // 식중독지수
            try {
                // logger.info("<<생활기상지수조회-식중독지수>> jsonObject: {}", jsonObject);

                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        if (i > 0) {
                            break;
                        }

                        item = items.getJSONObject(i);

                        code = String.valueOf(item.get("code"));
                        date = String.valueOf(item.get("date"));
                        today = String.valueOf(item.get("today"));
                        tomorrow = String.valueOf(item.get("tomorrow"));
                        if (StringUtils.isEmpty(today)) {
                            today = tomorrow;
                        }
                        theDayAfterTomorrow = String.valueOf(item.get("theDayAfterTomorrow"));

                        int todayVal = Integer.valueOf(today);

                        if (todayVal >= 90) {
                            lifeIndexMap.put("level", "위험");
                            lifeIndexMap.put("level_text", "음식물 섭취에 주의하세요.");
                        } else if (todayVal >= 60 && todayVal < 90) {
                            lifeIndexMap.put("level", "경고");
                            lifeIndexMap.put("level_text", "식중독 발생 가능성이 높습니다.");
                        } else if (todayVal >= 30 && todayVal < 60) {
                            lifeIndexMap.put("level", "주의");
                            lifeIndexMap.put("level_text", "식중독 발생 가능성이 중간정도입니다.");
                        } else if (todayVal < 30) {
                            lifeIndexMap.put("level", "관심");
                            lifeIndexMap.put("level_text", "식중독 발생 가능성이 낮습니다.");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("<<생활기상지수-식중독지수 API 처리중 오류 발생>>", e.getMessage());
            }
        } else if (apiUrl.contains("getUltrvLifeList")) { // 자외선지수
            try {
                // logger.info("<<생활기상지수조회-자외선지수>> jsonObject: {}", jsonObject);

                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        if (i > 0) {
                            break;
                        }

                        item = items.getJSONObject(i);

                        code = String.valueOf(item.get("code"));
                        date = String.valueOf(item.get("date"));
                        today = String.valueOf(item.get("today"));
                        tomorrow = String.valueOf(item.get("tomorrow"));
                        if (StringUtils.isEmpty(today)) {
                            today = tomorrow;
                        }
                        theDayAfterTomorrow = String.valueOf(item.get("theDayAfterTomorrow"));

                        int todayVal = Integer.valueOf(today);

                        if (todayVal >= 11) {
                            lifeIndexMap.put("level", "위험");
                            lifeIndexMap.put("level_text", "가능한 실내에 머물러 계세요.");
                        } else if (todayVal >= 8 && todayVal < 11) {
                            lifeIndexMap.put("level", "매우 높음");
                            lifeIndexMap.put("level_text", "한낮에 외출을 피해주세요.");
                        } else if (todayVal >= 6 && todayVal < 8) {
                            lifeIndexMap.put("level", "높음");
                            lifeIndexMap.put("level_text", "선크림 챙기세요!");
                        } else if (todayVal >= 3 && todayVal < 6) {
                            lifeIndexMap.put("level", "보통");
                            lifeIndexMap.put("level_text", "모자를 챙기면 좋아요!");
                        } else if (todayVal < 30) {
                            lifeIndexMap.put("level", "낮음");
                            lifeIndexMap.put("level_text", "나들이가기 좋아요.");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("<<생활기상지수-자외선지수 API 처리중 오류 발생>>", e.getMessage());
            }
        } else if (apiUrl.contains("getDsplsLifeList")) { // 불쾌지수
            try {
                // logger.info("<<생활기상지수조회-불쾌지수>> jsonObject: {}", jsonObject);

                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        if (i > 0) {
                            break;
                        }

                        item = items.getJSONObject(i);

                        code = String.valueOf(item.get("code"));
                        date = String.valueOf(item.get("date"));
                        h3 = String.valueOf(item.get("h3"));

                        int h3Val = Integer.valueOf(h3);

                        if (h3Val >= 80) {
                            lifeIndexMap.put("level", "매우 높음");
                            lifeIndexMap.put("level_text", "외출을 자제하세요!");
                        } else if (h3Val >= 75 && h3Val < 80) {
                            lifeIndexMap.put("level", "높음");
                            lifeIndexMap.put("level_text", "마인드컨트롤 필요!");
                        } else if (h3Val >= 68 && h3Val < 75) {
                            lifeIndexMap.put("level", "보통");
                            lifeIndexMap.put("level_text", "실내/야외 활동 모두 좋습니다.");
                        } else if (h3Val < 68) {
                            lifeIndexMap.put("level", "낮음");
                            lifeIndexMap.put("level_text", "쾌적한 하루 보내세요~");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("<<생활기상지수-불쾌지수 API 처리중 오류 발생>>", e.getMessage());
            }
        } else if (apiUrl.contains("getSensorytemLifeList")) { // 체감온도지수
            try {
                // logger.info("<<생활기상지수조회-체감온도지수>> jsonObject: {}", jsonObject);

                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        if (i > 0) {
                            break;
                        }

                        item = items.getJSONObject(i);

                        code = String.valueOf(item.get("code"));
                        date = String.valueOf(item.get("date"));
                        h3 = String.valueOf(item.get("h3"));

                        int h3Val = Integer.valueOf(h3);

                        if (h3Val >= -10) {
                            lifeIndexMap.put("level", "관심");
                            lifeIndexMap.put("level_text", "따뜻하게 챙겨입으세요!");
                        } else if (h3Val >= -25 && h3Val < -10) {
                            lifeIndexMap.put("level", "주의");
                            lifeIndexMap.put("level_text", "저체온증에 주의하세요.");
                        } else if (h3Val >= -45 && h3Val < -25) {
                            lifeIndexMap.put("level", "경고");
                            lifeIndexMap.put("level_text", "보온용품이 필요해요.");
                        } else if (h3Val < -45) {
                            lifeIndexMap.put("level", "위험");
                            lifeIndexMap.put("level_text", "야외활동은 위험합니다!");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("<<생활기상지수-체감온도지수 API 처리중 오류 발생>>", e.getMessage());
            }
        } else if (apiUrl.contains("getFrostbiteLifeList")) { // 동상가능지수
            try {
                // 공공데이타 생활기상지수에서 제외됐음
                // logger.info("<<생활기상지수조회-동상가능지수>> jsonObject: {}", jsonObject);

                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        if (i > 0) {
                            break;
                        }

                        item = items.getJSONObject(i);

                        code = String.valueOf(item.get("code"));
                        date = String.valueOf(item.get("date"));
                        h3 = String.valueOf(item.get("h3"));

                        int h3Val = Integer.valueOf(h3);

                        if (h3Val >= -1.5) {
                            lifeIndexMap.put("level", "낮음");
                            lifeIndexMap.put("level_text", "감기조심하세요.");
                        } else if (h3Val >= -5 && h3Val < -1.5) {
                            lifeIndexMap.put("level", "보통");
                            lifeIndexMap.put("level_text", "보온용품을 소지하세요.");
                        } else if (h3Val < -5) {
                            lifeIndexMap.put("level", "높음");
                            lifeIndexMap.put("level_text", "야외활동에 주의하세요!");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("<<생활기상지수-동상가능지수 API 처리중 오류 발생>>", e.getMessage());
            }
        } else if (apiUrl.contains("getWinterLifeList")) { // 동파가능지수
            try {
                // logger.info("<<생활기상지수조회-동파가능지수>> jsonObject: {}", jsonObject);

                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        if (i > 0) {
                            break;
                        }

                        item = items.getJSONObject(i);

                        code = String.valueOf(item.get("code"));
                        date = String.valueOf(item.get("date"));
                        h3 = String.valueOf(item.get("h3"));

                        int h3Val = Integer.valueOf(h3);

                        if (h3Val > 75) {
                            lifeIndexMap.put("level", "매우 높음");
                            lifeIndexMap.put("level_text", "수도꼭지를 조금 틀어주세요!");
                        } else if (h3Val > 50 && h3Val <= 75) {
                            lifeIndexMap.put("level", "높음");
                            lifeIndexMap.put("level_text", "수도꼭지와 계량기 보온에 신경쓰세요.");
                        } else if (h3Val > 25 && h3Val <= 50) {
                            lifeIndexMap.put("level", "보통");
                            lifeIndexMap.put("level_text", "수도꼭지를 가끔 확인하세요.");
                        } else if (h3Val <= 25) {
                            lifeIndexMap.put("level", "낮음");
                            lifeIndexMap.put("level_text", "안심하세요!");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("<<생활기상지수-동파가능지수 API 처리중 오류 발생>>", e.getMessage());
            }
        }

        return lifeIndexMap;
    }
    // 월별로
    // # 자외선지수(3월~11월)
    // openapi.data.lifeIndex.ultrvLife.service.url=http://203.247.66.146/iros/RetrieveLifeIndexService/getUltrvLifeList?ServiceKey=%s&AreaNo=%s
    // # 식중독지수(연중)
    // openapi.data.lifeIndex.fsnLife.service.url=http://203.247.66.146/iros/RetrieveLifeIndexService/getFsnLifeList?ServiceKey=%s&AreaNo=%s
    // # 불쾌지수(6월~9월)
    // openapi.data.lifeIndex.dsplsLife.service.url=http://203.247.66.146/iros/RetrieveLifeIndexService/getDsplsLifeList?ServiceKey=%s&AreaNo=%s
    // # 체감온도(11월~3월)
    // openapi.data.lifeIndex.sensorytemLife.service.url=http://203.247.66.146/iros/RetrieveLifeIndexService/getSensorytemLifeList?ServiceKey=%s&AreaNo=%s
    // # 동상가능지수(12월~2월, 공공데이타 생활기상지수에서 제외됐음)
    // openapi.data.lifeIndex.frostbiteLife.service.url=http://203.247.66.146/iros/RetrieveLifeIndexService/getFrostbiteLifeList?ServiceKey=%s&AreaNo=%s
    // # 동파가능지수(12월~2월)
    // openapi.data.lifeIndex.winterLife.service.url=http://203.247.66.146/iros/RetrieveLifeIndexService/getWinterLifeList?ServiceKey=%s&AreaNo=%s

    /**
     * API 호출
     *
     * @param url
     * @param mediaType
     * @return
     */
    private JSONObject callAPI(String url, MediaType mediaType, boolean addContentType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypeList = new ArrayList<MediaType>();
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        mediaTypeList.add(MediaType.APPLICATION_XML);
        // headers.setAccept(Arrays.asList(mediaType));
        headers.setAccept(mediaTypeList);

        // if (addContentType) {
        // headers.setContentType(mediaType);
        // }

        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        // logger.debug("<<url>> {}", url);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        // logger.debug("<<responseBody>> {}", responseBody);

        // if (mediaType.equals(MediaType.APPLICATION_JSON)) {
        // return new JSONObject(responseBody);
        // } else if (mediaType.equals(MediaType.APPLICATION_XML)) {
        // return XML.toJSONObject(responseBody);
        // }

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(responseBody);
        } catch (Exception e) {
            jsonObject = XML.toJSONObject(responseBody);
        }

        return jsonObject;
    }

}
