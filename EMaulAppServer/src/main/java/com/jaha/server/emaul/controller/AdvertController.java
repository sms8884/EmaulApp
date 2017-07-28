package com.jaha.server.emaul.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jaha.server.emaul.common.code.AdvertCode;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.RestFulUtil;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by shavrani on 16. 09. 01..
 */
@Controller
public class AdvertController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private RestFulUtil restFulUtil;

    /**
     * 앱 메뉴 좌측 생활편의시스템 광고 ( 개수설정을 서버에서 결정하기위해 api따로 추가 )
     * 
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/advert/menu/life-support")
    @ResponseBody
    public ApiResponse<?> aptApSave(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        ApiResponseHeader header = apiResponse.getHeader();
        String jsonTmp = restFulUtil.getAdvertListJsonString(AdvertCode.MENU_LIFE_001.name(), user.id, "N", 20);// 2016-09-01에는 20개

        if (StringUtil.isBlank(jsonTmp)) {
            header.setResultCode(StringUtil.nvl("99"));
            header.setResultMessage(StringUtil.nvl("FAIL"));
            logger.debug(" # 광고데이터 없음 [" + AdvertCode.MENU_LIFE_001.name() + "]");
        } else {
            List<Map<String, Object>> resultList = new Gson().fromJson(jsonTmp, new TypeToken<List<Map<String, Object>>>() {}.getType());
            apiResponse.setBody(resultList);
        }

        return apiResponse;
    }

}
