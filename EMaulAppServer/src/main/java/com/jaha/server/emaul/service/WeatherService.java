package com.jaha.server.emaul.service;

import java.util.List;
import java.util.Map;

import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.Pollution;
import com.jaha.server.emaul.model.Weather;

public interface WeatherService {

    public static final String POLLUTION_SUCCESS_CODE = "00";

    public static final String WEATHER_SUCCESS_CODE = "0000";

    public static final String FORECAST_GRIB_PREFIX = "GRIB";

    public static final String FORECAST_SPACE_PREFIX = "SPACE";

    /**
     * 대기 오염 정보 조회
     *
     * @param sido
     */
    public Pollution getPollution(String sido);


    Map<String, String> getCurrentPollution(String stationName);

    /**
     * 대기오염 정보 API 호출
     */
    public void callPollution();

    /**
     * 대기오염 push
     */
    public List<Map<String, Object>> pushPollution();

    /**
     * 동네 날씨 정보 조회
     *
     * @param user
     * @return
     */
    public Weather getWeather(Double lat, Double lng);

    /**
     * 생활기상지수 조회
     *
     * @param addr
     * @return
     */
    public ApiResponse<Map<String, Object>> getLifeIndex(String addr) throws Exception;

}
