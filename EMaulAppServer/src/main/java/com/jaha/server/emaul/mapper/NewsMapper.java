package com.jaha.server.emaul.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NewsMapper {

    public List<Map<String, Object>> selectLockScreenNewsList(Map<String, Object> params);

}
