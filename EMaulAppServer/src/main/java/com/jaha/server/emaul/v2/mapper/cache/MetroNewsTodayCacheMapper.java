/**
 *
 */
package com.jaha.server.emaul.v2.mapper.cache;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.v2.model.cache.MetroNewsTodayCacheVo;

/**
 * @author 전강욱(realsnake@jahasmart.com) <br />
 *         This Mapper class mapped db-table called metro_news_today_cache
 */
@Mapper
public interface MetroNewsTodayCacheMapper {

    /**
     * 메트로 오늘의 뉴스 JSON 데이타를 등록한다
     */
    void insertMetroNewsTodayCache(MetroNewsTodayCacheVo param);

    /**
     * 최근 30분전부터 등록된 메트로 오늘의 뉴스 JSON 데이타 한 건을 조회한다.
     */
    MetroNewsTodayCacheVo selectMetroNewsTodayCacheBefore30Minutes();

}
