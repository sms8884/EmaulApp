package com.jaha.server.emaul.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Maps;
import com.jaha.server.emaul.common.code.Code;
import com.jaha.server.emaul.constants.Constants;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.ApiResponses;
import com.jaha.server.emaul.model.AptAp;
import com.jaha.server.emaul.model.AptApAccessLog;
import com.jaha.server.emaul.model.AptApDaemonLog;
import com.jaha.server.emaul.model.AptApMonitoring;
import com.jaha.server.emaul.model.AptApMonitoringNoti;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.CommonService;
import com.jaha.server.emaul.service.EdoorService;
import com.jaha.server.emaul.service.GcmService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;

/**
 * @author shavrani
 * @since 2016. 9. 2.
 * @version 1.0
 */
@Controller
public class EdoorController {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;
    @Autowired
    private GcmService gcmService;
    @Autowired
    private EdoorService edoorService;
    @Autowired
    private CommonService commonService;

    @Autowired
    private Environment env;

    @Autowired
    public JavaMailSender mailSender;


    /**
     * apt ap skip user 권한체크및 셋팅
     */
    private List<String> getSkipAuthUser(User user) {
        List<String> skipAuths = new ArrayList<String>();
        if (user.type.jaha) {
            skipAuths.add("jaha");
        }
        if (user.type.admin) {
            skipAuths.add("admin");
        }
        if (user.type.gasChecker) {
            skipAuths.add("gasChecker");
        }
        if (user.type.buildingGuard) {
            skipAuths.add("buildingGuard");
        }
        if (user.type.communityMaster) {
            skipAuths.add("communityMaster");
        }
        if (user.type.houseHost) {
            skipAuths.add("houseHost");
        }
        if (user.type.official) {
            skipAuths.add("official");
        }
        if (user.type.parcelChecker) {
            skipAuths.add("parcelChecker");
        }
        return skipAuths;
    }

    /**
     * Created by shavrani on 16-06-23
     */
    @RequestMapping(value = "/api/apt/ap/access-list")
    @ResponseBody
    public List<AptAp> apiAptApAccessList(HttpServletRequest req) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
            return null;
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("aptId", user.house.apt.id);
            params.put("dong", user.house.dong);
            params.put("ho", user.house.ho);
            params.put("userId", user.id);
            params.put("includedOperationMode", Code.APP_AP_OPEN_TYPE_SERVER.getCode());// 구버전 출입 권한 가능 AP목록은 서버형이 포함된 AP만 검색한다.
            params.put("_active", true);

            List<String> skipAuths = getSkipAuthUser(user);
            if (skipAuths.size() > 0) {
                params.put("skipAuths", skipAuths);
            }

            String _apPattern = "J" + StringUtil.leftPad(String.valueOf(user.house.apt.id), 5, "0");
            params.put("_apPattern", _apPattern);
            return edoorService.selectAptApAccessList(params);
        }

    }

    /**
     * Created by shavrani on 16-06-23 (2016-08-10 이전 앱에서 호출하는 version)
     */
    @RequestMapping(value = "/api/apt/ap/access-log-save")
    @Transactional
    @ResponseBody
    public String saveAptApAccessLog(HttpServletRequest req, @RequestParam(value = "id") String id, @RequestParam(value = "success", required = false) String success, @RequestParam(
            value = "accessDate", required = false) String accessDate, @RequestParam(value = "mobileDeviceModel", required = false) String mobileDeviceModel, @RequestParam(value = "mobileDeviceOs",
            required = false) String mobileDeviceOs, @RequestParam(value = "delayTime", required = false) int delayTime) {

        String result = "-1";

        if (!StringUtil.isEmpty(id)) {

            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);
            // params.put("_active", true); //혹여라도 삭제처리된 항목이 조회될경우 memo해주기위해 _active 검색조건 주석처리.
            AptAp aptAp = edoorService.selectAptAp(params);

            // e도어가 오픈해서 log저장을 호출한것이기때문에 ap존재유무와 상관없이 로그는 저장.
            // if(aptAp != null){
            AptApAccessLog aptApAccessLog = new AptApAccessLog();
            aptApAccessLog.apId = id;
            aptApAccessLog.userId = user.id;
            aptApAccessLog.success = success;
            aptApAccessLog.mobileDeviceModel = mobileDeviceModel;
            aptApAccessLog.mobileDeviceOs = mobileDeviceOs;
            aptApAccessLog.delayTime = delayTime;

            if (StringUtils.isEmpty(accessDate)) {
                aptApAccessLog.accessDate = new Date();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    aptApAccessLog.accessDate = sdf.parse(accessDate);
                } catch (Exception e) {
                    aptApAccessLog.accessDate = new Date();
                    logger.info(" /api/apt/ap/access-log-save date parse Exceltion !! ");
                }
            }

            if (aptAp == null) {
                aptApAccessLog.memo = id + "번 AP가 존재하지않음.";
            } else {
                if (aptAp.deactiveDate != null) {
                    aptApAccessLog.memo = id + "번 AP가 삭제처리된 항목.";
                }
            }

            edoorService.saveAptApAccessLog(aptApAccessLog);

            /*
             * 
             * //알림대상일경우 출입시 보호자에게 push boolean useNoti = false; if(useNoti == true){
             * 
             * String titleMsg = ""; if(aptAp == null){ titleMsg = user.getFullName() + "님이 출입하였습니다."; } else { titleMsg = user.getFullName() + "님이 "+aptAp.aptName+"의 "+aptAp.apName+"에 출입하였습니다."; }
             * 
             * GcmSendForm form = new GcmSendForm(); Map<String, String> msg = Maps.newHashMap(); msg.put("type", "action");*-
             * 
             * msg.put("title", titleMsg); msg.put("value", "content"); //msg.put("action", "emaul://test?id="); List<Long> userList = null;// 연락받을 보호자 form.setUserIds(userList); form.setMessage(msg);
             * gcmService.sendGcm(form); }
             */

            result = "1";
            // }
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-22 ( 2016-08-22 이후 앱에서 요청하는 version )
     */
    @RequestMapping(value = "/api/apt/ap/access/request")
    @ResponseBody
    public String aptApAccessRequest(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        logger.info(" # [ {} ] {} 유저의 e-door 요청 [ apBeaconUuid : {} ]", user.id, user.getFullName(), apBeaconUuid);

        String result = "N";

        // parameter가 있는지 확인
        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
        } else {
            result = edoorService.aptApAccessRequest(user, params);
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-10 ( 아파트 AP 에서 접속하는 앱의 open 요청건 유무 ) AP에서 요청하는거라 /api/public 이어야한다. AP 에서 Polling 하는 요청 ( 사용안함. 2016-08-22 )
     */
    @RequestMapping(value = "/api/public/apt/ap/access/request/exist")
    @ResponseBody
    public String aptApAccessRequestExist(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        String result = "N";
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
        } else {
            result = edoorService.aptApAccessRequestExist(params);
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-11 ( 아파트 AP ( access device )에서 접속하는 권한체크 ) AP에서 요청하는거라 /api/public 이어야한다. ( 사용안함. 2016-08-22 )
     */
    @RequestMapping(value = "/api/public/apt/ap/access/device/check")
    @ResponseBody
    public String aptApAccessDevice(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        String result = "N";
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
        } else {
            result = edoorService.aptApAccessDevice(params);
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-17 ( 아파트 AP 에서 앱요청으로 open하고 난후 process ) AP에서 요청하는거라 /api/public 이어야한다.
     */
    @RequestMapping(value = "/api/public/apt/ap/access/open/success")
    @ResponseBody
    public String aptApAccessOpenSuccess(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        String result = "N";
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
        } else {
            result = edoorService.aptApAccessOpenSuccess(params);
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-10 ( 아파트 AP ( access device )에서 접속하는 device list ) AP에서 요청하는거라 /api/public 이어야한다. 파일로 다운
     */
    @RequestMapping(value = "/api/public/apt/ap/access/device/list/file")
    public ResponseEntity<byte[]> aptApAccessDeviceList(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String lastUpdateTime = StringUtil.nvl(params.get("lastUpdateTime"));

        params.put("_active", true);
        AptAp aptAp = edoorService.selectAptAp(params);

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
            return null;
        }

        if (aptAp == null) {
            logger.info("ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
            return null;
        }

        List<Map<String, Object>> aptApAccessDeviceAuthList = edoorService.selectAptApAccessDeviceAuthList(params);

        if (aptApAccessDeviceAuthList != null && aptApAccessDeviceAuthList.size() > 0) {

            String replaceUuid = apBeaconUuid.replace(":", "");// 파일명에는 ':' 이 들어갈수없음. ( 윈도우에서만 )

            String fileName = "device-list-" + replaceUuid + "-" + System.currentTimeMillis();

            String jsonData = new JSONArray(aptApAccessDeviceAuthList).toString();

            FileWriter fw = null;
            BufferedWriter bw = null;
            File file = null;
            File dir = null;
            try {
                String path = String.format("/nas/EMaul/aptAp/%s/%s", aptAp.aptId, replaceUuid);
                dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File[] files = dir.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        files[i].delete();
                    }
                }

                file = new File(dir, fileName + ".txt");
                fw = new FileWriter(file);
                bw = new BufferedWriter(fw);

                // bw.write("20160811120000");
                // bw.newLine();
                bw.write(jsonData);

            } catch (Exception e) {
                logger.error("", e);
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            File gzipFile = new File(dir, fileName + ".gz");

            // gzip 압축
            if (file != null && dir != null) {
                GZIPOutputStream gout = null;
                FileOutputStream fos = null;
                FileInputStream fis = null;
                try {
                    fos = new FileOutputStream(gzipFile);
                    gout = new GZIPOutputStream(fos);
                    fis = new FileInputStream(file);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = fis.read(buf)) != -1) {
                        gout.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                } finally {
                    if (gout != null) {
                        try {
                            gout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                file.delete(); // txt 파일은 삭제
            }

            return Responses.getFileEntity(gzipFile, gzipFile.getName());

        }

        return null;

    }

    /**
     * Created by shavrani on 16-08-10 ( 아파트 AP ( access device )에서 접속하는 device list ) AP에서 요청하는거라 /api/public 이어야한다. 바이트로 전송
     */
    @RequestMapping(value = "/api/public/apt/ap/access/device/list/byte")
    public ResponseEntity<byte[]> aptApAccessDeviceListByte(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String lastUpdateTime = StringUtil.nvl(params.get("lastUpdateTime"));

        params.put("_active", true);
        AptAp aptAp = edoorService.selectAptAp(params);

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
            return null;
        }

        if (aptAp == null) {
            logger.info("ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
            return null;
        }

        List<Map<String, Object>> aptApAccessDeviceAuthList = edoorService.selectAptApAccessDeviceAuthList(params);

        if (aptApAccessDeviceAuthList != null && aptApAccessDeviceAuthList.size() > 0) {

            String jsonData = new JSONArray(aptApAccessDeviceAuthList).toString();

            byte[] result = null;

            ByteArrayOutputStream bos = null;
            GZIPOutputStream gos = null;
            BufferedOutputStream bfos = null;

            try {

                bos = new ByteArrayOutputStream();
                gos = new GZIPOutputStream(bos);
                bfos = new BufferedOutputStream(gos);

                bfos.write(jsonData.getBytes());

            } catch (Exception e) {
                logger.error("", e);
            } finally {
                if (bfos != null) {
                    try {
                        bfos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (gos != null) {
                    try {
                        gos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (bos != null) {
                result = bos.toByteArray();
                logger.info("원본 byte array length : " + jsonData.getBytes().length);
                logger.info("압축 byte array length : " + result.length);
            }

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        }

        return null;

    }

    /**
     * 테스트용 api ( /api/public/apt/ap/access/device/list/byte 결과 테스트 )
     */
    @RequestMapping(value = "/api/public/apt/ap/access/device/list/byte/read")
    @ResponseBody
    public String aptApAccessDeviceListByteRead(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        String result = "";

        String apiStr = "";
        String requestURL = req.getRequestURL().toString();
        URL url = null;
        try {
            url = new URL(requestURL);
            apiStr = "http://" + url.getHost() + ":" + url.getPort();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        try {
            URL apiUrl = new URL(apiStr + "/api/public/apt/ap/access/device/list/byte?apBeaconUuid=" + params.get("apBeaconUuid"));
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();

            conn.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");


            ByteArrayInputStream bis = null;
            GZIPInputStream gis = null;
            BufferedInputStream bfis = null;
            ByteArrayOutputStream bos = null;

            try {
                byte[] connByte = IOUtils.toByteArray(conn.getInputStream());
                if (connByte != null && connByte.length > 0) {
                    bis = new ByteArrayInputStream(connByte);
                    gis = new GZIPInputStream(bis);
                    // bfis = new BufferedInputStream(gis);
                    bos = new ByteArrayOutputStream();

                    byte[] buf = new byte[100];
                    int len;
                    while ((len = gis.read(buf, 0, buf.length)) != -1) {
                        bos.write(buf, 0, len);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bfis != null) {
                    try {
                        bfis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (gis != null) {
                    try {
                        gis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (bos != null) {
                result = new String(bos.toByteArray());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-12 ( 아파트 AP 에서 접속하는 beacon의 위치추적용 log ) AP에서 요청하는거라 /api/public 이어야한다. url에서 apt는 제거 ( 나중에 통합용으로 사용가능한 항목이라서 )
     */
    @RequestMapping(value = "/api/public/ap/access/location/save")
    @ResponseBody
    public String apAccessLocationLogBeacon(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        /**
         * 통신상의 오류로 appear와 disappear의 짝이 안맞는건 고려하지 않는다.
         */

        String result = "N";
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String accessKey = StringUtil.nvl(params.get("accessKey"));
        String state = StringUtil.nvl(params.get("state"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
        } else if ("".equals(accessKey)) {
            logger.info(" # accessKey parameter is null");
        } else if ("".equals(state)) {
            logger.info(" # state parameter is null");
        } else {
            params.put("_active", true);
            result = edoorService.saveApAccessLocationLogBeacon(params);
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-12 ( 아파트 AP 에서 접속하는 앱의 위치추적용 log ) url에서 apt는 제거 ( 나중에 통합용으로 사용가능한 항목이라서 )
     */
    @RequestMapping(value = "/api/ap/access/location/save")
    @ResponseBody
    public String apAccessLocationLogApp(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        /**
         * 통신상의 오류로 appear와 disappear의 짝이 안맞는건 고려하지 않는다.
         */

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
            return null;
        }

        params.put("userId", user.id);

        String result = "N";

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String state = StringUtil.nvl(params.get("state"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
        } else if ("".equals(state)) {
            logger.info(" # state parameter is null");
        } else {
            result = edoorService.saveApAccessLocationLogApp(params);
        }

        return result;
    }

    /**
     * ap에서 호출하는 ap 자신의 ip 저장
     *
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/public/apt/ap/ip/save")
    @ResponseBody
    public String aptApIpSave(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String apExpIp = StringUtil.nvl(params.get("apExpIp"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
            return "N";
        }
        if ("".equals(apExpIp)) {
            logger.info(" # apExpIp parameter is null");
            return "N";
        }
        return edoorService.saveAptApExpIp(params);
    }

    /**
     * ap에서 호출하는 ap 자신을 등록및 수정
     *
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/public/apt/ap/save")
    @ResponseBody
    public ApiResponse<?> aptApSave(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        ApiResponseHeader header = apiResponse.getHeader();

        Map<String, Object> result = edoorService.saveAptAp(params);

        if ("00".equals(result.get("resultCode"))) {
            result.remove("resultCode");
            apiResponse.setBody(result);
        } else {
            header.setResultCode(StringUtil.nvl(result.get("resultCode")));
            header.setResultMessage(StringUtil.nvl(result.get("resultMessage")));
        }

        return apiResponse;
    }

    /**
     * ap에서 호출하는 ap 기본셋팅 데이터
     *
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/public/apt/ap/default/config")
    @ResponseBody
    public ApiResponse<?> aptApDefaultConfig(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        // application.properties 에 설정한 기본 ap 설정값을 셋팅한다.
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("beconMajor", StringUtil.nvl(env.getProperty("ap.config.beconMajor")));
        result.put("beconMinor", StringUtil.nvl(env.getProperty("ap.config.beconMinor")));
        result.put("eDoorWiFiName", StringUtil.nvl(env.getProperty("ap.config.eDoorWiFiName")));
        result.put("eDoorName", StringUtil.nvl(env.getProperty("ap.config.eDoorName")));
        result.put("eDoorPasswd", StringUtil.nvl(env.getProperty("ap.config.eDoorPasswd")));
        result.put("timeServerUrl", StringUtil.nvl(env.getProperty("ap.config.timeServerUrl")));
        result.put("rssi", StringUtil.nvl(env.getProperty("ap.config.rssi")));
        result.put("reOpenDelay", StringUtil.nvl(env.getProperty("ap.config.reOpenDelay")));
        result.put("gpiodelay", StringUtil.nvl(env.getProperty("ap.config.gpiodelay")));

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(result);

        return apiResponse;

    }

    /**
     * ap에서 호출하는 ap내의 daemon dead 코드 로그 저장
     *
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/public/apt/ap/daemon/dead/log")
    @ResponseBody
    public String aptApDaemonDeadLog(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        String result = "N";
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String code = StringUtil.nvl(params.get("code"));
        String message = StringUtil.nvl(params.get("message"));

        params.clear();
        params.put("apBeaconUuid", apBeaconUuid);
        params.put("_active", true);
        AptAp aptAp = edoorService.selectAptAp(params);

        if (aptAp == null) {
            logger.info("ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
            return result;
        }

        AptApDaemonLog aadl = new AptApDaemonLog();
        aadl.apId = aptAp.id;
        aadl.code = code;
        aadl.message = message;
        aadl.regDate = new Date();

        AptApDaemonLog resultAadl = edoorService.saveAptApDaemonLog(aadl);
        if (resultAadl != null) {
            result = "Y";
        }

        return result;

    }

    /**
     * ap 의 외부망 ip 체크
     *
     * @return
     */
    @RequestMapping(value = "/api/public/apt/ap/ip/read")
    @ResponseBody
    public String aptApIpRead(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        return req.getRemoteAddr();
    }

    /**
     * 유저의 비콘기기 목록
     *
     * @return
     */
    @RequestMapping(value = "/api/apt/ap/user/beacon/list")
    @ResponseBody
    public ApiResponse<?> aptApUserBeaconList(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
            apiHeader.setResultCode("99");
            apiHeader.setResultMessage("auth fail");
            return apiResponse;
        }

        params.put("userId", user.id);
        params.put("_active", true);
        List<Map<String, Object>> aaadList = edoorService.selectAptApAccessDeviceList(params);
        for (int i = 0; i < aaadList.size(); i++) {
            Map<String, Object> item = aaadList.get(i);
            String accessKey = StringUtil.nvl(item.get("accessKey"));
            String macAddress = accessKey;
            if (accessKey.length() > 0 && accessKey.indexOf(":") == -1) {
                macAddress = accessKey.replaceAll("(..)(?!$)", "$1:");
            }
            item.put("macAddress", macAddress);

        }
        apiResponse.setBody(aaadList);

        return apiResponse;
    }

    /**
     * TEST api e킨더 mock up
     *
     * @param req
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/public/ap/access/ekmockup/action")
    @ResponseBody
    public String apAccessBeaconMockupPush(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        String result = "N";
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String accessKey = StringUtil.nvl(params.get("accessKey"));
        String state = StringUtil.nvl(params.get("state"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
        } else if ("".equals(accessKey)) {
            logger.info(" # accessKey parameter is null");
        } else if ("".equals(state)) {
            logger.info(" # state parameter is null");
        } else {
            result = edoorService.apAccessBeaconMockupPush(params);
        }

        return result;
    }

    /**
     * ap monitoring daily data
     */
    @RequestMapping(value = "/api/public/ap/monitoring/save")
    @ResponseBody
    public ApiResponse<?> saveApMonitoring(HttpServletRequest req, AptApMonitoring aptApMonitoring) {

        ApiResponse<String> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        if (!StringUtil.isBlank(aptApMonitoring.getApBeaconUuid())) {

            Map<String, Object> params = Maps.newHashMap();
            params.put("apBeaconUuid", aptApMonitoring.getApBeaconUuid());
            params.put("toDay", true);
            params.put("_active", true);
            AptAp aptAp = edoorService.selectAptAp(params);
            if (aptAp == null) {
                apiHeader.setResultCode("99");
                apiHeader.setResultMessage("empty ap");
                apiResponse.setBody("N");
                logger.info("<< AP가 존재하지않음 [ apBeaconUuid : {} ] >>", aptApMonitoring.getApBeaconUuid());
            } else {
                params.put("apId", aptAp.id);
                aptApMonitoring.setApId(aptAp.id);
                AptApMonitoring exist = edoorService.selectAptApMonitoring(params);
                if (exist != null) {
                    aptApMonitoring.setId(exist.getId());
                }

                // 중복은 업데이트처리. ( 하루에 한번만 insert )
                int result = edoorService.insertAptApMonitoring(aptApMonitoring);
                if (result > 0) {
                    apiResponse.setBody("Y");
                } else {
                    apiResponse.setBody("N");
                }
            }
        }

        return apiResponse;

    }

    /**
     * ap monitoring noti data
     */
    @RequestMapping(value = "/api/public/ap/monitoring/noti/save")
    @ResponseBody
    public ApiResponse<?> saveApMonitoringNoti(HttpServletRequest req, AptApMonitoringNoti aptApMonitoringNoti) {

        ApiResponse<String> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        if (!StringUtil.isBlank(aptApMonitoringNoti.getApBeaconUuid())) {

            Map<String, Object> params = Maps.newHashMap();
            params.put("apBeaconUuid", aptApMonitoringNoti.getApBeaconUuid());
            params.put("_active", true);

            AptAp aptAp = edoorService.selectAptAp(params);
            if (aptAp == null) {
                apiHeader.setResultCode("99");
                apiHeader.setResultMessage("empty ap");
                apiResponse.setBody("N");
                logger.info("<< AP가 존재하지않음 [ apBeaconUuid : {} ] >>", aptApMonitoringNoti.getApBeaconUuid());
            } else {
                aptApMonitoringNoti.setApId(aptAp.id);
                int result = edoorService.insertAptApMonitoringNoti(aptApMonitoringNoti);
                if (result > 0) {
                    apiResponse.setBody("Y");
                } else {
                    apiResponse.setBody("N");
                }
            }
        }

        return apiResponse;

    }

    /**
     * @author shavrani 2016.12.05
     */
    @RequestMapping(value = "/api/public/apt/ap/inspection/send-mail")
    @ResponseBody
    public String monitoringSendMail(HttpServletRequest req) {
        int result = edoorService.monitoringSendMailUserList();
        if (result == 1) {
            return "OK";
        }
        return "";
    }

    /**
     * @author shavrani 2017.01.03
     */
    @RequestMapping(value = "/api/public/apt/ap/inspection/no-history/send-mail")
    @ResponseBody
    public String monitoringNoHistoryApSendMail(HttpServletRequest req, @RequestParam(value = "hour", required = false) Integer hour) {
        int result = edoorService.monitoringNoHistoryApSendMail(hour);
        if (result == 1) {
            return "OK";
        }
        return "";
    }

    /**
     * @author shavrani 2017.01.11
     */
    @RequestMapping(value = "/api/public/apt/ap/monitoring/health/check")
    @ResponseBody
    public String aptApMonitoringHealthCheck(HttpServletRequest req, @RequestParam(value = "storagePeriod", required = false) Integer storagePeriod) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("aptSearchType", "notTestApt");
        params.put("testAptId", Constants.AP_TEST_APT_ID);
        params.put("excludeAptId", Constants.AP_EXCLUDE_APT_ID);
        params.put("existExpIp", "Y");
        params.put("includedOperationMode", Code.APP_AP_OPEN_TYPE_SERVER.getCode());// AP목록은 서버연동형이 포함된 AP만 검색한다.
        params.put("_active", true);

        List<AptAp> apList = edoorService.selectAptApList(params);

        // if (storagePeriod == null) {
        // storagePeriod = 30;// 데이터 유지일수. 현시간부터 설정한 일수이전 데이터는 삭제 ( null이면 기본 2달간으로 설정됨. )
        // }

        Map<String, Object> result = edoorService.aptApMonitoringHealthCheck(apList, storagePeriod);

        return "OK";
    }

    /**
     * 이도어 사용자 모니터링 배치용
     * 
     * @param req
     * @return
     */

    @RequestMapping(value = "/api/public/user/monitoring/list/batch")
    @ResponseBody
    public String aptApUserMonitoringListBatch(HttpServletRequest req) {

        edoorService.selectAptApUserMonitoringBatch();

        return "OK";
    }



    /**
     * ##############################################################################################################################################################################
     * #################################################################### 2017-02-01 이후 e-door api 개발 버전 ####################################################################
     * ##############################################################################################################################################################################
     */

    /**
     * 앱에서 ap open을 서버에 요청
     */
    @RequestMapping(value = "/api/ap/open/request")
    @ResponseBody
    public ApiResponses apOpenRequest(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        ApiResponses apiResponse = new ApiResponses();

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        logger.info(" # [ id : {}, name : {} ]  ap open request [ apBeaconUuid : {} ]", user.id, user.getFullName(), apBeaconUuid);

        String result = "N";

        // parameter가 있는지 확인
        if (StringUtil.isBlank(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
            apiResponse.resultCode("99");
            apiResponse.resultMessage("Required parameter is null");
        } else {
            result = edoorService.apOpenRequest(user, params);
            apiResponse.data(result);

            // ap open 요청 실패및 상태가 고장이면 헤더에 응답값 셋팅
            if ("N".equals(result)) {
                apiResponse.resultCode("98");
                apiResponse.resultMessage("[e도어] 요청 실패하였습니다\n이용에 불편을 드려 죄송합니다\n잠시 후 다시 시도해주시기 바랍니다");
            } else if ("X".equals(result)) {
                apiResponse.resultCode("97");
                apiResponse.resultMessage("[e도어] 점검 중인 기기입니다\n이용에 불편을 드려 죄송합니다\n빠르게 정상화되도록 최선을 다하겠습니다");
            }
        }

        return apiResponse;
    }

    /**
     * 앱에서 서버연동형이아닌 모드로 ap open 성공한후 결과 저장
     */
    @RequestMapping(value = "/api/ap/open/result/save")
    @ResponseBody
    public ApiResponses apOpenResultSave(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        ApiResponses apiResponse = new ApiResponses();

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String data = StringUtil.nvl(params.get("data"));

        String result = "N";

        // parameter가 있는지 확인
        if (StringUtil.isBlank(data)) {
            logger.info(" # data parameter is null");
            apiResponse.resultCode("99");
            apiResponse.resultMessage("Required parameter is null");
        } else {
            result = edoorService.saveApOpenResult(user, params);
            apiResponse.data(result);

            // 저장 실패
            if ("N".equals(result)) {
                apiResponse.resultCode("98");
                apiResponse.resultMessage("Save Fail");
            }
        }

        return apiResponse;
    }

    /**
     * 앱에서 서버연동형이아닌 모드로 ap open 성공한 앱의로컬 결과 저장 ( 앱이 데이터통신이 될때 일괄저장 )
     */
    @RequestMapping(value = "/api/ap/open/result/batch-save")
    @ResponseBody
    public ApiResponses apOpenResultBatchSave(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        ApiResponses apiResponse = new ApiResponses();

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String data = StringUtil.nvl(params.get("data"));// json array string

        String result = "N";

        if (StringUtil.isBlank(data)) {
            logger.info(" # data parameter is null");
            apiResponse.resultCode("99");
            apiResponse.resultMessage("Required parameter is null");
        } else {
            result = edoorService.batchSaveApOpenResult(user, params);
            apiResponse.data(result);

            // 저장 실패
            if ("N".equals(result)) {
                apiResponse.resultCode("98");
                apiResponse.resultMessage("Save Fail");
            }
        }

        return apiResponse;
    }

    /**
     * 앱에서 AP의 장애상태 파악후 전송
     */
    @RequestMapping(value = "/api/ap/report/failure")
    @ResponseBody
    public ApiResponses apReportFailure(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        ApiResponses apiResponse = new ApiResponses();

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        String result = "N";

        // parameter가 있는지 확인
        if (StringUtil.isBlank(apBeaconUuid)) {
            logger.info(" # apBeaconUuid parameter is null");
            apiResponse.resultCode("99");
            apiResponse.resultMessage("Required parameter is null");
        } else {
            result = edoorService.saveApBrokenLog(user, params);
            apiResponse.data(result);

            // 저장 실패
            if ("N".equals(result)) {
                apiResponse.resultCode("98");
                apiResponse.resultMessage("Save Fail");
            }
        }

        return apiResponse;
    }

    /**
     * 앱에서 선택할 AP OPEN TYPE 종류
     */
    @RequestMapping(value = "/api/ap/app/open-type")
    @ResponseBody
    public ApiResponses apOpenTypeList(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        ApiResponses apiResponse = new ApiResponses();

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        CommonCode code = new CommonCode();
        code.setCodeGroup("APP_AP_OPEN_TYPE");
        code.setUseYn("Y");
        List<Map<String, Object>> codeList = commonService.getCodeList(code);

        apiResponse.list(codeList);

        return apiResponse;
    }

    /**
     * AP 접근권한 가능 목록
     */
    @RequestMapping(value = "/api/ap/accessible/list")
    @ResponseBody
    public ApiResponses apAccessibleList(HttpServletRequest req) {

        ApiResponses apiResponse = new ApiResponses();

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
            apiResponse.resultCode("99");
            apiResponse.resultMessage("auth fail");
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("aptId", user.house.apt.id);
            params.put("dong", user.house.dong);
            params.put("ho", user.house.ho);
            params.put("userId", user.id);
            params.put("_active", true);

            List<String> skipAuths = getSkipAuthUser(user);
            if (skipAuths.size() > 0) {
                params.put("skipAuths", skipAuths);
            }

            String _apPattern = "J" + StringUtil.leftPad(String.valueOf(user.house.apt.id), 5, "0");
            params.put("_apPattern", _apPattern);

            apiResponse.list(edoorService.selectAptApAccessList(params));

        }

        return apiResponse;

    }

    /**
     * AP 정보 검색
     */
    @RequestMapping(value = "/api/public/ap/data")
    @ResponseBody
    public ApiResponses getApData(HttpServletRequest req, @RequestParam Map<String, Object> params) {

        ApiResponses apiResponse = new ApiResponses();

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        params.put("_active", true);

        if (StringUtil.isBlank(apBeaconUuid)) {
            apiResponse.resultCode("99");
            apiResponse.resultMessage("Required parameter is null");
        } else {

            AptAp aptAp = edoorService.selectAptAp(params);

            if (aptAp == null) {
                apiResponse.resultCode("98");
                apiResponse.resultMessage("AP not found [ apBeaconUuid : " + apBeaconUuid + " ]");
            } else {

                // AP에 필요한 정보만 골라서 전달 ( 통신량을 줄이기위함. )
                Map<String, Object> result = Maps.newHashMap();
                result.put("id", aptAp.id);
                result.put("apBeaconUuid", aptAp.apBeaconUuid);
                result.put("apPassword", aptAp.apPassword);
                result.put("sshPassword", aptAp.sshPassword);
                result.put("expIp", aptAp.expIp);
                result.put("modem", aptAp.modem);
                result.put("firmwareVersion", aptAp.firmwareVersion);
                result.put("natWay", aptAp.natWay);
                result.put("rssi", aptAp.rssi);
                result.put("keepon", aptAp.keepon);
                result.put("rssiApp", aptAp.rssiApp);
                result.put("operationMode", aptAp.operationMode);
                result.put("status", aptAp.status);
                result.put("gpiodelay", aptAp.gpiodelay);
                result.put("wifiMac", aptAp.wifiMac);

                apiResponse.data(result);
            }

        }

        return apiResponse;

    }

}
