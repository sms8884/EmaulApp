package com.jaha.server.emaul.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import com.jaha.server.emaul.common.code.ErrorCode;
import com.jaha.server.emaul.constants.Gu;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.LockScreenService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by shavrani on 16. 08. 18..
 */
@Controller
public class LockScreenController {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;
    @Autowired
    private LockScreenService lockScreenService;

    @RequestMapping(value = "/api/lockscreen/news")
    @ResponseBody
    public ApiResponse<?> lockscreenNews(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        params.put("userId", user.id);

        String pageType = StringUtil.nvl(params.get("pageType"));// main, list 현재는 두가지

        // pageType에 따라 해당 list size를 개수로 고정
        if ("main".equals(pageType)) {
            params.put("size", 4);
        } else if ("list".equals(pageType)) {
            params.put("size", 10);
        } else {
            logger.info(" ## parameter 'pageType' is null or not [ main, list ]");
            return null;
        }

        String iconUrl = "";
        String requestURL = req.getRequestURL().toString();
        URL url = null;
        try {
            url = new URL(requestURL);
            iconUrl = "http://" + url.getHost() + ":" + url.getPort();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }
        params.put("iconUrl", iconUrl);

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(lockScreenService.getLockScreenNews(params));
        return apiResponse;

    }

    // 잠금화면 메인뉴스
    @RequestMapping(value = "/api/lockscreen/today-news")
    @ResponseBody
    public ApiResponse<?> metroNews(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(lockScreenService.getMetroNews(params));
        return apiResponse;
    }

    @RequestMapping(value = "/api/lockscreen/river-level", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse<Map<String, Object>> riverLevel(@RequestParam(value = "guCode") Gu gu) {
        return new ApiResponse<>(lockScreenService.getRiverLevelInformation(gu));
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseBody
    public ApiResponse<Map<String, Object>> restClientExceptionHandler() {
        return new ApiResponse<>(ErrorCode.RIVER_LVL_ERROR_81.getCode(), ErrorCode.RIVER_LVL_ERROR_81.getMessage(), new HashMap<String, Object>());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ApiResponse<Map<String, Object>> illegalArgumentExceptionHandler() {
        return new ApiResponse<>(ErrorCode.RIVER_LVL_ERROR_83.getCode(), ErrorCode.RIVER_LVL_ERROR_83.getMessage(), new HashMap<String, Object>());
    }
}
