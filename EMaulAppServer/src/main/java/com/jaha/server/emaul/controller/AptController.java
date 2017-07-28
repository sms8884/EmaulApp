package com.jaha.server.emaul.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.ApiResponseHeader;
import com.jaha.server.emaul.model.Apt;
import com.jaha.server.emaul.model.AptContact;
import com.jaha.server.emaul.model.AptFee;
import com.jaha.server.emaul.model.AptFeeAvr;
import com.jaha.server.emaul.model.AptScheduler;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.UserType;
import com.jaha.server.emaul.service.HouseService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAlarmSetting;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushGubun;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushMessage;
import com.jaha.server.emaul.v2.model.user.UserUpdateHistoryVo;
import com.jaha.server.emaul.v2.service.user.UserHouseTransferLogService;
import com.jaha.server.emaul.v2.util.PushUtils;

/**
 * Created by doring on 15. 3. 30..
 */
@Controller
public class AptController {

    private static final Logger logger = LoggerFactory.getLogger(AptController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private HouseService houseService;

    @Autowired
    private UserHouseTransferLogService userHouseTransferLogService;

    // @Autowired
    // private GcmService gcmService;

    // [START] 광고 푸시 추가 by realsnake 2016.10.28
    @Autowired
    private PushUtils pushUtils;

    // [END]

    @RequestMapping(value = "/api/public/apt/search", method = RequestMethod.GET)
    public @ResponseBody String searchApt(@RequestParam(value = "keyword") String keyword) {
        List<Apt> list = houseService.searchApt(keyword);

        return aptListToJsonForm(list);
    }

    @RequestMapping(value = "/api/public/apt/search/registered", method = RequestMethod.GET)
    public @ResponseBody String searchRegisteredApt(@RequestParam(value = "keyword") String keyword) {
        List<Apt> list = houseService.searchRegisteredApt(keyword);

        return aptListToJsonForm(list);
    }

    private String aptListToJsonForm(List<Apt> list) {
        JSONArray jsonArray = new JSONArray();
        for (Apt apt : list) {
            JSONObject obj = new JSONObject();
            obj.put("address", apt.strAddress);
            obj.put("addressOld", apt.strAddressOld);
            obj.put("name", apt.name);
            obj.put("code", apt.address.건물관리번호);
            jsonArray.put(obj);
        }

        return jsonArray.toString();
    }

    /**
     * create by shavrani 2016-10-17
     *
     * @desc 개편된 회원가입 아파트검색
     * @param params
     */
    @RequestMapping(value = "/api/public/apt/search/all")
    @ResponseBody
    public ApiResponse<?> searchAptAll(@RequestParam Map<String, Object> params) {

        ApiResponse<List<Map<String, Object>>> apiResponse = new ApiResponse<>();
        ApiResponseHeader apiHeader = apiResponse.getHeader();

        String sidoNm = StringUtil.nvl(params.get("sidoNm"));
        String sggNm = StringUtil.nvl(params.get("sggNm"));
        if (StringUtil.isBlank(sidoNm) || StringUtil.isBlank(sggNm)) {
            logger.info("<< /api/public/apt/search/all , required parameter is empty !! >>");
            apiHeader.setResultCode("99");
            apiHeader.setResultMessage("required parameter is empty");
            return apiResponse;
        }

        apiResponse.setBody(houseService.searchAptAll(params));

        return apiResponse;
    }

    @RequestMapping(value = "/api/apt/reset", method = RequestMethod.POST)
    public @ResponseBody User resetApt(HttpServletRequest req, @RequestParam Map<String, Object> params) {
        // Long userId = SessionAttrs.getUserId(req.getSession());
        // User user = userService.getUser(userId);
        // // user.house = userService.selectOrCreateHouse(addressCode, dong, ho);
        //
        // House house = user.house;
        // Apt apt = houseService.getAptByAddressCode(addressCode);
        // house.apt = apt;
        // house.dong = dong;
        // house.ho = ho;
        // user.house = houseService.saveAndFlush(house);
        // if (!user.type.jaha) {
        // user.type = new UserType(user.id);
        // }
        // // 아파트 다시 선택한 경우 관리자에게 알림 메시지 전송
        // if (user != null && user.type.anonymous && !user.type.admin && !user.type.jaha) {
        // gcmService.sendGcmToAdmin(userService.getAdminUsers(user.house.apt.id), user.house.dong +
        // "동 " + user.house.ho + "호 " + user.getFullName() + "님이 가입하셨습니다.\n관리자 웹페이지에서 주민 확인 후에
        // 승인해주세요.");
        // }
        //
        // req.getSession().invalidate();
        //
        // return userService.saveAndFlush(user);

        String addressCode = StringUtil.nvl(params.get("addressCode"));
        String dong = StringUtil.nvl(params.get("dong"));
        String ho = StringUtil.nvl(params.get("ho"));

        // addressCode가 없을시 받는 parameter
        String sidoNm = StringUtil.nvl(params.get("sidoNm"));
        String sggNm = StringUtil.nvl(params.get("sggNm"));
        String emdNm = StringUtil.nvl(params.get("emdNm"));
        String addressDetail = StringUtil.nvl(params.get("addressDetail"));

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        // 전출 처리를 위한 변수
        Boolean wasUser = user.type.user;
        Long oldHouseId = user.house.id;
        // 전출 처리를 위한 변수

        if (StringUtil.isBlank(addressCode)) {
            // addressCode가 없으면 동단위의 address 생성후 아파트 생성
            user.house = userService.selectOrCreateAddressAndHouse(sidoNm, sggNm, emdNm);
            user.setAddressDetail(addressDetail);
        } else {
            // addressCode가 있으면 조회하여 house 생성
            user.house = userService.selectOrCreateHouse(addressCode, dong, ho);
        }

        // 자하권한 이외에는 기존 권한을 모두 초기화한다.
        if (!user.type.jaha) {

            UserType userType = new UserType(user.id);
            if (StringUtil.isBlank(addressCode)) {
                // 가상아파트로 생성된 유저는 기본 type이 익명이 아니고 주민 ( 가상아파트는 주민승인해줄 관리자가 없음 )
                userType.anonymous = false;
                userType.user = true;
            } else {
                // 정상적인 아파트로 등록했지만 계약된 아파트가 아니면 관리자를 할 관리소가 없기때문에 주민으로 처리 ( 차후 계약아파트 지정시 주민권한박탈및 게시판 재정립해야함. )
                if (user.house.apt.registeredApt == false) {
                    userType.anonymous = false;
                    userType.user = true;
                }
            }
            user.type = userType;

        }

        // 전출 처리를 위한 변수
        Boolean isAnonymous = user.type.anonymous;

        // 사용자 주소 변경 시 전출 처리
        this.userHouseTransferLogService.saveTransferOutByUser(wasUser, isAnonymous, userId, oldHouseId);

        // -- 사용자 설정변경 HISTORY --
        try {
            userService.saveUserUpdateHistory(user, user, UserUpdateHistoryVo.TYPE_CHANGE_APT, null);
        } catch (Exception e) {
            logger.error(">>> 사용자 설정변경 히스토리 오류 [ 아파트변경 ]", e);
        }
        // -- 사용자 설정변경 HISTORY --

        // 아파트 다시 선택한 경우 관리자에게 알림 메시지 전송 ( admin jaha권한과 가상아파트 계약안된아파트만 걸러냄 )
        if (user != null && user.type.anonymous && !user.type.admin && !user.type.jaha && !user.house.apt.virtual && user.house.apt.registeredApt) {
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
            // gcmService.sendGcmToAdmin(userService.getAdminUsers(user.house.apt.id), user.house.dong + "동 " + user.house.ho + "호 " + user.getFullName() + "님이 가입하셨습니다.\n관리자 웹페이지에서 주민 확인 후에 승인해주세요.");

            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetAdminList(PushAlarmSetting.ALARM, Lists.newArrayList(user.house.apt.id));
            String value = String.format(PushMessage.USER_AGREE_REQ.getValue(), user.house.dong, user.house.ho, user.getFullName());

            this.pushUtils.sendPush(PushGubun.USER_AGREE_REQ, "주민 승인 요청", value, null, null, false, targetUserList);
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
        }

        req.getSession().invalidate();

        return userService.saveAndFlush(user);
    }

    @RequestMapping(value = "/api/apt/fee/list", method = RequestMethod.GET)
    public @ResponseBody List<AptFee> getAptFeeList(HttpServletRequest req) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user.house == null || user.house.apt == null) {
            return null;
        }

        // 임시로 권한 없이 모든 사용자가 관리비 볼 수 있게 함.
        // return houseService.getAptFeeList(user.house.id);

        if (user.house.apt.id == 1) {
            return houseService.getAptFeeList(user.house.id);
        } else if (user.type.admin || user.type.jaha || user.type.user || user.type.houseHost) {
            List<AptFee> feeList = houseService.getAptFeeList(user.house.id);
            if (feeList != null && !feeList.isEmpty()) {
                AptFee latestFee = feeList.get(0);
                JSONObject jsonObject = new JSONObject(latestFee.json);
                String latestEnterHouseDate = jsonObject.optString("입주일자");
                if (latestEnterHouseDate == null) {
                    return feeList;
                }

                List<AptFee> ret = Lists.newArrayList();
                for (AptFee aptFee : feeList) {
                    if (aptFee.date.compareTo(latestEnterHouseDate) >= 0) { // 아파트 관리비 부과월이 입주일자보다 큰 경우
                        ret.add(aptFee);
                    } else {
                        break;
                    }
                }

                return ret;
            }
        }

        return Lists.newArrayList();
    }

    @RequestMapping(value = "/api/apt/fee/avr/{date}", method = RequestMethod.GET)
    public @ResponseBody AptFeeAvr getAptFeeAvr(HttpServletRequest req, @PathVariable("date") String date) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (user.house == null || user.house.apt == null) {
            return null;
        }

        return houseService.getAptFeeAvr(user.house.apt.id, date, user.house.sizeMeter);
    }

    @RequestMapping(value = "/api/apt/contacts", method = RequestMethod.GET)
    public @ResponseBody List<AptContact> aptContacts(HttpServletRequest req) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        return user.house.apt.aptInfo.contacts;
    }

    @RequestMapping(value = "/api/apt/scheduler")
    @ResponseBody
    public String apiAptSchedulerData(HttpServletRequest req, @RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        List<AptScheduler> result = houseService.apiAptSchedulerData(user, startDate, endDate);

        return new JSONArray(result).toString();
    }

    @RequestMapping(value = "/api/apt/scheduler-detail")
    @ResponseBody
    public String apiAptSchedulerDetailData(HttpServletRequest req, @RequestParam(value = "id") String id) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        AptScheduler result = null;
        if (!StringUtils.isEmpty(id)) {
            result = houseService.apiAptSchedulerDetailData(user, Long.parseLong(id));
            if (result != null) {
                return new JSONObject(result).toString();
            }
        }
        return "{}";
    }

}
