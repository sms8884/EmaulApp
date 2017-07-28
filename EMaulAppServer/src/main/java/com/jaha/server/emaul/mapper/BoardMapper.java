package com.jaha.server.emaul.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.model.BoardCategory;

/**
 * Created by shavrani on 16-10-25
 */
@Mapper
public interface BoardMapper {

    int insertBoardCategory(BoardCategory boardCategory);

}
