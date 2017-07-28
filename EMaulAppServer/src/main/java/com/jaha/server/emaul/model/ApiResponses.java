package com.jaha.server.emaul.model;

import java.util.Map;

import com.google.common.collect.Maps;
import com.jaha.server.emaul.util.StringUtil;

/**
 * <pre>
 * Class Name : ApiResponses.java
 * Description : 재정의된 api 응답 format
 * </pre>
 *
 * @author shavrani
 * @since 2017. 2. 2.
 * @version 1.0
 */
public class ApiResponses {

    private final Map<String, Object> header;
    private final Map<String, Object> body;
    private final Map<String, Object> footer;

    public ApiResponses() {
        header = Maps.newHashMap();
        body = Maps.newHashMap();
        footer = Maps.newHashMap();

        setDefaultHeader();
    }

    private void setDefaultHeader() {
        header.put("resultCode", "00");
        header.put("resultMessage", "success");
        header.put("version", "1.0");
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public Map<String, Object> getFooter() {
        return footer;
    }

    /* ----------------------------------------------------- header 기본값 set / get ------------------------------------------------ */

    /**
     * [ header ] result code setter
     */
    public void resultCode(String code) {
        header.put("resultCode", code);
    }

    /**
     * [ header ] result code getter
     */
    public String resultCode() {
        return StringUtil.nvl(header.get("resultCode"));
    }

    /**
     * [ header ] result message setter
     */
    public void resultMessage(String message) {
        header.put("resultMessage", message);
    }

    /**
     * [ header ] result message getter
     */
    public String resultMessage() {
        return StringUtil.nvl(header.get("resultMessage"));
    }

    /**
     * [ header ] version setter
     */
    public void version(String version) {
        header.put("version", version);
    }

    /**
     * [ header ] version getter
     */
    public String version() {
        return StringUtil.nvl(header.get("version"));
    }

    /* ----------------------------------------------------- header 기본값 set / get ------------------------------------------------ */

    /* ----------------------------------------------------- body 기본값 set / get ------------------------------------------------ */

    /**
     * [ body ] list setter
     */
    public void list(Object object) {
        body.put("list", object);
    }

    /**
     * [ body ] list getter
     */
    public Object list() {
        return body.get("list");
    }

    /**
     * [ body ] data setter
     */
    public void data(Object object) {
        body.put("data", object);
    }

    /**
     * [ body ] data getter
     */
    public Object data() {
        return body.get("data");
    }

    /* ----------------------------------------------------- body 기본값 set / get ------------------------------------------------ */

    /* ----------------------------------------------------- footer 기본값 set /get ------------------------------------------------ */

    /**
     * [ footer ] nextPageToken setter
     */
    public void nextPageToken(String nextPageToken) {
        footer.put("nextPageToken", nextPageToken);
    }

    /**
     * [ footer ] nextPageToken getter
     */
    public String nextPageToken() {
        return StringUtil.nvl(footer.get("nextPageToken"));
    }

    /**
     * [ footer ] totalCount setter
     */
    public void totalCount(Integer totalCount) {
        footer.put("totalCount", totalCount);
    }

    /**
     * [ footer ] totalCount getter
     */
    public Integer totalCount() {
        return StringUtil.nvlInt(footer.get("totalCount"), null);
    }

    /**
     * [ footer ] rowCount setter
     */
    public void rowCount(Integer rowCount) {
        footer.put("rowCount", rowCount);
    }

    /**
     * [ footer ] rowCount getter
     */
    public Integer rowCount() {
        return StringUtil.nvlInt(footer.get("rowCount"), null);
    }

    /* ----------------------------------------------------- footer 기본값 set / get ------------------------------------------------ */

}
