package com.jaha.server.emaul.service;

import java.util.List;

import com.jaha.server.emaul.model.GcmSendForm;
import com.jaha.server.emaul.model.User;

/**
 * Created by doring on 15. 4. 28..
 */
public interface GcmService {
    void setGcmId(Long userId, String gcmId);

    void setGcmId(Long userId, String gcmId, String kind);

    String sendGcm(GcmSendForm form);

    void sendGcmToAdmin(List<User> admins, String value);
}
