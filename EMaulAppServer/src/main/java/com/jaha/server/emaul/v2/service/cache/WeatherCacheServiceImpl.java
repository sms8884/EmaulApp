/**
 * Copyright (c) 2017 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2017. 1. 31.
 */
package com.jaha.server.emaul.v2.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaha.server.emaul.v2.mapper.cache.WeatherCacheMapper;
import com.jaha.server.emaul.v2.model.cache.WeatherCacheVo;

/**
 * <pre>
 * Class Name : WeatherCacheImpl.java
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
@Service
public class WeatherCacheServiceImpl implements WeatherCacheService {

    @Autowired
    private WeatherCacheMapper weatherCacheMapper;

    @Override
    @Transactional
    public void regWeatherCache(WeatherCacheVo param) throws Exception {
        this.weatherCacheMapper.insertWeatherCache(param);
    }

    @Override
    @Transactional(readOnly = true)
    public WeatherCacheVo findWeatherCacheBefore30Minutes(Double lat, Double lng) throws Exception {
        WeatherCacheVo param = new WeatherCacheVo();
        param.setLat(lat);
        param.setLng(lng);

        return this.weatherCacheMapper.selectWeatherCacheBefore30Minutes(param);
    }

    @Override
    @Transactional(readOnly = true)
    public WeatherCacheVo findWeatherCacheBefore30Minutes(String addr) throws Exception {
        WeatherCacheVo param = new WeatherCacheVo();
        param.setAddr(addr);

        return this.weatherCacheMapper.selectWeatherCacheBefore30Minutes(param);
    }

}
