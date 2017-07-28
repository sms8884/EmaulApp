/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 12. 2.
 */
package com.jaha.server.emaul.v2.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.v2.mapper.common.GovOfficeMapper;
import com.jaha.server.emaul.v2.model.common.GovOfficeVo;

/**
 * <pre>
 * Class Name : GovOfficeServiceImpl.java
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
@Service
public class GovOfficeServiceImpl implements GovOfficeService {

    @Autowired
    GovOfficeMapper govOfficeMapper;

    /*
     * (non-Javadoc)
     * 
     * @see com.jaha.server.emaul.v2.service.common.GovOfficeService#getGovOfficeVo(com.jaha.server.emaul.v2.model.common.GovOfficeVo)
     */
    @Override
    public GovOfficeVo getGovOfficeVo(User user) {
        String sigungu = user.house.apt.address.시군구명; // 시군구명
        String sido = user.house.apt.address.시도명; // 서울특별시, 세종특별자치시
        GovOfficeVo govOfficeParam = new GovOfficeVo();
        govOfficeParam.setSigungu(sigungu);

        GovOfficeVo resultGovOffice;
        resultGovOffice = govOfficeMapper.selectGovOfficeVo(govOfficeParam);

        if (resultGovOffice == null) { // 시군구명으로 조회시 없을경우 도단위로 조회
            govOfficeParam.setSigungu(sido);
            resultGovOffice = govOfficeMapper.selectGovOfficeVo(govOfficeParam);
        }

        return resultGovOffice;
    }

}
