package com.jaha.server.emaul.controller;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaha.server.emaul.config.LiteDevice;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponses;
import com.jaha.server.emaul.model.AppVersion;
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.model.PushLog;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.CommonService;
import com.jaha.server.emaul.service.ParcelService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.util.Util;

/**
 * Created by doring on 15. 4. 2..
 */
@Controller
public class CommonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private CommonService commonService;

    @Autowired
    private UserService userService;

    @Value("${file.path.editor.image}")
    private String editorImagePath;

    @RequestMapping(method = RequestMethod.GET, value = "/api/public/common/app-version/{kind}")
    public @ResponseBody AppVersion handleAppVersionRequest(@PathVariable(value = "kind") String kind) {
        return commonService.getAppVersion(kind);
    }

    /**
     * @author shavrani 2016.05.31
     */
    @RequestMapping(value = "/api/common/editor/image/{middlePath}/{id}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> editorImageView(@PathVariable("middlePath") String middlePath, @PathVariable("id") String id, @PathVariable("fileName") String fileName) {

        File toServeUp = new File(String.format(editorImagePath + "/%s/%s", middlePath, id), fileName);

        return Responses.getFileEntity(toServeUp, fileName);
    }



    @Autowired
    private ParcelService parcelService;

    @RequestMapping(value = "/api/public/push-rec-check")
    public @ResponseBody String handlePushRecCheckRequest(HttpServletRequest request, LiteDevice device, @RequestParam(value = "pushIds", required = false) String pushIds, @RequestParam(
            value = "deviceType", required = false, defaultValue = "IOS") String deviceType) {
        LOGGER.info("<</api/public/push-click-check, 디바이스 타입: {}, 디바이스 플랫폼: {}>>", device.getDeviceType().name(), device.getDevicePlatform().name()); // 브라우저만 인식

        if (StringUtils.isNotBlank(pushIds)) {
            try {
                String[] temps = pushIds.split("[,]", -1);
                for (String temp : temps) {
                    if (StringUtils.isNotBlank(temp)) {
                        Long pushId = Long.valueOf(temp);
                        this.parcelService.modifyDeviceRecYn(pushId, deviceType);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("<<푸시 수신 확인 처리 중 오류 발생>>", e);
            }
        }

        return "OK";
    }

    @RequestMapping(value = "/api/public/push-click-check")
    public @ResponseBody ApiResponse<?> handlePushClickCheckRequest(HttpServletRequest request, LiteDevice device, @RequestParam(value = "pushId", required = false) String pushId, @RequestParam(
            value = "deviceType", required = false, defaultValue = "IOS") String deviceType) {
        // LOGGER.info("<</api/public/push-click-check, 디바이스 타입: {}, 디바이스 플랫폼: {}>>", device.getDeviceType().name(), device.getDevicePlatform().name()); // 브라우저만 인식

        ApiResponse<PushLog> ar = new ApiResponse<>();

        try {
            if (StringUtils.isNotBlank(pushId)) {
                Long pId = Long.valueOf(pushId);
                PushLog pushLog = this.parcelService.modifyPushClickCount(pId, deviceType);
                ar.setBody(pushLog);
            }
        } catch (Exception e) {
            LOGGER.error("<<푸시 클릭 확인 처리 중 오류 발생>>", e);
        }

        return ar;
    }

    /**
     * 무인택배함 푸시 미수신 사용자에게 재발송(배치에서 사용)
     *
     * @param pushId
     * @return
     */
    @RequestMapping(value = "/api/public/push-resend")
    public @ResponseBody String handlePushResendRequest() {
        try {
            String nextDatetime = Util.getNextDatetime(-10);
            this.parcelService.resendPush("N", Util.convertString2Date(nextDatetime));
        } catch (Exception e) {
            LOGGER.error("<<푸시 미수신 사용자에게 푸시 재발송 중 오류 발생>>", e);
        }

        return "OK";
    }

    /**
     * 장기미수취 택배, 관리자에게 푸시발송, 배치에서 사용, 하루 2번 오전10시/오후7시에 발송됨
     *
     * @param pushId
     * @return
     */
    @RequestMapping(value = "/api/public/push-send-to-admin")
    public @ResponseBody String handlePushSendToAdminRequest() {
        try {
            // this.parcelService.sendPush4Admin();
        } catch (Exception e) {
            LOGGER.error("<<장기미수취 택배 관리자에게 푸시 발송 중 오류 발생>>", e);
        }

        return "OK";
    }

    @RequestMapping(value = "/api/public/push-list")
    public @ResponseBody ApiResponse<?> handlePushClickCheckRequest(HttpServletRequest request, @RequestParam(value = "gubun", required = false) String gubun, @RequestParam(value = "userId",
            required = false) Long userId, @RequestParam(value = "lastPushId", required = false) Long lastPushId, @RequestParam(value = "count", required = false, defaultValue = "5") Integer count,
            @PageableDefault(sort = {"modDate"}, direction = Direction.DESC, size = 10) Pageable pageable) {
        Long uId = SessionAttrs.getUserId(request.getSession());
        if (uId == null) {
            uId = userId;
        }
        // LOGGER.info("<<푸시로그목록조회 사용자 아이디>> {}", uId);
        // User user = userService.getUser(uId);

        ApiResponse<ScrollPage<PushLog>> ar = new ApiResponse<>();

        try {
            ScrollPage<PushLog> pushLogPage = this.commonService.findPushList(lastPushId, uId, gubun, count);
            ar.setBody(pushLogPage);
        } catch (Exception e) {
            LOGGER.error("<<푸시로그 목록 조회 중 오류 발생>>", e);
        }

        return ar;
    }

    /**
     * <pre>
     * 1. 작성일 : 2016. 4. 14. 오후 2:15:03
     * 2. 작성자 : realsnake
     * 3. 설명 : 이미지 뷰
     * </pre>
     *
     * @param request
     * @param response
     * @param fileKey
     * @return
     */
    @RequestMapping(value = "/api/public/image-view/{fileKey}")
    public void findAndViewImage(HttpServletRequest request, HttpServletResponse response, @PathVariable Long fileKey) {
        try {
            FileInfo fileInfo = this.commonService.getFileInfo(fileKey);

            if (fileInfo == null) {
                LOGGER.info("<<공통 이미지 뷰 - FileInfo not retreived!>>");
            } else {
                Util.viewImage(fileInfo, request, response);
            }
        } catch (Exception e) {
            LOGGER.error("<<공통 이미지 뷰 에러>>", e);
        }
    }

    /**
     * App 페이지별 노출 저장
     * 
     * @author shavrani 2017.03.10
     */
    @RequestMapping(value = "/api/page-view/log")
    @ResponseBody
    public ApiResponses appPageViewLog(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        ApiResponses apiResponse = new ApiResponses();

        String pageCode = StringUtil.nvl(params.get("pageCode"));

        if (StringUtil.isBlank(pageCode)) {
            LOGGER.info(" # pageCode parameter is null");
            apiResponse.resultCode("99");
            apiResponse.resultMessage("Required parameter is null");
            return apiResponse;
        }

        params.put("userId", user.id);
        int result = commonService.saveAppPageViewLog(params);

        if (result > 0) {
            apiResponse.data("Y");
        } else {
            apiResponse.resultCode("98");
            apiResponse.resultMessage("page view log save fail");
        }

        return apiResponse;
    }

}
