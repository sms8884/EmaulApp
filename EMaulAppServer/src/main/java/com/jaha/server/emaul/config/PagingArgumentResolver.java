/**
 *
 */
package com.jaha.server.emaul.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.jaha.server.emaul.v2.constants.CommonConstants;
import com.jaha.server.emaul.v2.model.common.Search;
import com.jaha.server.emaul.v2.model.common.Sort;
import com.jaha.server.emaul.v2.util.PagingHelper;

/**
 * <pre>
 * Class Name : PagingArgumentResolver.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 9. 22.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 9. 22.
 * @version 1.0
 */
public class PagingArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PagingArgumentResolver.class);

    @Override
    public Object resolveArgument(MethodParameter mp, ModelAndViewContainer mvc, NativeWebRequest nwr, WebDataBinderFactory wdbf) throws Exception {
        try {
            Class<?> klass = mp.getParameterType();

            if (klass != null && klass.equals(PagingHelper.class) && nwr.getNativeRequest() != null && nwr.getNativeRequest() instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) nwr.getNativeRequest();

                PagingHelper pagingHelper = new PagingHelper();

                /////////////////////////////////////////////////////////////////////////// 페이징 조건 ///////////////////////////////////////////////////////////////////////////
                int pageSize = CommonConstants.DEFAULT_PAGE_SIZE;

                if (StringUtils.isNumeric(request.getParameter("count"))) {
                    pageSize = Integer.valueOf(request.getParameter("count"));

                    if (pageSize < 20) {
                        pageSize = 20;
                    }
                }
                pagingHelper.setPageSize(pageSize);
                /////////////////////////////////////////////////////////////////////////// 페이징 조건 ///////////////////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////////////////// 검색 조건 ///////////////////////////////////////////////////////////////////////////
                Search search = new Search();

                if (StringUtils.isNotBlank(request.getParameter("item")) && StringUtils.isNotBlank(request.getParameter("keyword"))) {
                    search.setItem(request.getParameter("item"));
                    search.setKeyword(request.getParameter("keyword"));
                }
                if (StringUtils.isNotBlank(request.getParameter("startDate"))) {
                    search.setStartDate(request.getParameter("startDate"));
                }
                if (StringUtils.isNotBlank(request.getParameter("endDate"))) {
                    search.setEndDate(request.getParameter("endDate"));
                }

                pagingHelper.setSearch(search);
                /////////////////////////////////////////////////////////////////////////// 검색 조건 ///////////////////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////////////////// 정렬 조건 ///////////////////////////////////////////////////////////////////////////
                // /admin/board/post/list?pageNum=1&pageSize=10&sort=house.dong,asc&sort=house.ho,asc
                String[] sorts = request.getParameterValues("sort");

                if (sorts != null && sorts.length > 0) {
                    List<Sort> sortList = new ArrayList<Sort>();

                    for (String temp : sorts) {
                        Sort s = new Sort();

                        if (temp.indexOf(',') > -1) {
                            String[] temps = temp.split("[,]", -1);
                            s.setColumn(temps[0]);
                            s.setAscOrDesc(temps[1]);
                        } else {
                            s.setColumn(temp);
                            s.setAscOrDesc(CommonConstants.SortType.DESC.getValue());
                        }

                        // LOGGER.debug("<<정렬>> {}", s.toString());
                        sortList.add(s);
                    }

                    pagingHelper.setSortList(sortList);
                }
                /////////////////////////////////////////////////////////////////////////// 정렬 조건 ///////////////////////////////////////////////////////////////////////////

                nwr.setAttribute("pagingHelper", pagingHelper, RequestAttributes.SCOPE_REQUEST);
                LOGGER.debug("<<페이징>> {}", pagingHelper.toString());

                return pagingHelper;
            }
        } catch (RuntimeException re) {
            LOGGER.error("<<PagingArgumentResolver 오류>>", re);
        }

        return null;
    }

    @Override
    public boolean supportsParameter(MethodParameter mp) {
        return mp.getParameterType().equals(PagingHelper.class);
    }

}
