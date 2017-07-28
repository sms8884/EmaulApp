package com.jaha.server.emaul.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jaha.server.emaul.mapper.CommonMapper;
import com.jaha.server.emaul.model.AppVersion;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.model.PushLog;
import com.jaha.server.emaul.repo.AppVersionRepository;
import com.jaha.server.emaul.repo.FileInfoRepository;
import com.jaha.server.emaul.repo.PushLogRepository;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by doring on 15. 4. 2..
 */
@Service
public class CommonServiceImpl implements CommonService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppVersionRepository appVersionRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private PushLogRepository pushLogRepository;

    @Autowired
    private CommonMapper commonMapper;

    @Override
    public AppVersion getAppVersion(String kind) {
        return appVersionRepository.findByKind(kind);
    }

    /**
     * @author shavrani 2016.05.31
     */
    @Override
    public FileInfo getFileInfo(Long fileKey) {
        return fileInfoRepository.findByFileKey(fileKey);
    }

    /**
     * @author shavrani 2016.05.31
     */
    @Override
    public FileInfo getFileInfo(String category, Object fileGroupKey, Long fileKey) {
        return fileInfoRepository.findByCategoryAndFileGroupKeyAndFileKey(category, StringUtil.nvl(fileGroupKey), fileKey);
    }

    /**
     * @author shavrani 2016.05.31
     */
    @Override
    public List<FileInfo> getFileGroup(String category, Object fileGroupKey) {
        return fileInfoRepository.findByCategoryAndFileGroupKey(category, StringUtil.nvl(fileGroupKey));
    }

    /**
     * @author shavrani 2016.05.31
     */
    @Override
    public List<FileInfo> getFileGroup(String category, Object fileGroupKey, String type) {
        return fileInfoRepository.findByCategoryAndFileGroupKeyAndType(category, StringUtil.nvl(fileGroupKey), type);
    }

    @Override
    public ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, String gubun, Integer count) throws Exception {
        ScrollPage<PushLog> body = null;

        if (lastPushId == null || lastPushId == 0L) {
            lastPushId = Long.MAX_VALUE;
        }

        // if (StringUtils.isNotBlank(gubun)) {
        // gubun = "parcel-ad";
        // }

        if (count == null || count == 0) {
            count = 5;
        }

        try {
            List<PushLog> pushLogList = this.pushLogRepository.findByIdLessThanAndUserIdOrderByIdDesc(lastPushId, userId);
            List<PushLog> returnPushLogList = new ArrayList<PushLog>();

            int size = 0;
            int returnPushLogListSize = 0;

            if (pushLogList != null && pushLogList.size() > 0) {
                size = pushLogList.size();

                if (count >= size) {
                    count = size;
                }

                for (int i = 0; i < count; i++) {
                    returnPushLogList.add(pushLogList.get(i));
                }

                returnPushLogListSize = returnPushLogList.size();
            }

            body = new ScrollPage<>();
            body.setContent(returnPushLogList);
            // body.setPageNumber(pageable.getPageNumber());
            body.setTotalCount(size);

            if (size >= count && returnPushLogListSize > 0) {
                long lastIdOfPushList = pushLogList.get(size - 1).getId();
                long lastIdOfReturnPushList = returnPushLogList.get(returnPushLogListSize - 1).getId();

                if (lastIdOfPushList != lastIdOfReturnPushList) {
                    body.setNextPageToken(String.valueOf(returnPushLogList.get(returnPushLogListSize - 1).getId()));
                }
            }
        } catch (Exception e) {
            logger.error("<<푸시로그 목록 조회 중 오류 발생>>", e);
        }

        return body;
    }

    @Override
    public List<Map<String, Object>> getCodeList(CommonCode commonCode) {
        return commonMapper.selectCodeList(commonCode);
    }

    @Override
    public Date selectDate() {
        return commonMapper.selectDate();
    }

    @Override
    public int saveAppPageViewLog(Map<String, Object> params) {
        return commonMapper.saveAppPageViewLog(params);
    }

}
