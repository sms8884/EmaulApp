package com.jaha.server.emaul.v2.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;
import com.jaha.server.emaul.model.BaseSecuModel;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.PhoneAuthService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAction;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAlarmSetting;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushMessage;
import com.jaha.server.emaul.v2.util.PushUtils;

@Controller("v2TrackerController")
public class TrackerController {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackerController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PhoneAuthService phoneAuthService;

    // [START] 광고 푸시 추가 by realsnake 2016.10.28
    @Autowired
    private PushUtils pushUtils;
    // [END]

    @RequestMapping(value = "/v2/api/tracker/send-location", method = RequestMethod.POST)
    public @ResponseBody String handleLocationSend(HttpServletRequest req, @RequestParam(value = "targetPhone") String targetPhone,
            @RequestParam(value = "longitude", required = false, defaultValue = "") String longitude, @RequestParam(value = "latitude", required = false, defaultValue = "") String latitude,
            @RequestParam(value = "address", required = false, defaultValue = "") String address, @RequestParam(value = "message", required = false, defaultValue = "") String message,
            @RequestParam(value = "datetime", required = false, defaultValue = "") String datetime) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user == null) {
            return "1";
        }

        String userFullName = user.getFullName();
        String userPhone = user.getPhone();

        BaseSecuModel bsm = new BaseSecuModel();
        targetPhone = bsm.encString(targetPhone);
        List<SimpleUser> targetUserList = this.pushUtils.findPushTargetUserList(PushAlarmSetting.ALARM, targetPhone);

        SimpleUser targetUser = null;

        if (targetUserList != null && targetUserList.size() > 0) {
            targetUser = targetUserList.get(0);
        }

        if (targetUser == null) {
            message = userFullName + "님이 \"이마을\"앱의 안전귀가 서비스를 요청하셨습니다. https://goo.gl/iyyLTO";

            // 발신자번호는 비즈뿌리오에 사전 등록된 번호만 문자 전송이 가능함
            phoneAuthService.sendMsgNow(bsm.descString(targetPhone), "028670816", message, "", "");

            return "2";
        } else {
            String title = PushMessage.SAFE_COMEBACKHOME_TITLE.getValue();
            Map<String, String> msg = Maps.newHashMap();
            msg.put("type", "notification");
            msg.put("title", StringUtil.nvl(title));
            msg.put("value", message);
            msg.put("sender", userFullName);
            msg.put("phone", userPhone);
            msg.put("longitude", longitude);
            msg.put("latitude", latitude);
            msg.put("address", address);
            msg.put("datetime", datetime);
            msg.put("action", String.format(PushAction.TRACKER.getValue(), userPhone));

            this.pushUtils.sendPushForSafeCombackHome(msg, targetUser);

            return "0";
        }
    }

}
