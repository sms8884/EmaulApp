package com.jaha.server.emaul.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.jaha.server.emaul.common.code.ErrorCode;
import com.jaha.server.emaul.common.exception.EmaulException;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.Weather;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.service.WeatherService;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.Util;
import com.jaha.server.emaul.v2.model.cache.WeatherCacheVo;
import com.jaha.server.emaul.v2.service.cache.WeatherCacheService;

@RestController
public class WeatherController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherCacheService weatherCacheService;

    /**
     * 동네 날씨 정보
     *
     * @param request
     * @param response
     * @param sido
     * @param sgg
     * @param emd
     * @return
     */
    @RequestMapping(value = "/api/weather/village/get", method = RequestMethod.GET)
    public ApiResponse<?> getVillageWeather(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "lat", required = false) Double paramLat,
            @RequestParam(value = "lng", required = false) Double paramLng) {

        // 날씨 조회
        User user = userService.getUser(SessionAttrs.getUserId(request.getSession()));

        if (user == null) {
            logger.info("### USER 정보 없음");
            throw new EmaulException(ErrorCode.COMMON_FAIL);
        }

        Double lat = paramLat != null ? paramLat : user.house.apt.latitude;
        Double lng = paramLng != null ? paramLng : user.house.apt.longitude;

        if (lat == null || lng == null) {
            logger.info("### 위경도 정보 없음: paramLat [{}], paramLng [{}], userLat [{}], userLng [{}]", paramLat, paramLng, lat, lng);
            throw new EmaulException(ErrorCode.COMMON_FAIL);
        }

        Weather weather = null;

        try {
            WeatherCacheVo weatherCacheVo = this.weatherCacheService.findWeatherCacheBefore30Minutes(lat, lng);

            if (weatherCacheVo == null) {
                // 날씨 API 조회
                weather = weatherService.getWeather(lat, lng);
                // weather 객체 저장
                this.saveWeatherCache(weather, lat, lng);
            } else {
                // weather 객체 읽기
                String base64EncodedweatherObj = weatherCacheVo.getWeatherObj();
                weather = this.readWeatherCache(base64EncodedweatherObj);
                // logger.info("<<날씨 데이타>> {}", weather.toString());
            }
        } catch (Exception e) {
            logger.info("<<동네날씨정보 오류>> {}", e.getMessage());
            // logger.error("<<동네날씨정보 오류>>", e);
            weather = null;
        }

        if (weather == null) {
            weather = new Weather();
        }

        ApiResponse<Weather> apiResponse = new ApiResponse<>();
        apiResponse.setBody(weather);

        return apiResponse;
    }

    // 대기오염 조회 항목은 별도로 없어서 주석처리 함
    //
    // /**
    // * 대기 오염 정보 조회
    // *
    // * @param request
    // * @param response
    // * @param sido
    // * @return
    // */
    // @RequestMapping(value = "/api/public/weather/pollution/get", method = RequestMethod.GET)
    // public Map<String, Object> getPollution(HttpServletRequest request,
    // HttpServletResponse response,
    // @RequestParam(value = "sido") String sido) {
    //
    // Map<String, Object> map = new HashMap<>();
    //
    // // 대기오염 정보
    // Pollution pollution = weatherService.getPollution(sido);
    //
    // // 처리결과
    // Map<String, Object> result = new HashMap<>();
    // if(pollution != null) {
    // result.put("resultCode", "00");
    // result.put("resultMessage", "SUCCESS");
    // map.put("body", pollution);
    // } else {
    // result.put("resultCode", "99");
    // result.put("resultMessage", "FAIL");
    // }
    //
    // map.put("header", result);
    //
    // return map;
    // }

    /**
     * 실시간 대기 오염 정보 조회
     *
     * @param request
     * @param response
     * @param addr
     * @return
     */
    @RequestMapping(value = "/api/public/weather/pollution/current", method = RequestMethod.GET)
    public Map<String, Object> getCurrentPollution(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "addr") String addr) {

        Map<String, String> pollution = weatherService.getCurrentPollution(addr);

        // 처리결과
        Map<String, Object> result = Maps.newHashMap();
        Map<String, Object> map = Maps.newHashMap();
        if (pollution != null && !pollution.isEmpty()) {
            result.put("resultCode", "00");
            result.put("resultMessage", "SUCCESS");
            map.put("body", pollution);
        } else {
            result.put("resultCode", "99");
            result.put("resultMessage", "FAIL");
        }

        map.put("header", result);

        return map;
    }

    /**
     * 대기오염 batch 실행(05시 기준 대기오염 정보 배치 실행)
     */
    @RequestMapping(value = "/api/public/weather/pollution/batch", method = RequestMethod.GET)
    public void batchPollution() {
        weatherService.callPollution();
    }

    /**
     * 대기오염 나쁨 이상일 경우 push batch - 08시 기준 거주지 기준 대기오염도 나쁨 이상일 경우 push 메시지 발송
     */
    @RequestMapping(value = "/api/public/weather/pollution/push", method = RequestMethod.GET)
    public void pushPollution(HttpServletRequest request, HttpServletResponse response) {
        weatherService.pushPollution();
    }

    /**
     * 생활기상지수 조회
     *
     * @param request
     * @param response
     * @param addr
     * @return
     */
    @RequestMapping(value = "/api/weather/life-index", method = RequestMethod.GET)
    public ApiResponse<?> getLifeIndex(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "addr") String addr) throws Exception {
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();

        try {
            WeatherCacheVo weatherCacheVo = this.weatherCacheService.findWeatherCacheBefore30Minutes(addr);

            if (weatherCacheVo == null) {
                apiResponse = this.weatherService.getLifeIndex(addr);

                if ("00".equals(apiResponse.getHeader().getResultCode())) {
                    // 생활기상지수 저장
                    Map<String, Object> lifeIndexMap = apiResponse.getBody();
                    this.saveLifeIndexCache(lifeIndexMap, addr);
                }
            } else {
                // 생활기상지수 읽기
                String base64EncodedweatherObj = weatherCacheVo.getWeatherObj();
                Map<String, Object> lifeIndexMap = this.readLifeIndexCache(base64EncodedweatherObj);

                apiResponse.setBody(lifeIndexMap);
            }
        } catch (Exception e) {
            logger.error("<<생활기상지수 조회 오류>>", e.getMessage());
            apiResponse = null;
        }

        if (apiResponse == null) {
            apiResponse = new ApiResponse<>();
            Map<String, Object> lifeIndexMap = this.getEmptyLifeIndexMap();

            apiResponse.setBody(lifeIndexMap);
        }

        return apiResponse;
    }

    /**
     * 생활기상지수 조회(public 조회용)
     *
     * @param request
     * @param response
     * @param addr
     * @return
     */
    @RequestMapping(value = "/api/public/weather/life-index", method = RequestMethod.GET)
    public ApiResponse<?> getPublicLifeIndex(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "addr") String addr) throws Exception {
        ApiResponse<?> res = this.weatherService.getLifeIndex(addr);
        return res;
    }

    /**
     * weather 객체 읽기
     *
     * @param base64EncodedweatherObj
     * @return
     * @throws Exception
     */
    private Weather readWeatherCache(String base64EncodedweatherObj) throws Exception {
        byte[] base64DecodedweatherObj = Base64.decodeBase64(base64EncodedweatherObj);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(base64DecodedweatherObj));
        return (Weather) ois.readObject();
    }

    /**
     * weather 객체 저장
     *
     * @param weather
     * @param lat
     * @param lng
     * @throws Exception
     */
    private void saveWeatherCache(Weather weather, Double lat, Double lng) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(baos);
        oout.writeObject(weather);
        oout.close();
        byte[] buf = baos.toByteArray();
        String base64EncodedweatherObj = Base64.encodeBase64String(buf);
        // logger.info("<<BASE64 인코딩된 날씨 데이타>> {}, {}", base64EncodedweatherObj.length(), base64EncodedweatherObj);

        WeatherCacheVo weatherCacheVo = new WeatherCacheVo();
        weatherCacheVo.setLat(lat);
        weatherCacheVo.setLng(lng);
        weatherCacheVo.setWeatherObj(base64EncodedweatherObj);
        this.weatherCacheService.regWeatherCache(weatherCacheVo);
    }

    /**
     * 생활기상지수 객체 읽기
     *
     * @param base64EncodedweatherObj
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readLifeIndexCache(String base64EncodedweatherObj) throws Exception {
        byte[] base64DecodedweatherObj = Base64.decodeBase64(base64EncodedweatherObj);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(base64DecodedweatherObj));
        return (Map<String, Object>) ois.readObject();
    }

    /**
     * 생활기상지수 객체 저장
     *
     * @param lifeIndexMap
     * @param addr
     * @throws Exception
     */
    private void saveLifeIndexCache(Map<String, Object> lifeIndexMap, String addr) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(baos);
        oout.writeObject(lifeIndexMap);
        oout.close();
        byte[] buf = baos.toByteArray();
        String base64EncodedweatherObj = Base64.encodeBase64String(buf);
        // logger.info("<<BASE64 인코딩된 생활기상지수 데이타>> {}, {}", base64EncodedweatherObj.length(), base64EncodedweatherObj);

        WeatherCacheVo weatherCacheVo = new WeatherCacheVo();
        weatherCacheVo.setAddr(addr);
        weatherCacheVo.setWeatherObj(base64EncodedweatherObj);
        this.weatherCacheService.regWeatherCache(weatherCacheVo);
    }

    /**
     * 빈 생활지수 맵을 반환
     *
     * @return
     */
    private Map<String, Object> getEmptyLifeIndexMap() {
        Map<String, Object> lifeIndexMap = Maps.newHashMap();
        Map<String, String> emptyMap = Maps.newHashMap();
        int nowMonth = 3;

        try {
            String nowDate = Util.getDateString();
            String dates[] = nowDate.split("[-]", -1);
            nowMonth = Integer.valueOf(dates[1]);
        } catch (Exception e) {
            logger.error("<<빈 생활기상지수 맵 반환 오류>>", e.getMessage());
        }

        lifeIndexMap.put("fsn_life", emptyMap); // 식중독지수

        if (nowMonth >= 3 && nowMonth <= 11) { // 자외선지수
            lifeIndexMap.put("ultrv_life", emptyMap);
        }
        if (nowMonth >= 6 && nowMonth <= 9) { // 불쾌지수
            lifeIndexMap.put("dspls_life", emptyMap);
        }
        if (nowMonth == 11 || nowMonth == 12 || nowMonth == 1 || nowMonth == 2 || nowMonth == 3) { // 체감온도
            lifeIndexMap.put("sensorytem_life", emptyMap);
        }
        if (nowMonth == 12 || nowMonth == 1 || nowMonth == 2) { // 동상가능지수, 동파가능지수
            // lifeIndexMap.put("frostbite_life", emptyMap);
            lifeIndexMap.put("winter_life", emptyMap);
        }

        return lifeIndexMap;
    }

}
