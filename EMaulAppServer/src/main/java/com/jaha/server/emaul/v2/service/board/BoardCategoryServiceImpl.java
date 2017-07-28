/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 11. 4.
 */
package com.jaha.server.emaul.v2.service.board;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaha.server.emaul.v2.mapper.board.BoardCategoryMapper;
import com.jaha.server.emaul.v2.model.board.BoardCategoryVo;

/**
 * <pre>
 * Class Name : BoardCategoryServiceImpl.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 11. 4.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 11. 4.
 * @version 1.0
 */
@Service("v2BoardCategoryService")
public class BoardCategoryServiceImpl implements BoardCategoryService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BoardCategoryMapper boardCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BoardCategoryVo> findBoardCategoryList(Long aptId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public BoardCategoryVo findBoardCategory(Long categoryId) throws Exception {
        return this.boardCategoryMapper.selectBoardCategory(categoryId);
    }

    @Override
    public BoardCategoryVo findBoardCategory(Long categoryId, List<String> userAuthTypeList) throws Exception {
        BoardCategoryVo boardCategory = this.boardCategoryMapper.selectBoardCategory(categoryId);
        boardCategory.setIsReadable(this.compareUserTypeToBoardType(userAuthTypeList, boardCategory.getJsonArrayReadableUserType()));
        boardCategory.setIsWritable(this.compareUserTypeToBoardType(userAuthTypeList, boardCategory.getJsonArrayWritableUserType()));
        return boardCategory;
    }

    /**
     * 사용자권한타입과 게시판사용자권한타입(["jaha","admin","user","gasChecker"])을 비교한다.
     *
     * @param user
     * @param userType
     * @return
     */
    private boolean compareUserTypeToBoardType(List<String> userAuthTypeList, String boardAuthType) {
        boardAuthType = StringUtils.remove(boardAuthType, '[');
        boardAuthType = StringUtils.remove(boardAuthType, ']');
        boardAuthType = StringUtils.remove(boardAuthType, '"');
        String[] boardTypes = boardAuthType.split("[,]", -1);

        boolean checkResult = false;
        for (String ut : userAuthTypeList) {
            if (ArrayUtils.contains(boardTypes, ut)) {
                checkResult = true;
                break;
            }
        }

        return checkResult;
    }

}
