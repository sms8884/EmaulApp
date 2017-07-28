/**
 *
 */
package com.jaha.server.emaul.v2.util;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.jaha.server.emaul.v2.model.common.Paging;
import com.jaha.server.emaul.v2.model.common.Search;
import com.jaha.server.emaul.v2.model.common.Sort;

/**
 * @author 전강욱(realsnake@jahasmart.com)
 * @설명 : 페이징 Helper
 */
public class PagingHelper extends Paging {

    /** SID */
    private static final long serialVersionUID = 3864334909507070327L;

    /** 검색조건 */
    private Search search;
    /** 정렬조건 */
    private List<Sort> sortList;

    /** 기본 생성자 */
    public PagingHelper() {

    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public List<Sort> getSortList() {
        return sortList;
    }

    public void setSortList(List<Sort> sortList) {
        this.sortList = sortList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
