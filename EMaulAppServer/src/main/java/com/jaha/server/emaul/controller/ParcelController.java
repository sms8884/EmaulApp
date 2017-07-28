package com.jaha.server.emaul.controller;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jaha.server.emaul.common.code.ParcelCode;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.GcmSendForm;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.ParcelLocker;
import com.jaha.server.emaul.model.ParcelNotification;
import com.jaha.server.emaul.model.PushLog;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.GcmService;
import com.jaha.server.emaul.service.HouseService;
import com.jaha.server.emaul.service.ParcelService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.Thumbnails;
import com.sybase.powerbuilder.cryptography.Base64;
import com.sybase.powerbuilder.cryptography.PBCrypto;

/**
 * Created by doring on 15. 5. 20..
 */
@Controller
public class ParcelController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParcelController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private ParcelService parcelService;
    @Autowired
    private GcmService gcmService;
    @Autowired
    private HouseService houseService;

    @RequestMapping(value = "/api/parcel/notify", method = RequestMethod.POST)
    public @ResponseBody String sendParcelNotify(MultipartHttpServletRequest req) throws IOException, ServletException {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (!user.type.admin && !user.type.jaha && !user.type.parcelChecker) {
            return null;
        }

        String message = req.getParameterValues("message")[0];
        String jsonHeader = req.getParameterValues("json")[0];
        @SuppressWarnings("serial")
        List<ParcelNotification> parcelList = new Gson().fromJson(jsonHeader, new TypeToken<List<ParcelNotification>>() {}.getType());

        Date notifyDate = new Date();

        int index = 0;
        List<User> users = Lists.newArrayList();
        for (ParcelNotification parcelBody : parcelList) {
            ParcelNotification parcel = new ParcelNotification();
            parcel.aptId = user.house.apt.id;
            parcel.dong = parcelBody.dong;
            parcel.ho = parcelBody.ho;
            parcel.message = message;
            parcel.sentDate = notifyDate;
            parcel.visible = true;

            House house = houseService.getHouse(user.house.apt.id, parcelBody.dong, parcelBody.ho);
            if (house == null) {
                parcel.notifySuccess = false;
            } else {
                List<User> usersInHouse = userService.getUsersByHouseId(house.id);
                List<User> usersAuth = usersInHouse.stream().filter(u -> !u.type.deactivated && !u.type.blocked && (u.type.user || u.type.admin || u.type.jaha)).collect(Collectors.toList());
                List<User> usersNotifyAccepted = usersAuth.stream().filter(u -> u.setting.notiParcel).collect(Collectors.toList());

                parcel.notifySuccess = !usersAuth.isEmpty();

                users.addAll(usersNotifyAccepted);
            }

            try {
                Part part = req.getPart(String.valueOf(index++));
                if (part != null) {
                    File dir = new File(String.format("/nas/EMaul/parcel/image/%s", String.valueOf(notifyDate.getTime()) + "_" + parcel.aptId));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File img = new File(dir, part.getSubmittedFileName());
                    part.write(img.getAbsolutePath());
                    Thumbnails.create(img);
                    parcel.imageUrl = "/api/parcel/image/" + (String.valueOf(notifyDate.getTime()) + "_" + parcel.aptId) + "/" + img.getName();
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }

            parcelService.save(parcel);
        }

        List<Long> userIds = users.stream().map(user1 -> user1.id).collect(Collectors.toList());

        GcmSendForm form = new GcmSendForm();
        Map<String, String> msgMap = Maps.newHashMap();
        msgMap.put("type", "parcel");
        msgMap.put("titleResId", "parcel_notification");
        msgMap.put("value", message);
        msgMap.put("action", "emaul://parcel");
        form.setUserIds(userIds);
        form.setMessage(msgMap);

        gcmService.sendGcm(form);

        return "";
    }

    @RequestMapping(value = "/api/parcel/image/{dir}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleImageRequest(@PathVariable("dir") String dir, @PathVariable("fileName") String fileName) {

        File toServeUp = new File("/nas/EMaul/parcel/image", String.format("/%s/%s", dir, fileName));

        return Responses.getFileEntity(toServeUp, dir + "-" + fileName);
    }

    @RequestMapping(value = "/api/parcel/notify-result", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<ParcelNotification> getParcelNotifyResult(@RequestParam(value = "nextPageToken", required = false) Long nextPageToken, HttpServletRequest req) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (!user.type.admin && !user.type.jaha && !user.type.parcelChecker) {
            return null;
        }

        return parcelService.getParcelNotifyResult(user.house.apt.id, nextPageToken);
    }

    @RequestMapping(value = "/api/parcel/list", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<ParcelNotification> getParcelNotifyResult(HttpServletRequest req, @RequestParam(value = "nextPageToken", required = false) Long nextPageToken) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        return parcelService.getParcelNotifications(user, nextPageToken);
    }

    @RequestMapping(value = "/api/parcel/check-admin", method = RequestMethod.GET)
    public @ResponseBody String checkAdmin(HttpServletRequest req) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        return user.type.admin || user.type.jaha || user.type.parcelChecker ? "1" : "0";
    }

    @RequestMapping(value = "/api/parcel/disable/{itemId}", method = RequestMethod.POST)
    public @ResponseBody ParcelNotification disableParcelItem(HttpServletRequest req, @PathVariable(value = "itemId") Long itemId) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        return parcelService.disableParcelItem(user, itemId);
    }



    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 택배기사 물품보관(택배기사가 무인택배함에 택배를 보관하는 경우), API 1번
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/input", method = RequestMethod.POST)
    public @ResponseBody String keepParcel(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            LOGGER.info("<<API 1번, 택배기사 물품보관(택배기사가 무인택배함에 택배를 보관하는 경우), 시작>>");

            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 인증키

            String lockerNum = obj.getString("lockerNum"); // 보관함 번호
            String password = obj.getString("password"); // 보관함 비밀번호

            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 아파트 입주민 핸드폰번호
            String parcelCompanyId = obj.getString("parcelCompanyId"); // 택배회사코드
            String parcelPhone = obj.getString("parcelPhone"); // 택배기사 핸드폰번호
            String date = obj.getString("date");

            // 택배회사 물품보관 입력
            this.parcelService.keepParcel(uuid, authKey, lockerNum, password, dong, ho, phone, parcelCompanyId, parcelPhone, date, 1);

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());

            LOGGER.info("<<API 1번, 택배기사 물품보관(택배기사가 무인택배함에 택배를 보관하는 경우), 종료>>");
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("<<무인택배함 API 1번 연동 중 오류 발생>>", e);
        }

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 입주민 택배찾기(입주민이 무인택배함에서 택배를 찾는 경우), API 2번
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/output", method = RequestMethod.POST)
    public @ResponseBody String findUserParcel(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            LOGGER.info("<<API 2번, 입주민 택배찾기(입주민이 무인택배함에서 택배를 찾는 경우), 시작>>");

            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 인증키

            String lockerNum = obj.getString("lockerNum"); // 보관함 번호

            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 핸드폰번호
            String date = obj.getString("date");

            // 입주민 택배찾기
            this.parcelService.findUserParcel(uuid, authKey, lockerNum, dong, ho, phone, date, 2);
            // push 및 sms 발송없음

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());

            LOGGER.info("<<API 2번, 입주민 택배찾기(입주민이 무인택배함에서 택배를 찾는 경우), 종료>>");
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("<<무인택배함 API 2번 연동 중 오류 발생>>", e);
        }

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 입주민 택배 보내기(아파트 입주민이 무인택배함에 택배를 보관하는 경우), API 3번
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/sendinput", method = RequestMethod.POST)
    public @ResponseBody String keepUserParcel(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            LOGGER.info("<<API 3번, 입주민 택배 보내기(아파트 입주민이 무인택배함에 택배를 보관하는 경우), 시작>>");

            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 인증키

            String lockerNum = obj.getString("lockerNum"); // 보관함 번호
            // String password = obj.getString("password"); // 보관함 비밀번호

            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 핸드폰번호
            String parcelPhone = obj.getString("parcelPhone"); // 택배기사 핸드폰번호
            String date = obj.getString("date");

            // 입주민 택배 보내기 입력
            this.parcelService.keepUserParcel("new", uuid, authKey, lockerNum, null, dong, ho, phone, null, parcelPhone, date, 3);

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());

            LOGGER.info("<<API 3번, 입주민 택배 보내기(아파트 입주민이 무인택배함에 택배를 보관하는 경우), 종료>>");
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("<<무인택배함 API 3번 연동 중 오류 발생>>", e);
        }

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 택배기사 물품배송(택배기사가 무인택배함에서 택배를 찾는 경우), API 4번
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/sendoutput", method = RequestMethod.POST)
    public @ResponseBody String findParcel(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            LOGGER.info("<<API 4번, 택배기사 물품배송(택배기사가 무인택배함에서 택배를 찾는 경우), 시작>>");

            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 인증키

            String lockerNum = obj.getString("lockerNum"); // 보관함 번호
            // String password = obj.getString("password"); // 보관함 비밀번호

            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 핸드폰번호
            // String parcelCompanyId = obj.getString("parcelCompanyId"); // 택배회사코드
            String parcelPhone = obj.getString("parcelPhone"); // 택배기사 핸드폰번호
            String date = obj.getString("date");

            // 택배기사 물품배송 입력
            // this.parcelService.findParcel("new", uuid, authKey, lockerNum, password, dong, ho, phone, parcelCompanyId, parcelPhone, date);
            this.parcelService.findParcel("new", uuid, authKey, lockerNum, null, dong, ho, phone, null, parcelPhone, date, 4);

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());

            LOGGER.info("<<API 4번, 택배기사 물품배송(택배기사가 무인택배함에서 택배를 찾는 경우), 종료>>");
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("<<무인택배함 API 4번 연동 중 오류 발생>>", e);
        }

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 입주민 반품 보관(아파트 입주민이 반품을 무인택배함에 보관하는 경우), API 5번
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/returninput", method = RequestMethod.POST)
    public @ResponseBody String keepUserReturn(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            LOGGER.info("<<API 5번, 입주민 반품 보관(아파트 입주민이 반품을 무인택배함에 보관하는 경우), 시작>>");

            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 인증키
            String lockerNum = obj.getString("lockerNum"); // 보관함 번호
            // String password = obj.getString("password"); // 보관함 비밀번호
            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 핸드폰번호
            String parcelPhone = obj.getString("parcelPhone"); // 택배기사 핸드폰번호
            String date = obj.getString("date");

            // 택배기사 물품배송 입력
            this.parcelService.keepUserParcel("return", uuid, authKey, lockerNum, null, dong, ho, phone, null, parcelPhone, date, 5);

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());

            LOGGER.info("<<API 5번, 입주민 반품 보관(아파트 입주민이 반품을 무인택배함에 보관하는 경우), 종료>>");
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("<<무인택배함 API 5번 연동 중 오류 발생>>", e);
        }

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 택배기사 반품 배송(택배기사가 무인택배함에서 반품을 찾는 경우)
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/returnoutput", method = RequestMethod.POST)
    public @ResponseBody String findReturn(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 인증키
            String lockerNum = obj.getString("lockerNum"); // 보관함 번호
            String password = obj.getString("password"); // 보관함 비밀번호
            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 핸드폰번호
            String parcelPhone = obj.getString("parcelPhone"); // 택배기사 핸드폰번호
            String date = obj.getString("date");

            this.parcelService.findParcel("return", uuid, authKey, lockerNum, password, dong, ho, phone, null, parcelPhone, date, 0);

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("", e);
        }

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 입주민 물품 장기보관, API 6번
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/longinput", method = RequestMethod.POST)
    public @ResponseBody String keepLongUserParcel(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            LOGGER.info("<<API 6번, 입주민 물품 장기보관(입주민이 물품을 장기간 찾지 않는 경우), 시작>>");

            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 보안인증키
            String lockerNum = obj.getString("lockerNum"); // 보관함 번호
            String password = obj.getString("password"); // 보관함 비밀번호
            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 핸드폰번호
            String parcelPhone = obj.getString("parcelPhone"); // 택배기사 핸드폰번호
            // String parcelCompanyId = obj.getString("parcelCompanyId"); // 택배회사코드
            String date = obj.getString("date");

            // 입주민 물품 장기보관 등록
            // this.parcelService.keepUserParcel("long", uuid, authKey, lockerNum, password, dong, ho, phone, parcelCompanyId, parcelPhone, date);
            this.parcelService.keepUserParcel("long", uuid, authKey, lockerNum, password, dong, ho, phone, null, parcelPhone, date, 6);

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());

            LOGGER.info("<<API 6번, 입주민 물품 장기보관(입주민이 물품을 장기간 찾지 않는 경우), 종료>>");
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("<<무인택배함 API 6번 연동 중 오류 발생>>", e);
        }

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 택배기사 물품 장기보관, API 7번
     *
     * @param version
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/parcel/{version}/longoutput", method = RequestMethod.POST)
    public @ResponseBody String keepLongParcel(@PathVariable("version") String version, @RequestBody String json, HttpServletRequest request) {
        JSONObject ret = null;

        try {
            json = this.decodeJson(version, json, request);

            JSONObject obj = new JSONObject(json);

            String uuid = obj.getString("uuid"); // 택배함 UUID
            String authKey = obj.getString("authKey"); // 보안인증키
            String lockerNum = obj.getString("lockerNum"); // 보관함 번호
            String password = obj.getString("password"); // 보관함 비밀번호
            int dong = obj.getInt("dong"); // 동
            int ho = obj.getInt("ho"); // 호
            String phone = obj.getString("phone"); // 핸드폰번호
            String parcelPhone = obj.getString("parcelPhone"); // 택배기사 핸드폰번호
            String date = obj.getString("date");

            // 택배기사 물품 장기보관 등록
            this.parcelService.findParcel("long", uuid, authKey, lockerNum, password, dong, ho, phone, null, parcelPhone, date, 7);

            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_SUCCESS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_SUCCESS.getMessage());
        } catch (Exception e) {
            ret = new JSONObject();
            ret.put("resultCode", ParcelCode.RESP_RUNTIMEFAILS.getCode());
            ret.put("resultMessage", ParcelCode.RESP_RUNTIMEFAILS.getMessage());

            LOGGER.error("<<무인택배함 API 7번 연동 중 오류 발생>>", e);
        }

        return ret.toString();
    }

    /**
     * 암호화된 json 데이터를 복호화한다.
     *
     * @param version
     * @param json
     * @param request
     * @return
     * @throws Exception
     */
    private String decodeJson(String version, String json, HttpServletRequest request) throws Exception {
        if ("2".equals(version)) {
            String uuid = request.getHeader("x-jaha-uuid");
            ParcelLocker parcelLocker = this.parcelService.findParcelLocker(uuid);

            PBCrypto pbCrypto = new PBCrypto();

            // 1. 보안인증키를 BASE64 인코딩한다.
            String base64EncodedAuthKey = Base64.encodeString(parcelLocker.getAuthKey());

            // 2. 암호화된 json 데이터 복호화
            json = pbCrypto.decryptCipherTextUsingBlockCipher("AES", base64EncodedAuthKey, json);
            LOGGER.debug("json: {}", json);

            return json;
        } else {
            return json;
        }
    }

    @RequestMapping(value = "/api/parcel/push-list")
    public @ResponseBody ApiResponse<?> handlePushClickCheckRequest(HttpServletRequest request, @RequestParam(value = "gubun", required = false, defaultValue = "parcel-ad") String gubun,
            @RequestParam(value = "userId", required = false) Long userId, @RequestParam(value = "lastPushId", required = false) Long lastPushId,
            @RequestParam(value = "count", required = false, defaultValue = "5") Integer count, @PageableDefault(sort = {"modDate"}, direction = Direction.DESC, size = 10) Pageable pageable) {
        Long uId = SessionAttrs.getUserId(request.getSession());
        if (uId == null) {
            uId = userId;
        }
        // LOGGER.info("<<푸시로그목록조회 사용자 아이디>> {}", uId);
        // User user = userService.getUser(uId);

        ApiResponse<ScrollPage<PushLog>> ar = new ApiResponse<>();

        try {
            ScrollPage<PushLog> pushLogPage = this.parcelService.findPushList(lastPushId, uId, gubun, count);
            ar.setBody(pushLogPage);
        } catch (Exception e) {
            LOGGER.error("<<푸시로그 목록 조회 중 오류 발생>>", e);
        }

        return ar;
    }

}
