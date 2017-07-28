package com.jaha.server.emaul.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.model.CommonCode;

@Mapper
public interface CommonMapper {

    public Map<String, Object> selectUdsMsg(Map<String, Object> params);

    public List<Map<String, Object>> selectCodeList(CommonCode commonCode);

    public Date selectDate();

    public int saveAppPageViewLog(Map<String, Object> params);

}
