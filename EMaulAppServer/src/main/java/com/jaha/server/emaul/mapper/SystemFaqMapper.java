package com.jaha.server.emaul.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.model.SystemFaq;

@Mapper
public interface SystemFaqMapper {

    /**
     * 시스템FAQ
     */
    public List<SystemFaq> selectSystemFaqList(Map<String, Object> params);

    public int selectSystemFaqListCount(Map<String, Object> params);

    public SystemFaq selectSystemFaq(Map<String, Object> params);

}
