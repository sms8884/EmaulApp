package com.jaha.server.emaul.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.model.AptScheduler;

@Mapper
public interface AptSchedulerMapper {

    /**
     * 아파트 일정 목록조회
     * @param sido
     * @return
     */
    public List<AptScheduler> selectAptSchedulerList(Map<String, Object> params);
    
}
