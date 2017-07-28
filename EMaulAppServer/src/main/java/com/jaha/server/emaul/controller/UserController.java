package com.jaha.server.emaul.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.jaha.server.emaul.constants.Constants;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.Setting;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.UserLoginLog;
import com.jaha.server.emaul.model.UserNickname;
import com.jaha.server.emaul.service.GcmService;
import com.jaha.server.emaul.service.PhoneAuthService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.PasswordHash;
import com.jaha.server.emaul.util.RandomKeys;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.util.Thumbnails;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAlarmSetting;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushGubun;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushMessage;
import com.jaha.server.emaul.v2.model.common.GovOfficeVo;
import com.jaha.server.emaul.v2.model.user.UserUpdateHistoryVo;
import com.jaha.server.emaul.v2.service.common.GovOfficeService;
import com.jaha.server.emaul.v2.util.PushUtils;

/**
 * Created by doring on 15. 3. 9..
 */
@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private GcmService gcmService;

    @Autowired
    private PhoneAuthService phoneAuthService;

    // [START] 광고 푸시 추가 by realsnake 2016.10.28
    @Autowired
    private PushUtils pushUtils;
    // [END]

    @Autowired
    private GovOfficeService govOfficeService;

    @RequestMapping(value = "/api/public/user/create-apt-user", method = RequestMethod.POST)
    public @ResponseBody String getOrRegisterUser(HttpServletRequest req, @RequestParam(value = "uid", required = false) String uid, @RequestParam(value = "kind", required = false) String kind,
            @RequestParam(value = "addressCode") String addressCode, @RequestParam(value = "dong") String dong, @RequestParam(value = "ho") String ho, @RequestParam(value = "birthYear",
                    required = false) String birthYear, @RequestParam(value = "gender", required = false) String gender, @RequestParam(value = "email") String email,
            @RequestParam(value = "name") String name, @RequestParam(value = "password") String password, @RequestParam(value = "phoneNumber") String phoneNumber, @RequestParam(
                    value = "phoneAuthCode") String phoneAuthCode, @RequestParam(value = "recommNickName", required = false) String recommNickName,
            @RequestParam(value = "phoneAuthKey") String phoneAuthKey, @RequestParam(value = "loiAgrmYn", required = false) String loiAgrmYn) throws JsonProcessingException {
        User user = userService.getUser(email);
        // if (user != null && !user.type.deactivated) {
        if (user != null) { // deactivated 상관없이 이메일에 해당하는 계정이 있으면 가입불가
            return "EXIST_EMAIL";
        }
        if (!phoneAuthService.checkAuth(phoneAuthCode, phoneAuthKey)) {
            return "PHONE_AUTH_ERROR";
        }
        String phoneNumberDb = phoneAuthService.getPhoneNumber(phoneAuthCode, phoneAuthKey);
        if (phoneNumberDb == null) {
            return null;
        }

        User recommUser = null;
        Long recommId = 0l;

        // 추천인 값이 있는 경우 추천인 체크
        if (recommNickName != null && !recommNickName.isEmpty()) {
            recommUser = userService.getUserByNickName(recommNickName);

            if (recommUser == null) {
                return "NO_SUCH_NICKNAME";
            } else {
                recommId = recommUser.id;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            name = StringUtils.trimToEmpty(name);
            email = StringUtils.trimToEmpty(email);
            phoneNumber = StringUtils.trimToEmpty(phoneNumber);
            user = userService.createUser(req, uid, addressCode, dong, ho, birthYear, gender, email, name, password, phoneNumberDb, recommId);

            // -- 사용자 설정변경 HISTORY --
            try {
                userService.saveUserUpdateHistory(user, user, UserUpdateHistoryVo.TYPE_SIGN_UP, null);

            } catch (Exception e) {
                LOGGER.error(">>> 사용자 가입 히스토리 오류", e);
            }
            // -- 사용자 설정변경 HISTORY --

            LOGGER.info("[회원가입정보] 이름:{}, 이메일:{}, 폰번호:{}, 생년:{}, 성별:{}, 동/호:{}/{}, 위치정보제공동의여부:{}", name, email, phoneNumber, birthYear, gender, dong, ho, loiAgrmYn);
        } catch (Exception e) {
            LOGGER.error("[회원가입 중 오류]", e);
        }

        // 방문자로 신규 가입한 경우 관리자에게 알림 메시지 전송
        if (user != null && user.type.anonymous && !user.type.admin && !user.type.jaha) {
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
            // gcmService.sendGcmToAdmin(userService.getAdminUsers(user.house.apt.id), user.house.dong + "동 " + user.house.ho + "호 " + user.getFullName() + "님이 가입하셨습니다.\n관리자 웹페이지에서 주민 확인 후에 승인해주세요.");

            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetAdminList(PushAlarmSetting.ALARM, Lists.newArrayList(user.house.apt.id));
            String value = String.format(PushMessage.USER_AGREE_REQ.getValue(), user.house.dong, user.house.ho, user.getFullName());

            this.pushUtils.sendPush(PushGubun.USER_AGREE_REQ, "주민 승인 요청", value, null, null, false, targetUserList);
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
        }

        return user == null ? null : mapper.writeValueAsString(user);
    }

    @RequestMapping(value = "/api/user/auth-levels", method = RequestMethod.GET)
    public @ResponseBody List<String> userAuthLevel(HttpServletRequest req) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        return user.type.getTrueTypes();
    }

    @RequestMapping(value = "/api/public/user/login", method = RequestMethod.POST)
    public @ResponseBody User login(HttpServletRequest req, @RequestParam(value = "email") String email, @RequestParam(value = "password") String password, @RequestParam(value = "uid",
            required = false) String uid, @RequestParam(value = "phoneNumber", required = false) String phoneNumber, @RequestParam(value = "gcmId", required = false) String gcmId, @RequestParam(
            value = "kind", required = false) String kind, @RequestParam(value = "appVersion", required = false) String appVersion,
            @RequestParam(value = "deviceId", required = false) String deviceId, @RequestParam(value = "osName", required = false) String osName,
            @RequestParam(value = "osVersion", required = false) String osVersion, @RequestParam(value = "maker", required = false) String maker,
            @RequestParam(value = "model", required = false) String model, @RequestParam(value = "handwork", required = false) String handwork) {

        // LOGGER.info(">>> login kind: {}, GCM ID: {}, UNIQUE DEVICE ID {}>>", kind, gcmId, deviceId);

        // login() method 일원화
        // 2016.11.17 cyt : appVersion 추가
        // 2016.11.24 cyt : deviceId 추가 (gcm_id, unique_device_id, multi_login_yn 을 사용하여 중복 로그인 체크)

        // where null - is null 조건 처리 회피용
        if (StringUtils.isEmpty(gcmId)) {
            gcmId = ""; // gcm_id
        }
        if (StringUtils.isEmpty(appVersion)) {
            appVersion = "";
        }
        if (StringUtils.isEmpty(deviceId)) {
            deviceId = "";
        }
        if (StringUtils.isEmpty(osName)) {
            osName = "";
        }
        if (StringUtils.isEmpty(osVersion)) {
            osVersion = "";
        }
        if (StringUtils.isEmpty(maker)) {
            maker = "";
        }
        if (StringUtils.isEmpty(model)) {
            model = "";
        }

        User user = userService.login(req, email, password, kind, gcmId, appVersion, deviceId, osName, osVersion, maker, model);

        if (user != null) {

            if ("Y".equals(handwork)) {
                userService.saveUserLoginLog(user, UserLoginLog.LOGIN);
            }

            if ("ios".equals(kind) && gcmId != null) {
                gcmService.setGcmId(user.id, gcmId, "ios");
            }

        }

        return user;

    }

    @RequestMapping(value = "/api/user/logout", method = RequestMethod.POST)
    public @ResponseBody String logout(HttpServletRequest req) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        if (user.kind.equals("ios")) {
            gcmService.setGcmId(user.id, "", "ios");
        }
        userService.logout(req);

        userService.saveUserLoginLog(user, UserLoginLog.LOGOUT);

        return "1";
    }

    @RequestMapping(value = "/session-check", method = RequestMethod.GET)
    public @ResponseBody String invalidUser() {
        return "OK";
    }

    @RequestMapping(value = "/api/gcm", method = RequestMethod.POST)
    public @ResponseBody String handleGcm(HttpServletRequest req, @RequestBody String json) throws IOException {

        String gcmId = new JSONObject(json).getString("gcmId");
        Long userId = SessionAttrs.getUserId(req.getSession());

        gcmService.setGcmId(userId, gcmId);

        try {
            // USER.gcm_id 컬럼도 업데이트 한다.
            if (StringUtils.isNotEmpty(gcmId)) {
                userService.updateUserGcmId(userId, gcmId);
            }
        } catch (Exception e) {
            // 해당 로직은 별도로 동작하도록 추가한다.
            LOGGER.error(">>> /api/gcm user table insert exception", e);
        }

        return "1";
    }

    @RequestMapping(value = "/api/setting", method = RequestMethod.POST)
    public @ResponseBody Setting handleSettingPost(HttpServletRequest req, @RequestBody String json) throws IOException {

        Setting setting = new Gson().fromJson(json, Setting.class);

        setting.userId = SessionAttrs.getUserId(req.getSession());

        return userService.saveAndFlush(setting);
    }

    @RequestMapping(value = "/api/setting", method = RequestMethod.GET)
    public @ResponseBody Setting handleSettingGet(HttpServletRequest req) throws IOException {

        Long userId = SessionAttrs.getUserId(req.getSession());

        return userService.getSetting(userId);
    }

    @RequestMapping(value = "/api/user/change-nickname", method = RequestMethod.POST)
    public @ResponseBody User changeNickname(HttpServletRequest req, @RequestBody String json) {

        String nickname = new JSONObject(json).getString("nickname");

        Long userId = SessionAttrs.getUserId(req.getSession());

        User user = userService.getUser(userId);

        if (user.getNickname() != null && user.getNickname().name.equals(nickname)) {
            return userService.convertToPrivateUser(user);
        }

        user = userService.changeUserNickname(user, nickname);

        if (user != null) {

            // -- 사용자 설정변경 HISTORY --
            try {
                userService.saveUserUpdateHistory(user, user, UserUpdateHistoryVo.TYPE_CHANGE_NICK, null);

            } catch (Exception e) {
                LOGGER.error(">>> 사용자 설정변경 히스토리 오류 [닉네임]", e);
            }
            // -- 사용자 설정변경 HISTORY --

            return user;
        }
        return null;
    }

    @RequestMapping(value = "/api/user/change-password", method = RequestMethod.POST)
    public @ResponseBody User changePassword(HttpServletRequest req, @RequestBody String json) {

        String pwOld = new JSONObject(json).getString("pwOld");
        String pwNew = new JSONObject(json).getString("pwNew");

        Long userId = SessionAttrs.getUserId(req.getSession());

        User user = userService.getUser(userId);

        try {
            if (PasswordHash.validatePassword(pwOld, user.passwordHash)) {
                user.passwordHash = PasswordHash.createHash(pwNew);

                // -- 사용자 설정변경 HISTORY --
                try {
                    String data = "userPwd : " + user.passwordHash;
                    userService.saveUserUpdateHistory(user, user, UserUpdateHistoryVo.TYPE_CHANGE_PWD, data);
                } catch (Exception e) {
                    LOGGER.error(">>> 사용자 설정변경 히스토리 오류 [비밀번호변경]", e);
                }
                // -- 사용자 설정변경 HISTORY --

                return userService.saveAndFlush(user);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return null;
    }

    @RequestMapping(value = "/api/user/deactivate", method = RequestMethod.POST)
    public @ResponseBody String deactivate(HttpServletRequest req) {

        userService.deactivate(req);

        return "1";
    }

    @RequestMapping(value = "/api/user/profile-image/delete", method = RequestMethod.DELETE)
    public @ResponseBody User removeProfileImage(HttpServletRequest req) {

        Long userId = SessionAttrs.getUserId(req.getSession());

        User user = userService.getUser(userId);

        long parentNum = user.id / 1000l;
        File dir = new File(String.format("/nas/EMaul/user/profile-image/%s/%s", parentNum, userId));
        File dest = new File(dir, String.format("%s.jpg", userId));
        File destThumb = new File(dir, String.format("%s-thumb.jpg", userId));

        if (dest.exists()) {
            dest.delete();
        }
        if (destThumb.exists()) {
            destThumb.delete();
        }

        user.hasProfileImage = false;

        user = userService.saveAndFlush(user);

        return user;
    }

    @RequestMapping(value = "/api/user/profile-image/upload", method = RequestMethod.POST)
    public @ResponseBody User saveProfileImage(HttpServletRequest req, @RequestParam(value = "image") MultipartFile image) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (image != null) {
            long parentNum = user.id / 1000l;
            long userId = user.id;

            try {
                File dir = new File(String.format("/nas/EMaul/user/profile-image/%s", parentNum));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, String.format("%s.jpg", userId));
                dest.createNewFile();
                image.transferTo(dest);
                Thumbnails.create(dest);
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
        user.hasProfileImage = true;
        user = userService.saveAndFlush(user);

        return user;
    }

    @RequestMapping(value = "/api/public/user/profile-image/{filename}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleImageRequest(@PathVariable("filename") String fileBaseName) {

        int idxThumb = fileBaseName.indexOf("-thumb");
        Long userId = Longs.tryParse(fileBaseName);
        if (idxThumb != -1) {
            userId = Longs.tryParse(fileBaseName.substring(0, idxThumb));
        }

        File toServeUp = new File("/nas/EMaul/user/profile-image", String.format("%s/%s.jpg", userId / 1000l, fileBaseName));

        return Responses.getFileEntity(toServeUp, fileBaseName + ".jpg");
    }

    @SuppressWarnings("unused")
    @RequestMapping(value = "/api/user/invitation/invite", method = RequestMethod.POST)
    public @ResponseBody String sendInvitation(HttpServletRequest req, @RequestBody String json) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user == null || json == null) {
            return Constants.HTTP_RESULT_AUTH_FAIL;
        }

        JSONObject obj = new JSONObject(json);

        if (obj == null) {
            return Constants.HTTP_RESULT_AUTH_FAIL;
        }

        String targetPhone = obj.getString("targetPhone");
        String senderId = obj.getString("senderId");

        String userId = String.valueOf(user.id);

        if (!userId.equals(senderId)) {
            return Constants.HTTP_RESULT_AUTH_FAIL;
        }

        String message = "";

        String userFullName = user.getFullName();
        UserNickname nickName = user.getNickname();
        List<User> targetUsers = userService.getUsersByPhone(targetPhone);
        User targetUser = null;

        if (targetUsers != null && targetUsers.size() > 0) {
            targetUser = targetUsers.get(0);
        }

        String nickNameMsg = "";

        if (nickName != null && nickName.name != null) {
            nickNameMsg = "(" + nickName.name + ")";
        }

        message = userFullName + nickNameMsg + "님이 아파트 앱 e마을을 추천합니다.\n" + "\n" + "설치 : " + Constants.APP_URLS[Constants.IDX_APP_URL_ANDROID];

        // 발신자번호는 비즈뿌리오에 사전 등록된 번호만 문자 전송이 가능함
        phoneAuthService.sendMsgNow(targetPhone, Constants.MESSAGE_SENDER_PHONE_NUMBER, message, "", "");

        LOGGER.info("[e마을추천] 수신번호: {}, 메시지: {}", targetPhone, message);

        if (targetUser == null) {
            return Constants.HTTP_RESULT_OK;
        } else {
            // return Constants.HTTP_RESULT_EXIST_TARGET_USER;
            // : 현재는 이미 회원인 경우도 문자 전송하게 처리하였으므로 리턴값 OK
            return Constants.HTTP_RESULT_OK;
        }
    }

    @RequestMapping(value = "/api/user/gu-office/info", method = RequestMethod.GET)
    public @ResponseBody String getGuOfficeInfo(HttpServletRequest req) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user == null) {
            return Constants.HTTP_RESULT_AUTH_FAIL;
        }
        String guOfficeName = "";
        String guOfficeUrl = "";

        if (user.house != null && user.house.apt != null && user.house.apt.address != null) {
            GovOfficeVo govOfficeVo = govOfficeService.getGovOfficeVo(user);
            guOfficeName = govOfficeVo.getName();
            guOfficeUrl = govOfficeVo.getUrl();
        }

        JSONObject obj = new JSONObject();
        obj.put("guOfficeName", guOfficeName);
        obj.put("guOfficeUrl", guOfficeUrl);

        return obj.toString();
    }

    // ///////////////////////////////////////////////// 아이디 / 비밀번호 찾기 추가 ///////////////////////////////////////////////////
    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 9.
     * @modifier shavrani 2016-10-21
     * @description
     *
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/user/auth-code/req", method = RequestMethod.POST)
    @ResponseBody
    public String reqAuthCode(@RequestBody String json) {
        LOGGER.debug("* 요청 JSON: {}", json);

        JSONObject obj = new JSONObject(json);

        String reqType = obj.getString("reqType");
        String userEmail = obj.getString("userEmail");
        String userName = obj.getString("userName");// 인증번호발송시 이름은 전달받되 활용은하지않는다.( 화면상에서 인증되는듯한 view용도 )
        String phoneNumber = obj.getString("phoneNumber");

        JSONObject ret = new JSONObject();

        List<SimpleUser> userList = null;
        try {
            userList = userService.checkUserInfo(userEmail, phoneNumber);
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        if (userList == null || userList.isEmpty()) {
            ret.put("resultCode", "01");
            ret.put("resultMsg", "USER NOT EXISTS!");
            ret.put("key", StringUtils.EMPTY);
            return ret.toString();
        }

        String code = String.format("%06d", (int) (Math.random() * 1000000));
        String key = RandomKeys.make(32);
        // 발신자번호는 비즈뿌리오에 사전 등록된 번호만 문자 전송이 가능함
        boolean tf = phoneAuthService.sendMsgNow(phoneNumber, "028670816", String.format("e마을 아이디찾기 인증번호 [%s]를 입력해주세요.", code), code, key);

        String temp = null;
        if ("id-search".equals(reqType)) {
            temp = "아이디찾기";
        } else if ("pw-search".equals(reqType)) {
            temp = "비밀번호찾기";
        }

        if (tf) {
            LOGGER.info("* {}, e마을 {} 인증번호[{}] 발송 성공", phoneNumber, temp, code);
        } else {
            LOGGER.info("* {}, e마을 {} 인증번호[{}] 발송 실패", phoneNumber, temp, code);
        }

        ret.put("resultCode", "00");
        ret.put("resultMsg", "OK");
        ret.put("key", key);
        ret.put("code", code);// 임시코드 ( 앱에 인증코드를 문자로 보내지만 결과값에도 포함 )

        return ret.toString();
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 9.
     * @description
     *
     * @param json
     * @return
     */
    @RequestMapping(value = "/api/public/user/auth-code/check", method = RequestMethod.POST)
    @ResponseBody
    public String checkAuthCode(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);

        String userEmail = obj.getString("userEmail");
        String userName = obj.getString("userName");
        String phoneNumber = obj.getString("phoneNumber");
        String code = obj.getString("code");
        String key = obj.getString("key");

        JSONObject ret = new JSONObject();

        if (phoneAuthService.checkAuth(code, key, phoneNumber)) {
            User user = null;
            try {
                user = this.userService.checkUserInfo(userEmail, userName, phoneNumber);
            } catch (Exception e) {
                LOGGER.error("", e);
            }

            if (user == null) {
                ret.put("resultCode", "01");
                ret.put("resultMsg", "USER NOT EXISTS!");
                ret.put("email", StringUtils.EMPTY);
                ret.put("regDate", StringUtils.EMPTY);
            } else {
                ret.put("resultCode", "00");
                ret.put("resultMsg", "OK");
                ret.put("email", user.getEmail());
                ret.put("regDate", user.regDate);
            }
        } else {
            ret.put("resultCode", "02");
            ret.put("resultMsg", "CODE IS WRONG!");
            ret.put("email", StringUtils.EMPTY);
            ret.put("regDate", StringUtils.EMPTY);
        }

        return ret.toString();
    }

    /**
     * @author shavrani 2016-10-21
     * @desc 인증코드체크 v2 ( 인증되면 user list로 return )
     * @return
     */
    @RequestMapping(value = "/api/public/v2/user/auth-code/check", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> checkAuthCodeV2(@RequestBody String json) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        JSONObject obj = new JSONObject(json);

        String userEmail = obj.getString("userEmail");
        String phoneNumber = obj.getString("phoneNumber");
        String code = obj.getString("code");
        String key = obj.getString("key");

        if (phoneAuthService.checkAuth(code, key, phoneNumber)) {
            List<SimpleUser> userList = null;
            try {
                userList = userService.checkUserInfo(userEmail, phoneNumber);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            if (StringUtil.isBlank(userEmail)) {
                apiResponse.setBody(userList);
            } else {
                if (userList != null && !userList.isEmpty()) {
                    SimpleUser simpleUser = userList.get(0);
                    apiResponse.setBody(simpleUser);
                }
            }

        } else {
            apiHeader.setResultCode("99");
            apiHeader.setResultMessage("CODE IS WRONG!");
        }

        return apiResponse;
    }

    @RequestMapping(value = "/api/public/user/pw-reset", method = RequestMethod.POST)
    @ResponseBody
    public String resetPassword(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);

        String password = obj.getString("password");
        String email = obj.getString("email");

        boolean tf = this.userService.resetPassword(password, email);

        JSONObject ret = new JSONObject();

        if (tf) {
            ret.put("resultCode", "00");
            ret.put("resultMsg", "OK");

            LOGGER.info("[{}]님의 비밀번호가 재설정되었습니다.", email);
        } else {
            ret.put("resultCode", "03");
            ret.put("resultMsg", "PASSWORD FAILS TO RESET!");
        }

        return ret.toString();
    }

    @RequestMapping(value = "/api/public/user/id-search", method = RequestMethod.POST)
    @ResponseBody
    public String searchId(@RequestBody String json) {
        JSONObject obj = new JSONObject(json);

        String email = obj.getString("email");

        User user = this.userService.getUser(email);

        JSONObject ret = new JSONObject();

        if (user == null) {
            ret.put("resultCode", "01");
            ret.put("resultMsg", "USER NOT EXISTS!");
        } else {

            if (user.type.deactivated) {
                ret.put("resultCode", "02");
                ret.put("resultMsg", "deactivated");
            } else {
                ret.put("resultCode", "00");
                ret.put("resultMsg", "OK");
            }
        }

        return ret.toString();
    }

    // ///////////////////////////////////////////////// 아이디 / 비밀번호 찾기 추가 ///////////////////////////////////////////////////

    @RequestMapping(value = "/api/public/user/phone-search")
    @ResponseBody
    public ApiResponse<?> phoneAccountSearch(@RequestParam(value = "phone", required = false) String phone) {

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        if (StringUtil.isBlank(phone)) {
            LOGGER.info("<< /api/public/user/phone-search , required parameter is empty !! >>");
            apiHeader.setResultCode("99");
            apiHeader.setResultMessage("required parameter is empty");
            return apiResponse;
        }

        apiResponse.setBody(userService.phoneAccountSearch(phone));

        return apiResponse;

    }

    /**
     * 개편된 계정등록
     *
     * @author shavrani 2016-10-18
     */
    @RequestMapping(value = "/api/public/user/create-user")
    @ResponseBody
    public ApiResponse<?> selectOrRegisterUser(HttpServletRequest req, @RequestParam Map<String, Object> params) throws Exception {

        ApiResponse<User> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        String uid = StringUtil.nvl(params.get("uid"));
        String addressCode = StringUtil.nvl(params.get("addressCode"));
        String dong = StringUtil.nvl(params.get("dong"));
        String ho = StringUtil.nvl(params.get("ho"));
        String birthYear = StringUtil.nvl(params.get("birthYear"));
        String gender = StringUtil.nvl(params.get("gender"));
        String email = StringUtils.trimToEmpty(StringUtil.nvl(params.get("email")));
        String name = StringUtils.trimToEmpty(StringUtil.nvl(params.get("name")));
        String password = StringUtil.nvl(params.get("password"));
        String phoneAuthCode = StringUtil.nvl(params.get("phoneAuthCode"), null);
        String phoneAuthKey = StringUtil.nvl(params.get("phoneAuthKey"), null);
        String recommNickName = StringUtil.nvl(params.get("recommNickName"));

        // addressCode가 없을시 받는 parameter
        String sidoNm = StringUtil.nvl(params.get("sidoNm"));
        String sggNm = StringUtil.nvl(params.get("sggNm"));
        String emdNm = StringUtil.nvl(params.get("emdNm"));
        String addressDetail = StringUtil.nvl(params.get("addressDetail"));

        /** parameter validation */
        if (StringUtil.isBlank(email) || StringUtil.isBlank(name) || StringUtil.isBlank(password)) {
            LOGGER.info("<< /api/public/user/create-user , required parameter is empty !! >>", email);
            apiHeader.setResultCode("94");
            apiHeader.setResultMessage("required parameter is empty");
            return apiResponse;
        }

        if (StringUtil.isBlank(addressCode)) {
            if (StringUtil.isBlank(sidoNm) || StringUtil.isBlank(sggNm) || StringUtil.isBlank(emdNm) || StringUtil.isBlank(addressDetail)) {
                LOGGER.info("<< /api/public/user/create-user , required parameter is empty !! >>", email);
                apiHeader.setResultCode("94");
                apiHeader.setResultMessage("required parameter is empty");
                return apiResponse;
            }
        } else {
            if (StringUtil.isBlank(dong) || StringUtil.isBlank(ho)) {
                LOGGER.info("<< /api/public/user/create-user , required parameter is empty !! >>", email);
                apiHeader.setResultCode("94");
                apiHeader.setResultMessage("required parameter is empty");
                return apiResponse;
            }
        }

        User user = userService.getUser(email);
        if (user != null) {
            // user.type.deactivated 상관없이 이메일 중복차단
            LOGGER.info("<< /api/public/user/create-user , already emaul [{}] !! >>", email);
            apiHeader.setResultCode("99");
            apiHeader.setResultMessage("exist email");
            return apiResponse;
        }

        if (!phoneAuthService.checkAuth(phoneAuthCode, phoneAuthKey)) {
            LOGGER.info("<< /api/public/user/create-user , phone auth check fail !! >>");
            apiHeader.setResultCode("98");
            apiHeader.setResultMessage("phone auth error");
            return apiResponse;
        }
        String phoneNumberDb = phoneAuthService.getPhoneNumber(phoneAuthCode, phoneAuthKey);
        if (phoneNumberDb == null) {
            LOGGER.info("<< /api/public/user/create-user , phone auth check fail !! >>");
            apiHeader.setResultCode("98");
            apiHeader.setResultMessage("phone auth error");
            return apiResponse;
        }
        params.put("phoneNumber", phoneNumberDb);

        Map<String, Object> phoneUserMap = userService.phoneAccountSearch(phoneNumberDb);
        if (phoneUserMap != null) {
            int currCnt = StringUtil.nvlInt(phoneUserMap.get("currCnt"));
            int maxCnt = StringUtil.nvlInt(phoneUserMap.get("maxCnt"));
            if (currCnt >= maxCnt) {
                LOGGER.info("<< /api/public/user/create-user , phone create account fail,  currCnt :{}, maxCnt : {} >>", currCnt, maxCnt);
                apiHeader.setResultCode("97");
                apiHeader.setResultMessage("phone create account max fail");
                return apiResponse;
            }
        }

        User recommUser = null;
        Long recommId = 0l;

        // 추천인 값이 있는 경우 추천인 체크
        if (!StringUtil.isBlank(recommNickName)) {
            recommUser = userService.getUserByNickName(recommNickName);
            if (recommUser == null) {
                LOGGER.info("<< /api/public/user/create-user , phone auth check fail !! >>");
                apiHeader.setResultCode("96");
                apiHeader.setResultMessage("no search recommender nickname");
                return apiResponse;
            } else {
                recommId = recommUser.id;
            }
        }
        params.put("recommId", recommId);

        try {
            user = userService.createUser(req, params);

            // -- 사용자 설정변경 HISTORY --
            try {
                userService.saveUserUpdateHistory(user, user, UserUpdateHistoryVo.TYPE_SIGN_UP, null);

            } catch (Exception e) {
                LOGGER.error(">>> 사용자 가입 히스토리 오류", e);
            }
            // -- 사용자 설정변경 HISTORY --

            LOGGER.info("[회원가입정보] 이름:{}, 이메일:{}, 폰번호:{}, 생년:{}, 성별:{}, 동/호:{}/{}", name, email, phoneNumberDb, birthYear, gender, dong, ho);
        } catch (Exception e) {
            LOGGER.error("<< /api/public/user/create-user , 회원가입 중 오류 >>", e);
            apiHeader.setResultCode("95");
            apiHeader.setResultMessage("user save exception, createUser fail !!");
            return apiResponse;
        }

        // 방문자로 신규 가입한 경우 관리자에게 알림 메시지 전송
        if (user != null && user.type.anonymous && !user.type.admin && !user.type.jaha) {
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
            // gcmService.sendGcmToAdmin(userService.getAdminUsers(user.house.apt.id), user.house.dong + "동 " + user.house.ho + "호 " + user.getFullName() + "님이 가입하셨습니다.\n관리자 웹페이지에서 주민 확인 후에 승인해주세요.");
            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetAdminList(PushAlarmSetting.ALARM, Lists.newArrayList(user.house.apt.id));
            String value = String.format(PushMessage.USER_AGREE_REQ.getValue(), user.house.dong, user.house.ho, user.getFullName());

            this.pushUtils.sendPush(PushGubun.USER_AGREE_REQ, "주민 승인 요청", value, null, null, false, targetUserList);
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
        }

        if (user == null) {
            LOGGER.info("<< /api/public/user/create-user , user save fail !! >>");
            apiHeader.setResultCode("95");
            apiHeader.setResultMessage("user save exception, user is null !!");
        } else {
            apiResponse.setBody(user);
        }

        return apiResponse;
    }


    /**
     * 주민번호를 이용한 고객 생년월일 + 성별 UPDATE<br/>
     * 휴대폰 본인 인증시 사용 <br/>
     * IOS는 가입 시 생년월일, 성별등을 입력받지 못하므로 <br/>
     * 휴대폰 본인인증 시 입력한 데이터로 사용자 정보를 업데이트 한다. <br/>
     * <br/>
     * cyt : 2016.11.16 <br/>
     * API 문서 : API정의서_사용자성별수정_20161116.xlsx
     *
     * @param req
     * @param uid
     * @return
     */
    @RequestMapping(value = "/v2/api/user/update-add-info", method = RequestMethod.POST)
    @ResponseBody
    public String updateUserAddInfo(HttpServletRequest req, @RequestParam(value = "uid", required = true) String uid) {

        JSONObject ret = new JSONObject();

        if (uid == null || uid.trim().length() != 13) {
            ret.put("resultCode", "01");
            ret.put("resultMsg", "invalid length uid");
            LOGGER.debug(">>> 입력데이터 오류 : " + uid);
            return ret.toString();
        }

        try {
            Long.parseLong(uid.trim());
        } catch (Exception e) {
            // 숫자형 데이터 오류
            ret.put("resultCode", "01");
            ret.put("resultMsg", "invalid number format uid");
            LOGGER.debug(">>> 입력데이터 오류 : " + uid);
            return ret.toString();
        }

        LOGGER.debug(">>> session : " + req.getSession());
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        LOGGER.debug(">>> user.type.deactivated : " + user.type.deactivated);

        if (user == null || user.id == null) {
            ret.put("resultCode", "02");
            ret.put("resultMsg", "user not exist");
        } else {

            if (user.type.deactivated) {
                ret.put("resultCode", "03");
                ret.put("resultMsg", "deactivated");
            } else {

                try {

                    int value = Integer.parseInt(uid.trim().substring(6, 7));
                    LOGGER.debug(">>> 주민번호 : " + uid.trim());
                    LOGGER.debug(">>> value : " + value);

                    String gender = (value % 2) == 1 ? "male" : "female";
                    String birthYear = uid.trim().substring(0, 2); // 생년월일

                    if (value % 9 == 0) {
                        birthYear = "18" + birthYear;
                    } else if (value <= 2) {
                        birthYear = "19" + birthYear;
                    } else if (value <= 4) {
                        birthYear = "20" + birthYear;
                    } else if (value <= 6) {
                        birthYear = "19" + birthYear;
                    } else if (value <= 8) {
                        birthYear = "20" + birthYear;
                    }

                    LOGGER.debug(">>> 생년월일 : " + birthYear + " / 성별 : " + gender);
                    /**
                     * female / male
                     */
                    this.userService.modifyUserAddInfo(user.id, birthYear, gender);
                    ret.put("resultCode", "00");
                    ret.put("resultMsg", "SUCCESS");
                } catch (Exception e) {
                    ret.put("resultCode", "04");
                    ret.put("resultMsg", "insert exception");
                    LOGGER.error(">>> /v2/api/user/update-add-info Exception uid [" + uid + "] : " + e.getMessage());
                }

            }
        }

        return ret.toString();
    }



    /**
     * 사용자의 생년월일 + 성별을 직접 입력받아 사용자 정보 수정<br />
     * updateUserAddInfo 는 주민번호를 입력받아 후처리 수행<br />
     * updateUserAddBirthInfo 는 생년, 성별을 직접 입력받아 후처리 수행<br />
     * 후처리 수행결과는 동일함 (생년월일, 성별 수정)<br />
     * cyt : 2016.11.16 <br/>
     * API 문서 : API정의서_사용자성별수정_20161116.xlsx
     *
     * @param req
     * @param uid
     * @return
     */
    @RequestMapping(value = "/v2/api/user/update-birth-info", method = RequestMethod.POST)
    @ResponseBody
    public String updateUserAddBirthInfo(HttpServletRequest req, @RequestParam(value = "birth", required = true) String birth, @RequestParam(value = "gender", required = true) String gender) {

        JSONObject ret = new JSONObject();

        if (birth == null || birth.trim().length() != 4) {
            ret.put("resultCode", "01");
            ret.put("resultMsg", "년도 정보 입력 오류");
            LOGGER.debug(">>> 입력데이터 오류 : birth" + birth == null ? "" : " : [" + birth.trim() + "]");
            return ret.toString();
        }

        try {
            Long.parseLong(birth.trim());
        } catch (Exception e) {
            // 숫자형 데이터 오류
            ret.put("resultCode", "02");
            ret.put("resultMsg", "년도 정보 입력 오류 : [" + birth + "]");
            LOGGER.debug(">>> 입력데이터 오류 : " + birth);
            return ret.toString();
        }

        if (gender == null) {
            ret.put("resultCode", "03");
            ret.put("resultMsg", "성별 정보 입력 오류");
            LOGGER.debug(">>> 입력데이터 오류 : gender");
            return ret.toString();
        }

        if (!("female".equalsIgnoreCase(gender) || "male".equalsIgnoreCase(gender))) {
            ret.put("resultCode", "04");
            ret.put("resultMsg", "정의된 성별정보로 입력해주세요 : [" + gender + "]");
            LOGGER.debug(">>> 입력데이터 오류 : " + gender);
            return ret.toString();
        }

        LOGGER.debug(">>> session : " + req.getSession());
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        LOGGER.debug(">>> user.type.deactivated : " + user.type.deactivated);

        if (user == null || user.id == null) {
            ret.put("resultCode", "05");
            ret.put("resultMsg", "사용자가 존재하지 않습니다.");
        } else {

            if (user.type.deactivated) {
                ret.put("resultCode", "06");
                ret.put("resultMsg", "탈퇴한 사용자입니다.");
            } else {

                try {

                    LOGGER.debug(">>> 생년월일 : " + birth + " / 성별 : " + gender);
                    /**
                     * female / male
                     */
                    this.userService.modifyUserAddInfo(user.id, birth, gender.toLowerCase());
                    ret.put("resultCode", "00");
                    ret.put("resultMsg", "SUCCESS");
                } catch (Exception e) {
                    ret.put("resultCode", "07");
                    ret.put("resultMsg", "insert exception : " + e.getMessage());
                    LOGGER.error(">>> /v2/api/user/update-birth-info Exception : " + e.getMessage());
                }

            }
        }

        return ret.toString();
    }

    /**
     * 로그인 사용자 비밀번호 재확인
     *
     * @param req
     * @param password
     * @param email
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/api/user/check-password", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> checkUserPassword(HttpServletRequest req, @RequestParam(value = "password", required = true) String password, @RequestParam(value = "email", required = false) String email)
            throws Exception {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {

            if (StringUtils.isEmpty(password)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("비밀번호가 입력되지 않았습니다.");
                return apiResponse;
            }

            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            User pUser = new User();
            pUser.id = user.id;
            pUser.passwordHash = password;

            // 날림
            user = null;

            User checkUser = this.userService.checkUserPassword(pUser);
            if (checkUser == null || checkUser.id == 0) {
                apiHeader.setResultCode("04");
                apiHeader.setResultMessage("사용자 정보가 존재하지 않습니다. (비밀번호 오류)");
                return apiResponse;
            }

            // 이메일을 입력시에만 체크
            if (StringUtils.isNotEmpty(email)) {
                if (!email.equalsIgnoreCase(checkUser.getEmail())) {
                    apiHeader.setResultCode("05");
                    apiHeader.setResultMessage("이메일이 일치하지 않습니다");
                    return apiResponse;
                }
            }

            if (checkUser.type.deactivated) {
                apiHeader.setResultCode("06");
                apiHeader.setResultMessage("탈퇴한 회원입니다.");
                return apiResponse;
            }

            if (checkUser.type.blocked) {
                apiHeader.setResultCode("07");
                apiHeader.setResultMessage("차단된 회원입니다.");
                return apiResponse;
            }

            // if (checkUser.type.anonymous) {
            // apiHeader.setResultCode("08");
            // apiHeader.setResultMessage("방문자입니다");
            // return apiResponse;
            // }

            apiHeader.setResultCode("00");
            apiHeader.setResultMessage(checkUser.getFullName() + " 고객님의 비밀번호를 확인하였습니다.");
            return apiResponse;

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            LOGGER.debug(">>> 비밀번호 확인 오류 : " + e.getMessage());
            return apiResponse;
        }

    }


    /**
     * 사용자 닉네임 변경
     *
     * @param req
     * @param nickname
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/api/user/update-nickname", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> updateUserNickname(HttpServletRequest req, @RequestParam(value = "nickname", required = true) String nickname) throws IOException {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {

            if (StringUtils.isEmpty(nickname)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("닉네임이 입력되지 않았습니다.");
                return apiResponse;
            }

            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            if (user.type.deactivated) {
                apiHeader.setResultCode("06");
                apiHeader.setResultMessage("탈퇴한 회원입니다.");
                return apiResponse;
            }

            if (user.type.blocked) {
                apiHeader.setResultCode("07");
                apiHeader.setResultMessage("차단된 회원입니다.");
                return apiResponse;
            }

            if (user.type.anonymous) {
                apiHeader.setResultCode("08");
                apiHeader.setResultMessage("방문자입니다.");
                return apiResponse;
            }


            if (this.userService.updateUserNickname(user.id, nickname) > 0) {
                apiHeader.setResultCode("00");
                apiHeader.setResultMessage(user.getFullName() + " 고객님의 닉네임을 변경하였습니다.");
                return apiResponse;
            } else {
                apiHeader.setResultCode("04");
                apiHeader.setResultMessage("사용자 정보가 존재하지 않습니다.");
                return apiResponse;
            }

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            LOGGER.debug(">>> 닉네임 변경 오류 : " + e.getMessage());
            return apiResponse;
        }


    }



    /**
     * 외부기기 로그인 허용여부 설정
     *
     * @param req
     * @param multiLoginYn
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/api/user/update-multilogin", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> updateUserMultiLogin(HttpServletRequest req, @RequestParam(value = "multiLoginYn", required = true) String multiLoginYn,
            @RequestParam(value = "gcmId", required = false) String gcmId, @RequestParam(value = "deviceId", required = false) String deviceId) throws IOException {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {

            if (StringUtils.isEmpty(multiLoginYn)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("설정값이 입력되지 않았습니다.");
                return apiResponse;
            }

            if (!("Y".equalsIgnoreCase(multiLoginYn) || "N".equalsIgnoreCase(multiLoginYn))) {
                apiHeader.setResultCode("04");
                apiHeader.setResultMessage("설정값 입력 오류 [" + multiLoginYn + "]");
                return apiResponse;
            }

            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            if (multiLoginYn.equalsIgnoreCase(user.multiLoginYn)) {
                apiHeader.setResultCode("05");
                apiHeader.setResultMessage("이미 설정되어 있습니다. [" + multiLoginYn + " :: user : " + user.multiLoginYn + "]");
                return apiResponse;
            }

            if (user.type.deactivated) {
                apiHeader.setResultCode("06");
                apiHeader.setResultMessage("탈퇴한 회원입니다.");
                return apiResponse;
            }

            if (user.type.blocked) {
                apiHeader.setResultCode("07");
                apiHeader.setResultMessage("차단된 회원입니다.");
                return apiResponse;
            }


            if (this.userService.updateUserMultiLogin(user.id, multiLoginYn, gcmId, deviceId) > 0) {
                apiHeader.setResultCode("00");
                apiHeader.setResultMessage(user.getFullName() + " 고객님의 외부기기 로그인 허용여부를 변경하였습니다.");
                return apiResponse;
            } else {
                apiHeader.setResultCode("08");
                apiHeader.setResultMessage("사용자 정보가 존재하지 않습니다.");
                return apiResponse;
            }

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            LOGGER.debug(">>> 외부기기 로그인 허용여부를 변경 오류 : " + e.getMessage());
            return apiResponse;
        }


    }



    /**
     * 외부기기 로그인 허용여부 확인
     *
     * @param req
     * @param multiLoginYn
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/v2/api/app/multilogin-check", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse<?> appMultiLoginCheck(HttpServletRequest req, @RequestParam(value = "gcmId", required = false) String gcmId,
            @RequestParam(value = "deviceId", required = false) String deviceId, @RequestParam(value = "osName", required = false) String osName,
            @RequestParam(value = "osVersion", required = false) String osVersion, @RequestParam(value = "maker", required = false) String maker,
            @RequestParam(value = "model", required = false) String model) throws IOException {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        try {

            if (StringUtils.isEmpty(gcmId) && StringUtils.isEmpty(deviceId)) {
                apiHeader.setResultCode("02");
                apiHeader.setResultMessage("GCM_ID, DEVICE_ID중 1개도 입력되지 않았습니다.");
                return apiResponse;
            }

            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

            if (user == null || user.id == 0) {
                apiHeader.setResultCode("03");
                apiHeader.setResultMessage("로그인한 사용자가 아닙니다.");
                return apiResponse;
            }

            if (user.type.deactivated) {
                apiHeader.setResultCode("06");
                apiHeader.setResultMessage("탈퇴한 회원입니다.");
                return apiResponse;
            }

            if (user.type.blocked) {
                apiHeader.setResultCode("07");
                apiHeader.setResultMessage("차단된 회원입니다.");
                return apiResponse;
            }

            // where null - is null 조건 처리 회피용
            if (StringUtils.isEmpty(osName)) {
                osName = "";
            }
            if (StringUtils.isEmpty(osVersion)) {
                osVersion = "";
            }
            if (StringUtils.isEmpty(maker)) {
                maker = "";
            }
            if (StringUtils.isEmpty(model)) {
                model = "";
            }

            // LOGGER.info(">>> login : 외부기기 설정 : " + user.multiLoginYn + "/ user.gcmId : " + user.gcmId + "/ gcmId : " + gcmId + "/ user.uniqueDeviceId : " + user.uniqueDeviceId + "/ deviceId : "
            // + deviceId + "/ osName : " + osName + "/ osVersion : " + osVersion + "/ maker : " + maker + "/ model : " + model);


            if (StringUtils.isEmpty(user.gcmId) && StringUtils.isNotEmpty(gcmId)) {
                // UPDATE user.gcm_id
                userService.updateUserGcmId(user.id, gcmId);
                user.gcmId = gcmId;
            }

            if (StringUtils.isEmpty(user.uniqueDeviceId) && StringUtils.isNotEmpty(deviceId)) {
                // UPDATE user.unique_device_id
                userService.updateUserUniqueDeviceId(user.id, deviceId);
                user.uniqueDeviceId = deviceId;
            }

            userService.updateUserDeviceInfo(user.id, osName, osVersion, maker, model);

            if ("Y".equalsIgnoreCase(user.multiLoginYn)) {
                apiHeader.setResultCode("00");
                apiHeader.setResultMessage("중복 로그인 허용 상태입니다.");
                return apiResponse;
            } else {

                if (StringUtils.isNotEmpty(gcmId)) {
                    if (!gcmId.equals(user.gcmId)) {
                        LOGGER.info(">>> login : gcmId가 일치하지 않아 로그아웃 해야 합니다.");
                        apiHeader.setResultCode("99");
                        apiHeader.setResultMessage("중복 로그인 비허용 [" + user.multiLoginYn + "] - GCM_ID가 일치하지 않습니다. - 로그아웃 수행 - [" + user.gcmId + "]");
                        return apiResponse;
                    }
                }

                if (StringUtils.isNotEmpty(deviceId)) {
                    if (!deviceId.equals(user.uniqueDeviceId)) {
                        LOGGER.info(">>> login : deviceId가 일치하지 않아 로그아웃 해야 합니다.");
                        apiHeader.setResultCode("99");
                        apiHeader.setResultMessage("중복 로그인 비허용 [" + user.multiLoginYn + "] - DEVICE_ID 가 일치하지 않습니다. - 로그아웃 수행 - [" + user.uniqueDeviceId + "]");
                        return apiResponse;
                    }
                }

                apiHeader.setResultCode("00");
                apiHeader.setResultMessage("같은 기기로 로그인한 상태입니다.");
                return apiResponse;

            }

        } catch (Exception e) {
            apiHeader.setResultCode("01");
            apiHeader.setResultMessage("exception : " + e.getMessage());
            LOGGER.debug(">>> 중복 로그인 가능여부 조회 중 오류 : " + e.getMessage());
            return apiResponse;
        }
    }

    /**
     * 세션으로 사용자 정보 조회 후 사용자 정보 반환
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/api/user/user-info", method = RequestMethod.GET)
    public @ResponseBody User login(HttpServletRequest req) {
        return userService.getUser(SessionAttrs.getUserId(req.getSession()));
    }

}
