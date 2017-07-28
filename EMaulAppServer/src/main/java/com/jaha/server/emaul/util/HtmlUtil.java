package com.jaha.server.emaul.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class HtmlUtil {

    public static List<String> getImgSrc(String str) {
        Pattern nonValidPattern = Pattern.compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");

        List<String> result = new ArrayList<String>();
        Matcher matcher = nonValidPattern.matcher(str);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    public static String removeTag(String html) {
        try {
            return html.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", StringUtils.EMPTY);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public static String extractBody(String content) {
        if (!StringUtils.isEmpty(content)) {
            int start = content.indexOf("<body>") + 6;
            int end = content.indexOf("</body>");
            if (end > -1) {
                return content.substring(start, end);
            }
        }
        return content;
    }

    public static String removeHtmlEntity(String content) {
        String result = content;
        if (!StringUtils.isEmpty(result)) {
            String[] entities = {"&nbsp;", "&amp;", "&middot;"};
            for (int i = 0; i < entities.length; i++) {
                result = result.replaceAll(entities[i], StringUtils.EMPTY);
            }
        }
        return result;
    }

    public static String removeTagAndEntity(String html) {
        return StringUtil.trim(removeHtmlEntity(removeTag(extractBody(html))));
    }

}
