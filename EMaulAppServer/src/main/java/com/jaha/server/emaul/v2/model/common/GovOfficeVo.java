/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 12. 2.
 */
package com.jaha.server.emaul.v2.model.common;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.ibatis.type.Alias;

/**
 * <pre>
 * Class Name : GovOffice.java
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
@Alias("GovOfficeVo")
public class GovOfficeVo implements Serializable {
    /** SID */
    private static final long serialVersionUID = 6691431808777322859L;

    /** 시도 */
    private String sido;
    /** 시군구 */
    private String sigungu;
    /** 관공서명 */
    private String name;
    /** url */
    private String url;

    /**
     * @return the sido
     */
    public String getSido() {
        return sido;
    }

    /**
     * @param sido the sido to set
     */
    public void setSido(String sido) {
        this.sido = sido;
    }

    /**
     * @return the sigungu
     */
    public String getSigungu() {
        return sigungu;
    }

    /**
     * @param sigungu the sigungu to set
     */
    public void setSigungu(String sigungu) {
        this.sigungu = sigungu;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
