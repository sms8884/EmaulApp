/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 12. 2.
 */
package com.jaha.server.emaul.v2.mapper.common;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.v2.model.common.GovOfficeVo;

/**
 * <pre>
 * Class Name : GovOfficeMapper.java
 * Description : Description
 *  
 * Modification Information
 * 
 * Mod Date     Modifier    		  Description
 * -----------      -----------       ---------------------
 * 2016. 12. 2.        MyoungSeop       Generation
 * </pre>
 *
 * @author AAA
 * @since 2016. 12. 2.
 * @version 1.0
 */
@Mapper
public interface GovOfficeMapper {
    /* 시군구 관공서 정보 가져오기 */

    GovOfficeVo selectGovOfficeVo(GovOfficeVo govOfficeVo);
}
