package com.jaha.server.emaul.service;


import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jaha.server.emaul.model.AppVersion;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.model.PushLog;
import com.jaha.server.emaul.util.ScrollPage;

/**
 * Created by doring on 15. 4. 2..
 */
public interface CommonService {
    AppVersion getAppVersion(String kind);

    FileInfo getFileInfo(Long fileKey);

    FileInfo getFileInfo(String category, Object fileGroupKey, Long fileKey);

    List<FileInfo> getFileGroup(String category, Object fileGroupKey);

    List<FileInfo> getFileGroup(String category, Object fileGroupKey, String type);

    ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, String gubun, Integer count) throws Exception;

    List<Map<String, Object>> getCodeList(CommonCode commonCode);

    public Date selectDate();

    int saveAppPageViewLog(Map<String, Object> params);
}
