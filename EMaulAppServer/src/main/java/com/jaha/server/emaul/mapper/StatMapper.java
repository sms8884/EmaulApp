package com.jaha.server.emaul.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface StatMapper {

    void insertSharer(Map<String, String> input);
}
