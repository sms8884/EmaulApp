package com.jaha.server.emaul.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeatherMapper {

    /**
     * 대기오염 PUSH 발송 대상자 목록 조회
     * 
     * @param sido
     * @return
     */
    List<Long> selectPollutionPushList(String sido);

    Map<String, String> getPollutionCache(Map<String, String> input);

    void insertPollutionCache(Map<String, String> input);
}
