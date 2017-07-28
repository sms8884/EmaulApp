package com.jaha.server.emaul.controller;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaha.server.emaul.model.GcmSendForm;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.GcmService;
import com.jaha.server.emaul.service.PhoneAuthService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
public class TrackerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackerController.class);

    @Autowired
    private GcmService gcmService;

    @Autowired
    private UserService userService;

    @Autowired
    private PhoneAuthService phoneAuthService;


    @RequestMapping(value = "/api/tracker/send-location", method = RequestMethod.POST)
    public @ResponseBody String handleLocationSend(HttpServletRequest req, @RequestParam(value = "targetPhone") String targetPhone,
            @RequestParam(value = "longitude", required = false, defaultValue = "") String longitude, @RequestParam(value = "latitude", required = false, defaultValue = "") String latitude,
            @RequestParam(value = "address", required = false, defaultValue = "") String address, @RequestParam(value = "message", required = false, defaultValue = "") String message,
            @RequestParam(value = "datetime", required = false, defaultValue = "") String datetime) {
        // @RequestBody String json) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        // LOGGER.debug("handleLocationSend / user = " + user);

        if (user == null) {
            return "1";
        }

        // JSONObject obj = new JSONObject(json);
        //
        // String targetPhone = obj.getString("targetPhone");
        // String longitude = obj.getString("longitude");
        // String latitude = obj.getString("latitude");
        // String address = obj.getString("address");
        // String datetime = obj.getString("datetime");

        String userFullName = user.getFullName();
        String userPhone = user.getPhone();
        List<User> targetUsers = userService.getUsersByPhone(targetPhone);
        User targetUser = null;

        if (targetUsers != null && targetUsers.size() > 0) {
            targetUser = targetUsers.get(0);
        }

        // LOGGER.debug("handleLocationSend / targetUser = " + targetUser);
        // LOGGER.debug("handleLocationSend / userFullName = " + userFullName);

        if (targetUser == null) {
            message = userFullName + "님이 \"이마을\"앱의 안전귀가 서비스를 요청하셨습니다. https://goo.gl/iyyLTO";

            // 발신자번호는 비즈뿌리오에 사전 등록된 번호만 문자 전송이 가능함
            phoneAuthService.sendMsgNow(targetPhone, "028670816", message, "", "");

            return "2";
        } else {
            String title = "안전귀가 위치 알림 서비스입니다.";

            Map<String, String> msg = Maps.newHashMap();
            msg.put("type", "notification");
            msg.put("title", StringUtil.nvl(title, ""));
            msg.put("value", message);
            msg.put("sender", userFullName);
            msg.put("phone", userPhone);
            msg.put("longitude", longitude);
            msg.put("latitude", latitude);
            msg.put("address", address);
            msg.put("datetime", datetime);
            msg.put("action", "emaul://tracker-map?phone=" + userPhone);


            GcmSendForm form = new GcmSendForm();
            form.setMessage(msg);
            form.setUserIds(Lists.newArrayList(targetUser.id));

            gcmService.sendGcm(form);

            return "0";
        }
    }
}
