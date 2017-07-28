/**
 *
 */
package com.jaha.server.emaul.v2.mapper.cache;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.v2.model.cache.WeatherCacheVo;

/**
 * @author 전강욱(realsnake@jahasmart.com) <br />
 *         This Mapper class mapped db-table called weather_cache
 */
@Mapper
public interface WeatherCacheMapper {

    /**
     * 동네날씨/생활기상지수 데이타를 등록한다.
     */
    void insertWeatherCache(WeatherCacheVo param);

    /**
     * 최근 30분전부터 등록된 동네날씨/생활기상지수 데이타 한 건을 조회한다.
     */
    WeatherCacheVo selectWeatherCacheBefore30Minutes(WeatherCacheVo param);

}
