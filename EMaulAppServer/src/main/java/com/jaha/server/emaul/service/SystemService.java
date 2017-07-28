package com.jaha.server.emaul.service;

import java.util.Map;

import com.jaha.server.emaul.model.Provision;
import com.jaha.server.emaul.model.SystemFaq;
import com.jaha.server.emaul.model.SystemNotice;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.util.ScrollPage;

/**
 * Created by shavrani on 16. 06. 10..
 */
public interface SystemService {

    ScrollPage<SystemNotice> getSystemNoticeList(User user, Map<String, Object> params);

    int getSystemNoticeListCount(User user, Map<String, Object> params);

    SystemNotice getSystemNotice(User user, Map<String, Object> params);

    ScrollPage<SystemFaq> getSystemFaqList(User user, Map<String, Object> params);

    SystemFaq getSystemFaq(User user, Map<String, Object> params);

    Provision getSystemProvisionUseStatus(Long id, String status);

}
