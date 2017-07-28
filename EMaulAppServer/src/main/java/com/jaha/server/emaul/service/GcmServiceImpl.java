package com.jaha.server.emaul.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaha.server.emaul.model.GcmSendForm;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.prop.UrlProperties;

/**
 * Created by doring on 15. 4. 28..
 */
@Service
public class GcmServiceImpl implements GcmService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private UrlProperties urlProperties;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setGcmId(Long userId, String gcmId) {
        this.setGcmId(userId, gcmId, "android");
    }

    @Override
    public void setGcmId(Long userId, String gcmId, String kind) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String appId = "emaul";
            if (kind.equals("ios")) {
                appId = "emaul_ios";
            }

            if (gcmId.isEmpty()) {
                restTemplate.delete(urlProperties.getGcmServer() + "/" + appId + "/" + userId);
            } else {
                restTemplate.put(urlProperties.getGcmServer() + "/" + appId + "/" + userId + "/" + gcmId, null);
            }
        } catch (Exception e) {
            logger.error("<<GCM 아이디 변동 처리 중 오류>>", e);
        }
    }

    @Override
    public String sendGcm(GcmSendForm form) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Long> checkUserIds = form.getUserIds();
        List<Long> finalUserIds = new ArrayList<Long>();

        for (Long userId : checkUserIds) {
            User user = this.userService.getUser(userId);
            // 탈퇴, 차단, 방문자 제외
            if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
                continue;
            }

            finalUserIds.add(userId);
        }

        form.setUserIds(finalUserIds);

        HttpEntity<byte[]> request = null;
        try {
            String formJson = this.mapper.writeValueAsString(form);
            request = new HttpEntity<>(formJson.getBytes("utf-8"), headers);
            logger.debug("<<푸시데이타>> {}", formJson);
        } catch (Exception e) {
            logger.error("<<푸시 발송 중 오류>>", e);
        }

        return restTemplate.postForObject(urlProperties.getGcmServer() + "/send", request, String.class);
    }

    // 관리자에게 알림 메시지 보내기
    @Override
    public void sendGcmToAdmin(List<User> admins, String value) {
        if (admins != null && value != null) {
            for (User admin : admins) {
                if (admin != null && admin.setting.notiAlarm) {
                    GcmSendForm form = new GcmSendForm();
                    Map<String, String> msg = Maps.newHashMap();
                    msg.put("type", "notification");
                    msg.put("value", value);
                    form.setUserIds(Lists.newArrayList(admin.id));
                    form.setMessage(msg);

                    this.sendGcm(form);
                }
            }
        }
    }
}
