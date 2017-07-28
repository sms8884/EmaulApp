package com.jaha.server.emaul.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaha.server.emaul.constants.Constants;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.FileInfo;
import com.jaha.server.emaul.model.Provision;
import com.jaha.server.emaul.model.SystemFaq;
import com.jaha.server.emaul.model.SystemNotice;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.CommonService;
import com.jaha.server.emaul.service.SystemService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by shavrani on 16. 06. 09..
 */
@Controller
public class SystemController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private SystemService systemService;


    /**
     * @author shavrani 2016.06.13
     */
    @RequestMapping(value = "/api/system-notice/list-data")
    @ResponseBody
    public ApiResponse<?> userSystemNoticeListData(HttpServletRequest req, Model model, @RequestParam Map<String, Object> params) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        params.put("status", "1");// 게시상태의 데이터만 조회
        List<String> viewServices = new ArrayList<String>();// 전체이거나 mobileType이 포함된 항목만 조회
        viewServices.add("1");// 전체

        // mobileType ==> 2 : IOS, 3 : Android, 4 : Web
        if ("ios".equals(user.kind)) {
            viewServices.add("2");
        } else if ("android".equals(user.kind)) {
            viewServices.add("3");
        }

        params.put("viewServices", viewServices);
        ScrollPage<SystemNotice> result = systemService.getSystemNoticeList(user, params);

        ApiResponse<ScrollPage<SystemNotice>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(result);

        return apiResponse;
    }

    /**
     * @author shavrani 2016.06.13
     */
    @RequestMapping(value = "/api/system-notice/form-data")
    @ResponseBody
    public ApiResponse<?> userSystemNoticeFormData(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        Map<String, Object> result = new HashMap<String, Object>();

        String id = StringUtil.nvl(params.get("id"));

        if (StringUtil.isBlank(id)) {
            apiHeader.setResultCode("99");
            apiHeader.setResultMessage("parameter 'id' is null");
            logger.info(" # " + apiHeader.getResultMessage());
        } else {
            params.put("status", "1");// 게시상태의 데이터만 조회
            List<String> viewServices = new ArrayList<String>();// 전체이거나 mobileType이 포함된 항목만 조회
            viewServices.add("1");// 전체

            // mobileType ==> 2 : IOS, 3 : Android, 4 : Web
            if ("ios".equals(user.kind)) {
                viewServices.add("2");
            } else if ("android".equals(user.kind)) {
                viewServices.add("3");
            }

            params.put("viewServices", viewServices);

            SystemNotice systemNotice = systemService.getSystemNotice(user, params);

            if (systemNotice == null) {
                apiHeader.setResultCode("99");
                apiHeader.setResultMessage("data detail is null OR viewServices restricted access ");
                logger.info(" # " + apiHeader.getResultMessage());
            } else {
                List<FileInfo> fileList = commonService.getFileGroup(Constants.FILE_CATEGORY_NOTICE, systemNotice.id);
                if (fileList != null) {
                    for (int i = 0; i < fileList.size(); i++) {
                        FileInfo fileInfo = fileList.get(i);
                        fileInfo.downUrl = "/api/system-notice/file-down?id=" + systemNotice.id + "&fileKey=" + fileInfo.fileKey;
                    }
                }
                result.put("fileList", fileList);
            }

            result.put("data", systemNotice);
        }

        apiResponse.setBody(result);

        return apiResponse;
    }

    /**
     * @author shavrani 2016.09.09
     */
    @RequestMapping(value = "/api/system-notice/new-list-count")
    @ResponseBody
    public ApiResponse<?> userSystemNoticeMoreListCount(HttpServletRequest req, Model model, @RequestParam Map<String, Object> params) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String firstPageToken = StringUtil.nvl(params.get("firstPageToken"));

        if ("".equals(firstPageToken)) {
            ApiResponse<String> apiResponse = new ApiResponse<>("99", "firstPageToken parameter is null");
            return apiResponse;
        }

        params.put("status", "1");// 게시상태의 데이터만 조회
        List<String> viewServices = new ArrayList<String>();// 전체이거나 mobileType이 포함된 항목만 조회
        viewServices.add("1");// 전체

        // mobileType ==> 2 : IOS, 3 : Android, 4 : Web
        if ("ios".equals(user.kind)) {
            viewServices.add("2");
        } else if ("android".equals(user.kind)) {
            viewServices.add("3");
        }

        params.put("viewServices", viewServices);
        int cnt = systemService.getSystemNoticeListCount(user, params);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("count", cnt);
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(result);

        return apiResponse;
    }

    /**
     * @author shavrani 2016.06.13
     */
    @RequestMapping(value = "/api/system-notice/file-down")
    public ResponseEntity<byte[]> userSystemNoticeFileDown(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String id = StringUtil.nvl(params.get("id"));

        if (StringUtil.isBlank(id)) {
            logger.info(" # parameter 'id' is null");
        } else {
            params.put("status", "1");// 게시상태의 데이터만 조회
            List<String> viewServices = new ArrayList<String>();// 전체이거나 mobileType이 포함된 항목만 조회
            viewServices.add("1");// 전체

            // mobileType ==> 2 : IOS, 3 : Android, 4 : Web
            if ("ios".equals(user.kind)) {
                viewServices.add("2");
            } else if ("android".equals(user.kind)) {
                viewServices.add("3");
            }

            params.put("viewServices", viewServices);

            SystemNotice systemNotice = systemService.getSystemNotice(user, params);
            if (systemNotice == null) {
                logger.info(" # data detail is null OR viewServices restricted access ");
            } else {
                /** 해당글의 fileGroupKey와 fileKey조합이 맞아야만 다운로드 되게 처리. */
                Long fileKey = StringUtil.nvlLong(params.get("fileKey"));
                if (fileKey > 0) {
                    FileInfo fileInfo = commonService.getFileInfo(Constants.FILE_CATEGORY_NOTICE, systemNotice.id, fileKey);
                    return Responses.getFileEntity(fileInfo.getFile(), fileInfo.fileOriginName);
                } else {
                    logger.info(" # parameter 'fileKey' is null");
                }
            }
        }
        return null;
    }

    /**
     * @author shavrani 2016.06.13
     */
    @RequestMapping(value = "/api/system-faq/list-data")
    @ResponseBody
    public ApiResponse<?> userSystemFaqListData(HttpServletRequest req, Model model, @RequestParam Map<String, Object> params) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        params.put("status", "1");// 게시상태의 데이터만 조회
        List<String> viewServices = new ArrayList<String>();// 전체이거나 mobileType이 포함된 항목만 조회
        viewServices.add("1");// 전체

        // mobileType ==> 2 : IOS, 3 : Android, 4 : Web
        if ("ios".equals(user.kind)) {
            viewServices.add("2");
        } else if ("android".equals(user.kind)) {
            viewServices.add("3");
        }

        params.put("viewServices", viewServices);
        ScrollPage<SystemFaq> result = systemService.getSystemFaqList(user, params);

        ApiResponse<ScrollPage<SystemFaq>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(result);

        return apiResponse;
    }

    /**
     * @author shavrani 2016.06.13
     */
    @RequestMapping(value = "/api/system-faq/form-data")
    @ResponseBody
    public ApiResponse<?> userSystemFaqFormData(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        Map<String, Object> result = new HashMap<String, Object>();
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        String id = StringUtil.nvl(params.get("id"));

        SystemFaq systemFaq = null;
        if (StringUtil.isBlank(id)) {
            apiHeader.setResultCode("99");
            apiHeader.setResultMessage("parameter 'id' is null");
            logger.info(" # " + apiHeader.getResultMessage());
        } else {
            params.put("status", "1");// 게시상태의 데이터만 조회
            List<String> viewServices = new ArrayList<String>();// 전체이거나 mobileType이 포함된 항목만 조회
            viewServices.add("1");// 전체

            // mobileType ==> 2 : IOS, 3 : Android, 4 : Web
            if ("ios".equals(user.kind)) {
                viewServices.add("2");
            } else if ("android".equals(user.kind)) {
                viewServices.add("3");
            }

            params.put("viewServices", viewServices);

            systemFaq = systemService.getSystemFaq(user, params);

            if (systemFaq == null) {
                apiHeader.setResultCode("99");
                apiHeader.setResultMessage("data detail is null OR viewServices restricted access ");
                logger.info(" # " + apiHeader.getResultMessage());
            }

            result.put("data", systemFaq);
        }

        apiResponse.setBody(result);

        return apiResponse;
    }

    /**
     * 게시상태의 약관만 조회
     * 
     * @author shavrani 2016.06.13
     */
    @RequestMapping(value = "/api/public/provision/view-data")
    @ResponseBody
    public Provision provisionViewData(HttpServletRequest req, @RequestParam(value = "id") Long id) {
        Provision data = systemService.getSystemProvisionUseStatus(id, "1");
        return data;
    }

}
