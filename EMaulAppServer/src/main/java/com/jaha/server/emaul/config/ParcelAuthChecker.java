package com.jaha.server.emaul.config;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jaha.server.emaul.common.code.ParcelCode;
import com.jaha.server.emaul.model.ParcelLocker;
import com.jaha.server.emaul.service.ParcelService;
import com.jaha.server.emaul.util.Util;
import com.sybase.powerbuilder.cryptography.Base64;
import com.sybase.powerbuilder.cryptography.PBCrypto;

public class ParcelAuthChecker extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParcelAuthChecker.class);

    private static final String PARCEL_AUTH_TARGET_URL = "/api/public/parcel";

    @Autowired
    private ParcelService parcelService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();

        boolean checkAuth = false;
        if (url.startsWith(PARCEL_AUTH_TARGET_URL) && url.contains("/2/")) {
            checkAuth = true;
        }

        if (checkAuth) {
            JSONObject jo = null;

            LOGGER.debug("auth checker. request.getRequestURI() : " + url);
            LOGGER.debug("auth checker. request.getRemoteAddr() : " + request.getRemoteAddr());

            try {
                String uuid = request.getHeader("x-jaha-uuid");
                String authToken = request.getHeader("x-jaha-authToken");
                LOGGER.debug("uuid: {}, authToken: {}", uuid, authToken);

                if (StringUtils.isBlank(uuid) || StringUtils.isBlank(authToken)) {
                    jo = new JSONObject();
                    jo.put("resultCode", ParcelCode.RESP_AUTHFAILS.getCode());
                    jo.put("resultMessage", ParcelCode.RESP_AUTHFAILS.getMessage());

                    response.setContentType("application/json");
                    jo.write(response.getWriter());

                    LOGGER.debug(jo.toString());
                    return false;
                }

                ParcelLocker parcelLocker = this.parcelService.findParcelLocker(uuid);

                if (parcelLocker == null || (parcelLocker.getPrivateKey() == null)) {
                    jo = new JSONObject();
                    jo.put("resultCode", ParcelCode.RESP_NOTEXISTS.getCode());
                    jo.put("resultMessage", ParcelCode.RESP_NOTEXISTS.getMessage());

                    response.setContentType("application/json");
                    jo.write(response.getWriter());

                    LOGGER.debug(jo.toString());
                    return false;
                }

                // LOGGER.debug("privateKey: {}", parcelLocker.getPrivateKey());

                PBCrypto pbCrypto = new PBCrypto();

                String decAuthToken = pbCrypto.decryptSecretKeyUsingRsaPrivateKey(authToken, parcelLocker.getPrivateKey());

                String base64DecodedAuthToken = Base64.decodeToString(decAuthToken);
                // LOGGER.debug("base64DecodedAuthToken: {}", base64DecodedAuthToken);

                String[] checks = base64DecodedAuthToken.split("\\|\\|");

                Date reqDatetime = Util.convertString2Date(checks[1]);
                Date nowDatetime = new Date();

                long diffMillis = nowDatetime.getTime() - reqDatetime.getTime();

                @SuppressWarnings("unused")
                int diff = (int) (diffMillis / 1000); // 일차이 (24 * 60 * 60 * 1000)

                // uuid가 같지 않거나 API 요청시간과 현재시간의 차이가 180초를 초과했을 경우
                if (!uuid.equals(checks[0])) { // 4 test
                    // if (!uuid.equals(checks[0]) || Math.abs(diff) > 180) {
                    jo = new JSONObject();
                    jo.put("resultCode", ParcelCode.RESP_AUTHFAILS.getCode());
                    jo.put("resultMessage", ParcelCode.RESP_AUTHFAILS.getMessage());

                    response.setContentType("application/json");
                    jo.write(response.getWriter());

                    LOGGER.debug(jo.toString());
                    return false;
                }

            } catch (Exception e) {
                LOGGER.error("<<무인택배함 API 요청 처리 중 오류 발생>>", e);

                jo = new JSONObject();
                jo.put("resultCode", ParcelCode.RESP_DECRYPTIONFAILS.getCode());
                jo.put("resultMessage", ParcelCode.RESP_DECRYPTIONFAILS.getMessage());

                response.setContentType("application/json");
                jo.write(response.getWriter());

                LOGGER.debug(jo.toString());
                return false;
            }

        }

        return super.preHandle(request, response, handler);
    }

}
