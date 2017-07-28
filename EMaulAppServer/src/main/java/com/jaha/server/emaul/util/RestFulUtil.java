package com.jaha.server.emaul.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class RestFulUtil {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RestTemplate restTemplate;

    public RestFulUtil() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(10000);
        factory.setConnectTimeout(10000);
        restTemplate = new RestTemplate(factory);
    }

    @Autowired
    private Environment env;

    /**
     * 
     * @param category 카테고리번호
     * @param userId 유저번호
     * @param pushLogYn push로그의 기록유무
     * @return
     */
    public String getAdvertListJsonString(String category, Long userId, String pushLogYn) {

        String result = "";
        if (!StringUtil.isBlank(category) && userId != null) {
            if (StringUtil.isBlank(pushLogYn)) {
                pushLogYn = "N";
            }
            String adUrl = env.getProperty("adapi.data.service.url") + "?userId=" + userId + "&pushLog=" + pushLogYn + "&category=" + category;
            result = restTemplate.getForObject(adUrl, String.class);
            if (StringUtil.isBlank(result)) {
                logger.info(" # 푸시광고 요청 에러 ", adUrl);
            }
        }
        if (StringUtil.isBlank(result)) {
            result = "[]";// null이거나 빈값이면 기본 jsonarray형태로 리턴
        }
        return result;
    }

    /**
     * 
     * @param category 카테고리번호
     * @param userId 유저번호
     * @param pushLogYn push로그의 기록유무
     * @param fixedCount 결과의 개수 요청
     * @return
     */
    public String getAdvertListJsonString(String category, Long userId, String pushLogYn, int fixedCount) {

        String result = "";
        if (!StringUtil.isBlank(category) && !StringUtil.isBlank(category)) {
            if (StringUtil.isBlank(pushLogYn)) {
                pushLogYn = "N";
            }
            String adUrl = env.getProperty("adapi.data.service.url") + "?userId=" + userId + "&pushLog=" + pushLogYn + "&category=" + category + "&fixedCount=" + fixedCount;
            result = restTemplate.getForObject(adUrl, String.class);
            if (StringUtil.isBlank(result)) {
                logger.info(" # 푸시광고 요청 에러 ", adUrl);
            }
        }
        if (StringUtil.isBlank(result)) {
            result = "[]";// null이거나 빈값이면 기본 jsonarray형태로 리턴
        }
        return result;
    }

    /**
     * ap에게 edoor open 요청
     * 
     * @return
     */
    public String edoorOpenRequest(Long userId, String ip) {

        String result = "N";
        if (!StringUtil.isBlank(ip)) {
            String adUrl = "http://" + ip + "/door/action/open/" + userId;
            try {
                restTemplate.getForObject(adUrl, String.class);
                result = "Y";
            } catch (Exception e) {
                logger.debug(" # AP open url connection fail [ " + adUrl + " ]");
            }
        }
        return result;
    }

}
