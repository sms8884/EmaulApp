/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 10. 26.
 */
package com.jaha.server.emaul.v2.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.GcmService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.PasswordHash;
import com.jaha.server.emaul.util.SessionAttrs;

/**
 * <pre>
 * Class Name : LoginController.java
 * Description : 로그인
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 10. 26.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 10. 26.
 * @version 1.0
 */
@Controller
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private GcmService gcmService;

    @RequestMapping(value = "/v2/api/public/user/login", method = RequestMethod.POST)
    public @ResponseBody User login(HttpServletRequest req, @RequestParam(value = "email") String email, @RequestParam(value = "password") String password,
            @RequestParam(value = "kind", required = false) String kind, @RequestParam(value = "gcmId", required = false) String gcmId,
            @RequestParam(value = "appVersion", required = false) String appVersion) throws JsonProcessingException {

        HttpSession session = req.getSession();
        // Long userId = (Long) session.getAttribute("userId");

        User user = null;

        // if (userId != null) { // 세션 사용자 아이디 존재유무 확인
        // LOGGER.info("<<이미 로그인 되있단다~>> {}", userId);
        // user = this.userService.getUser(userId);
        // return this.userService.convertToPrivateUser(user);
        // }

        try {
            LOGGER.info("<<이메일: {}, 비번: {}, 종류: {}, GCM ID: {}, 앱버전: {}>>", email, password, kind, gcmId, appVersion);

            // v1 로그인은 해당 메소드로 일원화 해두었습니다.
            // v2 적용 전 참고 수정이 필요합니다.
            // userService.login(req, email, password, kind, gcmId, appVersion);
            user = this.userService.login(email, kind, gcmId, appVersion);

            if (user == null) {
                session.invalidate();
                return null;
            }

            if (PasswordHash.validatePassword(password, user.passwordHash)) {
                SessionAttrs.setUserId(session, user.id);
                SessionAttrs.setKind(session, kind);

                if ("ios".equalsIgnoreCase(kind) && StringUtils.isNotBlank(gcmId)) {
                    this.gcmService.setGcmId(user.id, gcmId, "ios");
                }

                return user;
            } else {
                // 비밀번호가 틀리단다~
                LOGGER.info("<<비밀번호가 틀리단다~>> {}", user.id);
            }
        } catch (Exception e) {
            LOGGER.error("<<로그인 오류>>", e);
        }

        session.invalidate();
        return null;
    }

}
