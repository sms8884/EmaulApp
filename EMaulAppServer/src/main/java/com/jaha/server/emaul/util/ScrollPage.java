package com.jaha.server.emaul.util;

import java.util.List;

/**
 * Created by doring on 15. 3. 9..
 *
 * @version 2.0 EMAUL_KEY
 */
public class ScrollPage<T> {

    private List<T> content;

    private String nextPageToken;

    private String firstPageToken;

    public int pageNumber;

    public int totalCount;

    public int getSize() {
        return (content == null) ? 0 : content.size();
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<T> getContent() {
        return content;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getFirstPageToken() {
        return firstPageToken;
    }

    public void setFirstPageToken(String firstPageToken) {
        this.firstPageToken = firstPageToken;
    }

}
