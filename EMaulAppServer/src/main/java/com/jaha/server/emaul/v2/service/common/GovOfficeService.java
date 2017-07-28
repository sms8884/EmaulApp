/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 12. 2.
 */
package com.jaha.server.emaul.v2.service.common;

import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.v2.model.common.GovOfficeVo;

/**
 * <pre>
 * Class Name : GovOfficeService.java
 * Description : Description
 *  
 * Modification Information
 * 
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 12. 2.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 12. 2.
 * @version 1.0
 */
public interface GovOfficeService {
    /* 시군구 관공서 정보 가져오기 */
    GovOfficeVo getGovOfficeVo(User user);


}
