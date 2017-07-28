/**
 * Copyright (c) 2017 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2017. 1. 31.
 */
package com.jaha.server.emaul.v2.service.cache;

import com.jaha.server.emaul.v2.model.cache.WeatherCacheVo;

/**
 * <pre>
 * Class Name : WeatherCacheService.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2017. 1. 31.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2017. 1. 31.
 * @version 1.0
 */
public interface WeatherCacheService {

    /**
     * 동네날씨/생활기상지수 데이타를 등록한다.
     *
     * @param param
     */
    void regWeatherCache(WeatherCacheVo param) throws Exception;

    /**
     * 최근 30분전부터 등록된 동네날씨 데이타 한 건을 조회한다.
     *
     * @param lat
     * @param lng
     * @return
     */
    WeatherCacheVo findWeatherCacheBefore30Minutes(Double lat, Double lng) throws Exception;

    /**
     * 최근 30분전부터 등록된 생활기상지수 데이타 한 건을 조회한다.
     *
     * @param addr
     * @return
     */
    WeatherCacheVo findWeatherCacheBefore30Minutes(String addr) throws Exception;

}
