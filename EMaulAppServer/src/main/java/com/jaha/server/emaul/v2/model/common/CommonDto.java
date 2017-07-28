/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 10. 13.
 */
package com.jaha.server.emaul.v2.model.common;

import java.io.Serializable;

///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.model.User;
///////////////////////////////////////////////////////////////// 이전 버전의 클래스 /////////////////////////////////////////////////////////////////
import com.jaha.server.emaul.v2.util.PagingHelper;

/**
 * <pre>
 * Class Name : CommonDto.java
 * Description : 공통 Dto
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 10. 13.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 10. 13.
 * @version 1.0
 */
public class CommonDto implements Serializable {

    /** SID */
    private static final long serialVersionUID = 4026539935212986947L;

    /** 페이징 */
    private PagingHelper pagingHelper;

    /** 사용자 아이디 */
    private Long userId;

    /** 하우스(세대) 아이디 */
    private Long houseId;

    /** 사용자 아파트 아이디 */
    private Long aptId;

    /** 사용자 데이터 */
    private User user;

    public PagingHelper getPagingHelper() {
        if (pagingHelper == null) {
            pagingHelper = new PagingHelper();
        }
        return pagingHelper;
    }

    public void setPagingHelper(PagingHelper pagingHelper) {
        this.pagingHelper = pagingHelper;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHouseId() {
        return houseId;
    }

    public void setHouseId(Long houseId) {
        this.houseId = houseId;
    }

    public Long getAptId() {
        return aptId;
    }

    public void setAptId(Long aptId) {
        this.aptId = aptId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
