package com.jaha.server.emaul.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaha.server.emaul.common.code.ParcelCode;
import com.jaha.server.emaul.constants.Constants;
import com.jaha.server.emaul.model.BaseSecuModel;
import com.jaha.server.emaul.model.GcmSendForm;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.ParcelCompany;
import com.jaha.server.emaul.model.ParcelLocker;
import com.jaha.server.emaul.model.ParcelLog;
import com.jaha.server.emaul.model.ParcelNotification;
import com.jaha.server.emaul.model.ParcelSmsPolicy;
import com.jaha.server.emaul.model.PushLog;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.repo.HouseRepository;
import com.jaha.server.emaul.repo.ParcelCompanyRepository;
import com.jaha.server.emaul.repo.ParcelLockerRepository;
import com.jaha.server.emaul.repo.ParcelLogRepository;
import com.jaha.server.emaul.repo.ParcelRepository;
import com.jaha.server.emaul.repo.ParcelSmsPolicyRepository;
import com.jaha.server.emaul.repo.PushLogRepository;
import com.jaha.server.emaul.repo.UserRepository;
import com.jaha.server.emaul.util.RandomKeys;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by doring on 15. 5. 20..
 */
@Service
public class ParcelServiceImpl implements ParcelService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private ParcelCompanyRepository parcelCompanyRepository;
    @Autowired
    private ParcelLockerRepository parcelLockerRepository;
    @Autowired
    private ParcelLogRepository parcelLogRepository;
    @Autowired
    private ParcelSmsPolicyRepository parcelSmsPolicyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private PushLogRepository pushLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GcmService gcmService;

    @Autowired
    private Environment env;

    @Autowired
    private UserService userService;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void save(ParcelNotification parcel) {
        parcelRepository.save(parcel);
    }

    @Override
    public ScrollPage<ParcelNotification> getParcelNotifyResult(Long aptId, Long lastItemId) {
        if (lastItemId == null || lastItemId == 0l) {
            lastItemId = Long.MAX_VALUE;
        }

        List<ParcelNotification> list =
                parcelRepository.findFirst20ByAptIdAndIdLessThan(aptId, lastItemId, new Sort(new Sort.Order(Sort.Direction.DESC, "sentDate"), new Sort.Order(Sort.Direction.DESC, "notifySuccess")));

        ScrollPage<ParcelNotification> ret = new ScrollPage<>();

        ret.setContent(list);

        final int size = list.size();
        if (size >= 20) {
            ret.setNextPageToken(String.valueOf(list.get(size - 1).id));
        }

        return ret;
    }

    @Override
    public ScrollPage<ParcelNotification> getParcelNotifications(User user, Long lastItemId) {
        if (!user.type.admin && !user.type.jaha && !user.type.user && !user.type.parcelChecker) {
            return null;
        }

        if (lastItemId == null || lastItemId == 0l) {
            lastItemId = Long.MAX_VALUE;
        }

        List<ParcelNotification> list =
                parcelRepository.findFirst20ByAptIdAndDongAndHoAndVisibleIsTrueAndIdLessThan(user.house.apt.id, user.house.dong, user.house.ho, lastItemId, new Sort(Sort.Direction.DESC, "sentDate"));

        ScrollPage<ParcelNotification> ret = new ScrollPage<>();

        ret.setContent(list);

        final int size = list.size();
        if (size >= 20) {
            ret.setNextPageToken(String.valueOf(list.get(size - 1).id));
        }

        return ret;
    }

    @Override
    public ParcelNotification disableParcelItem(User user, Long itemId) {
        if (!user.type.user && !user.type.admin && !user.type.jaha && !user.type.parcelChecker) {
            return null;
        }

        ParcelNotification parcel = parcelRepository.findOne(itemId);

        if (parcel != null) {
            if (parcel.aptId.equals(user.house.apt.id) && parcel.dong.equals(user.house.dong) && parcel.ho.equals(user.house.ho)) {
                parcel.visible = false;

                return parcelRepository.save(parcel);
            }
        }

        return null;
    }



    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 무인택배함 존재유무 및 인증 처리
     *
     * @param uuid
     * @param authKey
     * @throws Exception
     */
    private ParcelLocker checkLocker(String uuid, String authKey) throws Exception {
        // 무인택배함 조회
        ParcelLocker parcelLocker = this.parcelLockerRepository.findByUuid(uuid);

        if (parcelLocker == null) {
            throw new Exception(ParcelCode.RESP_NOTEXISTS.getMessage());
        }
        // 무인택배함 인증
        if (authKey == null || !authKey.equals(parcelLocker.getAuthKey())) {
            throw new Exception(ParcelCode.RESP_AUTHFAILS.getMessage());
        }

        return parcelLocker;
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 아파트 입주민 여부 판별
     *
     * @param aptId
     * @param phone
     * @return
     * @throws Exception
     */
    private List<User> checkAptUser(Long aptId, String phone, int dong, int ho) {
        List<User> returnUserList = null;
        BaseSecuModel bsm = new BaseSecuModel();

        List<User> userList = this.userRepository.findByPhone(bsm.encString(phone));

        if (userList != null && userList.size() > 0) {
            returnUserList = new ArrayList<User>();

            for (User user : userList) {
                if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
                    logger.info("<<탈퇴/차단/방문자 아이디>> {}", user.id);
                    continue;
                } else {
                    if (aptId.equals(user.house.apt.id)) {
                        logger.info("<<푸쉬 또는 SMS를 발송할 사용자 아이디>> {}", user.id);
                        returnUserList.add(user);
                        // break;
                    }
                }
            }
        } else {
            try {
                // House house = this.houseRepository.findOneByAptIdAndDongAndHo(aptId, String.valueOf(dong), String.valueOf(ho));
                List<House> houseList = this.houseRepository.findByAptIdAndDongAndHo(aptId, String.valueOf(dong), String.valueOf(ho));
                List<User> checkUserList = this.userRepository.findByHouseIdIn(Lists.transform(houseList, input -> input.id));

                if (checkUserList != null && checkUserList.size() > 0) {
                    logger.info("<<아파트아이디: {}, 동: {}, 호: {} 로 하우스정보 조회>>", aptId, dong, ho);

                    returnUserList = new ArrayList<User>();

                    for (User user : checkUserList) {
                        if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
                            logger.info("<<탈퇴/차단/방문자 아이디>> {}", user.id);
                            continue;
                        } else {
                            if (aptId.equals(user.house.apt.id)) {
                                logger.info("<<푸쉬 또는 SMS를 발송할 사용자 아이디>> {}", user.id);
                                returnUserList.add(user);
                                // break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("<<하우스아이디로 입주민 정보 조회가 안됨!>>", e);
            }
        }

        return returnUserList;
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description push 또는 sms 발송
     *
     * @param status
     * @param title
     * @param message
     * @param user
     * @param phone
     * @param aptId
     * @param parcelLogId
     * @throws Exception
     */
    @Transactional
    private void sendPushOrSms(String status, String title, String message, User userOrAdmin, String phone, Long aptId, int apiNumber, String pushTitle, String adText, Long parcelLogId)
            throws Exception {
        if (StringUtils.isBlank(message)) {
            logger.info("<<메시지가 없어 푸시 또는 SMS/MMS 발송을 하지 않습니다!>>");
            return;
        }

        if ("push".equals(status)) {
            if (userOrAdmin == null) {
                logger.info("<<사용자가 없어 푸시 또는 SMS/MMS 발송을 하지 않습니다!>>");
                return;
            }

            List<Long> androidUserIds = new ArrayList<Long>();
            List<Long> iosUserIds = new ArrayList<Long>();
            List<Long> androidNotAdUserIds = new ArrayList<Long>();
            List<Long> iosNotAdUserIds = new ArrayList<Long>();
            List<User> smsUsers = new ArrayList<User>();

            // String landingUrl = null;

            List<User> userList = Lists.newArrayList(userOrAdmin);

            for (User user : userList) {
                try {
                    if ("android".equals(user.kind)) {
                        String adapi = env.getProperty("adapi.data.service.url");
                        String adUrl = adapi + "?userId=" + user.id + "&category=9&pushLog=Y"; // 광고 플랫폼 API 개발
                        String jsonTmp = restTemplate.getForObject(adUrl, String.class);

                        JSONArray jsonArray = new JSONArray(jsonTmp);

                        if (jsonArray.length() == 0) {
                            androidNotAdUserIds.add(user.id);
                            logger.info("<<광고플랫폼 오류로 인한 사용자 아이디>> {}", user.id);
                        } else {
                            StringBuilder json = new StringBuilder("{" + '"' + "content" + '"' + ":[");
                            json.append(jsonArray.get(0).toString());
                            json.append("]," + '"' + "push_message" + '"' + ":" + '"' + message + '"');
                            json.append("," + '"' + "push_title" + '"' + ":" + '"' + StringUtil.nvl(pushTitle, "") + '"');
                            json.append("," + '"' + "ad_text" + '"' + ":" + '"' + adText + '"');
                            json.append("," + '"' + "prefix_text" + '"' + ":" + '"' + Constants.ADVERT_PUSH_PREFIX + '"');// 2017-01-26 광고 푸쉬의 (후원) prefix글자.
                            json.append("," + '"' + "api_number" + '"' + ":" + '"' + apiNumber + '"' + "}");
                            message = json.toString();

                            androidUserIds.add(user.id);
                            logger.info("<<안드로이드 사용자 아이디>> {}", user.id);
                        }
                    } else if ("ios".equals(user.kind)) {
                        String adapi = env.getProperty("adapi.data.service.url");
                        String adUrl = adapi + "?userId=" + user.id + "&category=9&pushLog=Y"; // 광고 플랫폼 API 개발
                        String jsonTmp = restTemplate.getForObject(adUrl, String.class);

                        JSONArray jsonArray = new JSONArray(jsonTmp);

                        if (jsonArray.length() == 0) {
                            iosNotAdUserIds.add(user.id);
                            logger.info("<<광고플랫폼 오류로 인한 iOS 사용자 아이디>> {}", user.id);
                        } else {
                            // JSONObject jsonObj = jsonArray.getJSONObject(0);
                            // String advertDescription = StringUtil.nvl(jsonObj.get("description"));
                            // landingUrl = jsonObj.getString("landing_url");
                            //
                            // if (StringUtils.isNotBlank(advertDescription)) {
                            // message = title + "\n" + advertDescription;
                            // }

                            iosUserIds.add(user.id);
                            logger.info("<<iOS 사용자 아이디>> {}", user.id);
                        }
                    } else {
                        smsUsers.add(user);
                        logger.info("<<안드로이드/iOS 사용자가 아닌 기타 사용자 아이디>> {}", user.id);
                    }
                } catch (Exception e) {
                    smsUsers.add(user);
                    logger.info("<<오류({})로 인한 기타 사용자 아이디>> {}", e.getMessage(), user.id);
                }
            }

            List<Long> pushIds = new ArrayList<Long>();

            if (!androidUserIds.isEmpty()) {
                try {
                    for (Long userId : androidUserIds) {
                        try {
                            PushLog pushLog = new PushLog();
                            pushLog.setAptId(aptId);
                            pushLog.setUserId(userId);
                            pushLog.setTitle(title);
                            pushLog.setMessage(message);
                            pushLog.setGubun("parcel-ad");

                            pushLog.setDeviceRecYn("N");
                            pushLog.setPushSendCount(1);
                            pushLog.setPushClickCount(0);
                            pushLog.setSmsYn("N");
                            pushLog.setEtc(String.valueOf(parcelLogId));

                            PushLog resultPushLog = pushLogRepository.saveAndFlush(pushLog);
                            pushIds.add(resultPushLog.getId());
                            logger.info("<<안드로이드 광고푸시로그 저장 성공>> {}", resultPushLog.toString());
                        } catch (Exception e) {
                            logger.error("<<안드로이드 광고푸시로그 저장 중 오류>>", e);
                        }
                    }

                    GcmSendForm form = new GcmSendForm();
                    Map<String, String> msg = Maps.newHashMap();
                    msg.put("push_type", "parcel-ad");
                    msg.put("type", "parcel-ad");
                    msg.put("title", StringUtil.nvl(title, ""));
                    msg.put("value", message);
                    msg.put("push_check_ids", StringUtils.join(pushIds, ",")); // 1,2,3,4 의 형태
                    form.setUserIds(androidUserIds);
                    form.setMessage(msg);
                    this.gcmService.sendGcm(form);
                    logger.info("<<안드로이드 사용자에게 광고푸쉬 발송>> {}", message);
                } catch (Exception e) {
                    logger.error("<<무인택배함-안드로이드 사용자-푸쉬 발송 중 오류>>", e);
                }
            }

            if (!androidNotAdUserIds.isEmpty()) {
                for (Long userId : androidNotAdUserIds) {
                    try {
                        PushLog pushLog = new PushLog();
                        pushLog.setAptId(aptId);
                        pushLog.setUserId(userId);
                        pushLog.setTitle(title);
                        pushLog.setMessage(message);
                        pushLog.setGubun("parcel-noad");

                        pushLog.setDeviceRecYn("N");
                        pushLog.setPushSendCount(1);
                        pushLog.setPushClickCount(0);
                        pushLog.setSmsYn("N");
                        pushLog.setEtc(String.valueOf(parcelLogId));

                        PushLog resultPushLog = pushLogRepository.saveAndFlush(pushLog);
                        pushIds.add(resultPushLog.getId());
                        logger.info("<<안드로이드 일반푸시로그 저장 성공>> {}", resultPushLog.toString());
                    } catch (Exception e) {
                        logger.error("<<안드로이드 일반푸시로그 저장 중 오류>>", e);
                    }
                }

                GcmSendForm form = new GcmSendForm();
                Map<String, String> msg = Maps.newHashMap();
                msg.put("type", "action");
                msg.put("title", StringUtil.nvl(title));
                msg.put("value", message);
                msg.put("push_check_ids", StringUtils.join(pushIds, ",")); // 1,2,3,4 의 형태
                form.setUserIds(androidNotAdUserIds);
                form.setMessage(msg);
                this.gcmService.sendGcm(form);
                logger.info("<<광고푸시 오류가 발생한 경우 일반푸시 발송>> {}", message);
            }

            if (!iosUserIds.isEmpty()) {
                for (Long userId : iosUserIds) {
                    try {
                        PushLog pushLog = new PushLog();
                        pushLog.setAptId(aptId);
                        pushLog.setUserId(userId);
                        pushLog.setTitle(title);
                        pushLog.setMessage(message);
                        pushLog.setGubun("parcel-ad");

                        pushLog.setDeviceRecYn("N");
                        pushLog.setPushSendCount(1);
                        pushLog.setPushClickCount(0);
                        pushLog.setSmsYn("N");
                        pushLog.setEtc(String.valueOf(parcelLogId));

                        PushLog resultPushLog = pushLogRepository.saveAndFlush(pushLog);
                        pushIds.add(resultPushLog.getId());
                        logger.info("<<아이폰 광고푸시로그 저장 성공>> {}", resultPushLog.toString());
                    } catch (Exception e) {
                        logger.error("<<아이폰 광고푸시로그 저장 중 오류>>", e);
                    }
                }

                GcmSendForm form = new GcmSendForm();
                Map<String, String> msg = Maps.newHashMap();
                msg.put("type", "action");
                msg.put("title", StringUtil.nvl(title));
                msg.put("value", message);
                // msg.put("push_check_ids", StringUtils.join(pushIds, ",")); // 1,2,3,4 의 형태
                // if (StringUtils.isNotBlank(landingUrl)) {
                // msg.put("action", landingUrl);
                // }
                msg.put("action", String.format("emaul://push-detail?id=%s", StringUtils.join(pushIds, ",")));
                form.setUserIds(iosUserIds);
                form.setMessage(msg);
                this.gcmService.sendGcm(form);
                logger.info("<<아이폰 사용자에게 광고 푸쉬 발송>> {}", message);
            }

            if (!iosNotAdUserIds.isEmpty()) {
                for (Long userId : iosNotAdUserIds) {
                    try {
                        PushLog pushLog = new PushLog();
                        pushLog.setAptId(aptId);
                        pushLog.setUserId(userId);
                        pushLog.setTitle(title);
                        pushLog.setMessage(message);
                        pushLog.setGubun("parcel-noad");

                        pushLog.setDeviceRecYn("N");
                        pushLog.setPushSendCount(1);
                        pushLog.setPushClickCount(0);
                        pushLog.setSmsYn("N");
                        pushLog.setEtc(String.valueOf(parcelLogId));

                        PushLog resultPushLog = pushLogRepository.saveAndFlush(pushLog);
                        pushIds.add(resultPushLog.getId());
                        logger.info("<<아이폰 일반푸시로그 저장 성공>> {}", resultPushLog.toString());
                    } catch (Exception e) {
                        logger.error("<<아이폰 일반푸시로그 저장 중 오류>>", e);
                    }
                }

                GcmSendForm form = new GcmSendForm();
                Map<String, String> msg = Maps.newHashMap();
                msg.put("type", "action");
                msg.put("title", StringUtil.nvl(title));
                msg.put("value", message);
                msg.put("action", "emaul://push-list");
                // msg.put("push_check_ids", StringUtils.join(pushIds, ",")); // 1,2,3,4 의 형태
                form.setUserIds(iosNotAdUserIds);
                form.setMessage(msg);
                this.gcmService.sendGcm(form);
                logger.info("<<광고푸시 오류가 발생한 경우 일반푸시 발송>> {}", message);
            }

            if (!smsUsers.isEmpty()) {
                for (User u : smsUsers) {
                    try {
                        // BaseSecuModel bsm = new BaseSecuModel();
                        if (phone != null) {
                            this.sendSms(title, message, u.getPhone(), aptId, parcelLogId);
                            logger.info("<<안드로이드/아이폰도 아닌 기타 사용자 SMS 발송>>", u.id);
                        }
                    } catch (Exception e) {
                        logger.error("<<무인택배함-기타 사용자-SMS발송 중 오류>>", e);
                    }
                }
            }
        } else {
            if (phone != null) {
                this.sendSms(title, message, phone, aptId, parcelLogId);
            }
        }
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description sms 발송
     *
     * @param title
     * @param message
     * @param phone
     * @param aptId
     * @param parcelLogId
     * @throws Exception
     */
    private void sendSms(String title, String message, String phone, Long aptId, Long parcelLogId) throws Exception {
        if (StringUtils.isBlank(message)) {
            logger.info("<<메시지가 없어 SMS/MMS 발송을 하지 않습니다!>>");
            return;
        }

        String code = String.format("%06d", (int) (Math.random() * 1000000));
        String key = RandomKeys.make(32);
        String uniqueKey = System.currentTimeMillis() + RandomKeys.make(6);
        String gubun = "parcel-" + aptId;

        // MMS 발송(5)
        String sql =
                "INSERT INTO uds_msg (MSG_TYPE, CMID, REQUEST_TIME, SEND_TIME, DEST_PHONE, SEND_PHONE, SUBJECT, MSG_BODY, ETC1, ETC2, ETC3, ETC4) VALUES (5, ?, SYSDATE(), SYSDATE(), ?, ?, ?, ?, ?, ?, ?, ?)";
        this.jdbcTemplate.update(sql, uniqueKey, phone, "028670816", title, message, code, key, gubun, parcelLogId);
        logger.debug("<<무인택배함-SMS 발송 쿼리>> {}", sql);

        // SMS 발송(0)
        // String sql = "INSERT INTO uds_msg (MSG_TYPE, CMID, REQUEST_TIME, SEND_TIME, DEST_PHONE, SEND_PHONE, MSG_BODY, ETC1, ETC2, ETC3, ETC4) VALUES (0, ?, SYSDATE(), SYSDATE(), ?, ?, ?, ?, ?, ?,
        // ?)";
        // this.jdbcTemplate.update(sql, uniqueKey, phone, "028670816", message, code, key, gubun, parcelLogId);

        logger.info("<<무인택배함 - SMS발송>> {}", message);

        this.sendSms4AppDownload(title, message, phone, aptId, parcelLogId);
    }

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description sms 발송
     *
     * @param title
     * @param message
     * @param phone
     * @param aptId
     * @param parcelLogId
     * @throws Exception
     */
    private void sendSms4AppDownload(String title, String message, String phone, Long aptId, Long parcelLogId) {
        try {
            ParcelSmsPolicy psp = this.findParcelSmsPolicy(aptId);

            if (psp != null && psp.getTestServiceValid()) { // 시범서비스 기간 중일 경우
                title = psp.getTestServiceMsgTitle();
                message = psp.getTestServiceMsg();

                String code = String.format("%06d", (int) (Math.random() * 1000000));
                String key = RandomKeys.make(32);
                String uniqueKey = System.currentTimeMillis() + RandomKeys.make(6);
                String gubun = "parcel-appdown-" + aptId;

                // MMS 발송(5)
                String sql =
                        "INSERT INTO uds_msg (MSG_TYPE, CMID, REQUEST_TIME, SEND_TIME, DEST_PHONE, SEND_PHONE, SUBJECT, MSG_BODY, ETC1, ETC2, ETC3, ETC4) VALUES (5, ?, SYSDATE(), SYSDATE(), ?, ?, ?, ?, ?, ?, ?, ?)";
                this.jdbcTemplate.update(sql, uniqueKey, phone, "028670816", title, message, code, key, gubun, parcelLogId);

                logger.debug("<<무인택배함-SMS 앱다운로드 발송 쿼리>> {}", sql);

                // SMS 발송(0)
                // String sql = "INSERT INTO uds_msg (MSG_TYPE, CMID, REQUEST_TIME, SEND_TIME, DEST_PHONE, SEND_PHONE, MSG_BODY, ETC1, ETC2, ETC3, ETC4) VALUES (0, ?, SYSDATE(), SYSDATE(), ?, ?, ?, ?,
                // ?,
                // ?, ?)";
                // this.jdbcTemplate.update(sql, uniqueKey, phone, "028670816", message, code, key, gubun, parcelLogId);

                logger.info("<<무인택배함 - 이마을앱 다운로드 MMS발송>> {}", message);
            } else {
                logger.info("<<무인택배함 SMS 발송 정책이 존재하지 않습니다!, 아파트 아이디: {}>>", aptId);
            }
        } catch (Exception e) {
            logger.info("<<무인택배함 이마을앱 다운로드 MMS 발송 중 오류 발생>>", e);
        }
    }

    /**
     *
     *
     * @param apiNumber
     * @return
     */
    private String getAdPushTitle(int apiNumber) {
        String text = StringUtils.EMPTY;

        if (apiNumber == 1) {
            text = "[이마을] %s(이)가 도착하였습니다."; // 택배사명
        } else if (apiNumber == 2) {
            // text = "";
        } else if (apiNumber == 3) {
            text = "[이마을] 택배가 발송 접수되었습니다.";
        } else if (apiNumber == 4) {
            text = "[이마을] 택배가 담당자 인수되었습니다";
        } else if (apiNumber == 5) {
            text = "[이마을] 반송택배가 접수되었습니다.";
        } else if (apiNumber == 6) {
            text = "[경고] 장기 보관중 택배를 찾아가세요.";
        }

        return text;
    }

    /**
     *
     *
     * @param apiNumber
     * @return
     */
    private String getAdPushText(int apiNumber) {
        String text = StringUtils.EMPTY;

        if (apiNumber == 1) {
            text = Constants.ADVERT_PUSH_PREFIX + " %s 무인택배함 %s번"; // 택배함명, 함번호
        } else if (apiNumber == 2) {
            // text = "";
        } else if (apiNumber == 3) {
            text = Constants.ADVERT_PUSH_PREFIX + " %s 택배함 %s번 택배 발송 접수";
        } else if (apiNumber == 4) {
            text = Constants.ADVERT_PUSH_PREFIX + " %s 택배함 %s번 택배 담당자 인수 완료";
        } else if (apiNumber == 5) {
            text = Constants.ADVERT_PUSH_PREFIX + " %s 택배함 %s번 반송택배 예약 접수 완료";
        } else if (apiNumber == 6) {
            text = Constants.ADVERT_PUSH_PREFIX + " %s 무인택배함 %s번";
        }

        return text;
    }

    /**
     *
     * @param apiNumber
     * @return
     */
    private String getMessageTitle(String gubun, int apiNumber) {
        String text = StringUtils.EMPTY;

        if ("push".equals(gubun)) {
            if (apiNumber == 1) {
                text = "e마을 - 택배 도착 알림";
            } else if (apiNumber == 2) {
                // text = "";
            } else if (apiNumber == 3) {
                text = "e마을 - 택배 발송 접수 완료";
            } else if (apiNumber == 4) {
                text = "e마을 - 택배 담당자 인수 완료";
            } else if (apiNumber == 5) {
                text = "e마을 - 반송 택배 예약 접수 완료";
            } else if (apiNumber == 6) {
                text = "e마을 - 장기 보관중 택배 경고 메세지";
            }
        } else if ("sms".equals(gubun)) {
            if (apiNumber == 1) {
                text = "[이마을-택배도착]";
            } else if (apiNumber == 2) {
                // text = "";
            } else if (apiNumber == 3) {
                // text = "";
            } else if (apiNumber == 4) {
                text = "[이마을-택배접수알림]";
            } else if (apiNumber == 5) {
                // text = "";
            } else if (apiNumber == 6) {
                text = "[이마을-미수취택배]";
            }
        }

        return text;
    }

    /**
     *
     *
     * @param gubun
     * @param apiNumber
     * @return
     */
    private String getMessage(String gubun, int apiNumber, String parcelLockerName, String lockerNum, String password, String parcelCompanyName, String parcelPhone) {
        String text = StringUtils.EMPTY;

        if ("push".equals(gubun)) {
            if (apiNumber == 1) {
                text = "[%s 택배함 %s번] 보관함에 택배가 도착하였습니다.\n비밀번호는[%s] 입니다.\n담당자: %s %s";
                text = String.format(text, parcelLockerName, lockerNum, password, parcelCompanyName, parcelPhone);
            } else if (apiNumber == 2) {
                // text = "";
            } else if (apiNumber == 3) {
                text = "[%s 택배함 %s번] 무인택배함에 택배접수가 완료되었습니다. 택배 담당자가 인수 완료되면 알림 메시지가 전송됩니다.";
                text = String.format(text, parcelLockerName, lockerNum);
            } else if (apiNumber == 4) {
                text = "[%s 택배함 %s번] 무인택배함에 발송접수된 택배가 담당자에게 인수되었습니다. 담당자: %s";
                text = String.format(text, parcelLockerName, lockerNum, parcelPhone);
            } else if (apiNumber == 5) {
                text = "[%s 택배함 %s번] 무인택배함에 보관중인 반송택배가 정상적으로 예약 접수 완료되었습니다. 택배 담당자가 인수 완료되면 알림 메시지가 전송됩니다.";
                text = String.format(text, parcelLockerName, lockerNum);
            } else if (apiNumber == 6) {
                text = "[%s 택배함 %s번] 장기보관중인 택배가 있습니다.\n택배를 보관기간 3일 내에 찾아가지 않으실 경우 정책상 관리사무소에서 임의로 택배를 꺼내 보관 할 수 있으니 꼭 확인 후 찾아가시기 바랍니다. \n비밀번호는 [%s] 입니다.";
                text = String.format(text, parcelLockerName, lockerNum, password);
            }
        } else if ("sms".equals(gubun)) {
            if (apiNumber == 1) {
                text = "%s 택배함 %s번 보관함 비밀번호 [%s] %s %s";
                text = String.format(text, parcelLockerName, lockerNum, password, parcelCompanyName, parcelPhone);
            } else if (apiNumber == 2) {
                // text = "";
            } else if (apiNumber == 3) {
                // text = "";
            } else if (apiNumber == 4) {
                text = "택배가 정상 접수되었습니다. 담당자: %s";
                text = String.format(text, parcelPhone);
            } else if (apiNumber == 5) {
                // text = "";
            } else if (apiNumber == 6) {
                text = "%s 택배함 %s번 보관함 비밀번호 [%s] 담당자: %s";
                text = String.format(text, parcelLockerName, lockerNum, password, parcelPhone);
            }
        }

        return text;
    }

    /**
     * 택배기사가 무인택배함에 택배를 보관하는 경우, API 1번
     */
    @Override
    public ParcelLog keepParcel(String uuid, String authKey, String lockerNum, String password, int dong, int ho, String phone, String parcelCompanyId, String parcelPhone, String date, int apiNumber)
            throws Exception {
        ParcelLocker parcelLocker = this.checkLocker(uuid, authKey);

        ParcelCompany parcelCompany = this.parcelCompanyRepository.findOne(parcelCompanyId);

        ParcelLog parcelLog = new ParcelLog();
        parcelLog.setParcelLocker(parcelLocker);
        parcelLog.setLockerNum(lockerNum);
        parcelLog.setPassword(password);
        parcelLog.setType("keep");
        parcelLog.setDong(dong);
        parcelLog.setHo(ho);
        parcelLog.setPhone(phone);
        parcelLog.setParcelCompany(parcelCompany);
        parcelLog.setParcelPhone(parcelPhone);
        parcelLog.setInputDate(date);

        List<User> userList = this.checkAptUser(parcelLocker.getApt().id, phone, dong, ho);

        if (userList == null || userList.isEmpty()) {
            logger.info("<<이마을 주민이 아니므로 SMS 발송!>>");

            parcelLog.setStatus("sms");
        } else {
            logger.info("<<이마을 주민이므로 푸쉬 발송!>>");

            parcelLog.setStatus("push");
        }

        parcelLog.setApiNumber(apiNumber);
        ParcelLog log = this.parcelLogRepository.saveAndFlush(parcelLog);

        // ///////////////////////////////////////////////////////////////////// 푸시 및 SMS 발송 처리 ///////////////////////////////////////////////////////////////////////
        String title = this.getMessageTitle(parcelLog.getStatus(), apiNumber);
        String message = this.getMessage(parcelLog.getStatus(), apiNumber, parcelLocker.getName(), lockerNum, password, parcelCompany.getName(), parcelPhone);

        String pushTitle = this.getAdPushTitle(apiNumber);
        if (parcelCompany != null) {
            pushTitle = String.format(pushTitle, parcelCompany.getName());
        }
        String adText = this.getAdPushText(apiNumber);
        adText = String.format(adText, parcelLocker.getName(), lockerNum);

        if (userList == null || userList.isEmpty()) { // if ("sms".equals(parcelLog.getStatus())) {
            this.sendPushOrSms(parcelLog.getStatus(), title, message, null, phone, parcelLocker.getApt().id, apiNumber, pushTitle, adText, log.getId()); // SMS 발송
        } else {
            for (User user : userList) {
                this.sendPushOrSms(parcelLog.getStatus(), title, message, user, phone, parcelLocker.getApt().id, apiNumber, pushTitle, adText, log.getId()); // 푸시 발송
            }
        }

        return log;
    }

    /**
     * 입주민이 무인택배함에서 택배를 찾는 경우, API 2번
     */
    @Override
    @Transactional
    public ParcelLog findUserParcel(String uuid, String authKey, String lockerNum, int dong, int ho, String phone, String date, int apiNumber) throws Exception {
        ParcelLog returnParcelLog = null;

        try {
            ParcelLocker parcelLocker = this.checkLocker(uuid, authKey);

            ParcelLog parcelLog = new ParcelLog();
            parcelLog.setParcelLocker(parcelLocker);
            parcelLog.setLockerNum(lockerNum);
            parcelLog.setType("find");
            parcelLog.setDong(dong);
            parcelLog.setHo(ho);
            parcelLog.setPhone(phone);
            parcelLog.setOutputDate(date);
            parcelLog.setApiNumber(apiNumber);

            returnParcelLog = this.parcelLogRepository.saveAndFlush(parcelLog);

            try {
                // 장기보관택배(API 6번) 조회
                long longId = this.parcelLogRepository.findLongKeepParcel(uuid, lockerNum, dong, ho);
                if (longId > 0) {
                    this.parcelLogRepository.updateFindDate(longId);
                    logger.info("<<장기보관택배 찾은날짜 수정, {}>>", longId);
                }

                // 직전보관택배(API 1번) 조회 후 찾은 날짜 수정
                long justBeforeId = this.parcelLogRepository.findKeepParcelJustBefore(uuid, lockerNum, dong, ho);
                this.parcelLogRepository.updateFindDate(justBeforeId);
                logger.info("<<직전보관택배 찾은날짜 수정, {}>>", justBeforeId);
            } catch (Exception e) {
                logger.error("<<입주민 택배찾기(API 2번) 처리중 오류 발생>>", e);
            }

        } catch (Exception e) {
            logger.error("<<입주민 택배찾기(API 2번) 등록 중 오류 발생>>", e);
        }

        return returnParcelLog;
    }

    @Override
    public ParcelLog keepUserParcel(String gubun, String uuid, String authKey, String lockerNum, String password, int dong, int ho, String phone, String parcelCompanyId, String parcelPhone,
            String date, int apiNumber) throws Exception {
        ParcelLocker parcelLocker = this.checkLocker(uuid, authKey);

        ParcelCompany parcelCompany = null;
        if (StringUtils.isNotBlank(parcelCompanyId)) {
            parcelCompany = this.parcelCompanyRepository.findOne(parcelCompanyId);
        }

        String parcelCompanyName = StringUtils.EMPTY;

        ParcelLog parcelLog = new ParcelLog();
        parcelLog.setParcelLocker(parcelLocker);
        parcelLog.setLockerNum(lockerNum);
        parcelLog.setPassword(password);
        parcelLog.setGubun(gubun);
        parcelLog.setType("keep");
        parcelLog.setDong(dong);
        parcelLog.setHo(ho);
        parcelLog.setPhone(phone);
        if (parcelCompany != null) {
            parcelLog.setParcelCompany(parcelCompany);
            parcelCompanyName = parcelCompany.getName();
        }
        parcelLog.setParcelPhone(parcelPhone);
        parcelLog.setInputDate(date);

        List<User> userList = this.checkAptUser(parcelLocker.getApt().id, phone, dong, ho);

        if (userList == null || userList.isEmpty()) {
            logger.info("<<이마을 주민이 아니므로 SMS 발송!>>");

            parcelLog.setStatus("sms");
        } else {
            logger.info("<<이마을 주민이므로 푸쉬 발송!>>");

            parcelLog.setStatus("push");
        }

        parcelLog.setApiNumber(apiNumber);
        ParcelLog log = this.parcelLogRepository.saveAndFlush(parcelLog);

        // ///////////////////////////////////////////////////////////////////// 푸시 및 SMS 발송 처리 ///////////////////////////////////////////////////////////////////////
        String title = this.getMessageTitle(parcelLog.getStatus(), apiNumber);
        String message = this.getMessage(parcelLog.getStatus(), apiNumber, parcelLocker.getName(), lockerNum, password, parcelCompanyName, parcelPhone);

        String pushTitle = this.getAdPushTitle(apiNumber);
        if (parcelCompany != null) {
            pushTitle = String.format(pushTitle, parcelCompany.getName());
        }
        String adText = this.getAdPushText(apiNumber);
        adText = String.format(adText, parcelLocker.getName(), lockerNum);

        if (userList == null || userList.isEmpty()) { // if ("sms".equals(parcelLog.getStatus())) {
            this.sendPushOrSms(parcelLog.getStatus(), title, message, null, phone, parcelLocker.getApt().id, apiNumber, pushTitle, adText, log.getId()); // SMS 발송
        } else {
            for (User user : userList) {
                this.sendPushOrSms(parcelLog.getStatus(), title, message, user, phone, parcelLocker.getApt().id, apiNumber, pushTitle, adText, log.getId()); // 푸시 발송
            }
        }

        return log;
    }

    @Override
    public ParcelLog findParcel(String gubun, String uuid, String authKey, String lockerNum, String password, int dong, int ho, String phone, String parcelCompanyId, String parcelPhone, String date,
            int apiNumber) throws Exception {
        ParcelLocker parcelLocker = this.checkLocker(uuid, authKey);

        ParcelCompany parcelCompany = null;
        if (StringUtils.isNotBlank(parcelCompanyId)) {
            parcelCompany = this.parcelCompanyRepository.findOne(parcelCompanyId);
        }

        String parcelCompanyName = StringUtils.EMPTY;

        ParcelLog parcelLog = new ParcelLog();
        parcelLog.setParcelLocker(parcelLocker);
        parcelLog.setLockerNum(lockerNum);
        parcelLog.setPassword(password);
        parcelLog.setGubun(gubun);
        parcelLog.setType("find");
        parcelLog.setDong(dong);
        parcelLog.setHo(ho);
        parcelLog.setPhone(phone);
        if (parcelCompany != null) {
            parcelLog.setParcelCompany(parcelCompany);
            parcelCompanyName = parcelCompany.getName();
        }
        parcelLog.setParcelPhone(parcelPhone);
        parcelLog.setOutputDate(date);

        List<User> userList = this.checkAptUser(parcelLocker.getApt().id, phone, dong, ho);

        if (userList == null || userList.isEmpty()) {
            logger.info("<<이마을 주민이 아니므로 SMS 발송!>>");

            parcelLog.setStatus("sms");
        } else {
            logger.info("<<이마을 주민이므로 푸쉬 발송!>>");

            parcelLog.setStatus("push");
        }

        parcelLog.setApiNumber(apiNumber);
        ParcelLog log = this.parcelLogRepository.saveAndFlush(parcelLog);

        // ///////////////////////////////////////////////////////////////////// 푸시 및 SMS 발송 처리 ///////////////////////////////////////////////////////////////////////
        String title = this.getMessageTitle(parcelLog.getStatus(), apiNumber);
        String message = this.getMessage(parcelLog.getStatus(), apiNumber, parcelLocker.getName(), lockerNum, password, parcelCompanyName, parcelPhone);

        String pushTitle = this.getAdPushTitle(apiNumber);
        if (parcelCompany != null) {
            pushTitle = String.format(pushTitle, parcelCompany.getName());
        }
        String adText = this.getAdPushText(apiNumber);
        adText = String.format(adText, parcelLocker.getName(), lockerNum);

        if (userList == null || userList.isEmpty()) { // if ("sms".equals(parcelLog.getStatus())) {
            this.sendPushOrSms(parcelLog.getStatus(), title, message, null, phone, parcelLocker.getApt().id, apiNumber, pushTitle, adText, log.getId()); // SMS 발송
        } else {
            for (User user : userList) {
                this.sendPushOrSms(parcelLog.getStatus(), title, message, user, phone, parcelLocker.getApt().id, apiNumber, pushTitle, adText, log.getId()); // 푸시 발송
            }
        }

        return log;
    }

    @Override
    public ParcelLocker findParcelLocker(String uuid) throws Exception {
        return this.parcelLockerRepository.findByUuid(uuid);
    }

    /**
     * 아파트아이디로 무인택배함 메시지 발송 정책을 조회한다.
     *
     * @param aptId
     * @return
     * @throws Exception
     */
    private ParcelSmsPolicy findParcelSmsPolicy(Long aptId) throws Exception {
        ParcelSmsPolicy psp = this.parcelSmsPolicyRepository.findOne(aptId);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, psp.getLongParcelCollectDay()); // 오늘 날짜 기준 longParcelCollectDay +
        String longParcelCollectDate = Constants.SHORT_DATE_SDF.format(calendar.getTime());

        psp.setLongParcelCollectDate(longParcelCollectDate + " 24:00:00");

        logger.info("{}", psp.toString());
        return psp;
    }

    @Override
    @Transactional
    public void modifyDeviceRecYn(Long id, String deviceType) throws Exception {
        this.pushLogRepository.updateDeviceRecYn(id, deviceType);
        logger.info("<<푸시 수신 확인 수정: {}, {}>>", id, deviceType);
    }

    @Override
    @Transactional
    public void modifyPushSendCount(Long id) throws Exception {
        this.pushLogRepository.updatePushSendCount(id);
        logger.info("<<푸시 발송 횟수 수정, {}>>", id);
    }

    @Override
    @Transactional
    public PushLog modifyPushClickCount(Long id, String deviceType) throws Exception {
        this.pushLogRepository.updatePushClickCount(id, deviceType);
        logger.info("<<푸시 클릭 횟수 수정: {}, {}>>", id, deviceType);

        return this.pushLogRepository.findOne(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jaha.server.emaul.service.ParcelService#modifySmsYn(java.lang.Long)
     */
    @Override
    @Transactional
    public void modifySmsYn(Long id) throws Exception {
        this.pushLogRepository.updateSmsYn(id);
        logger.info("<<SMS 발송 여부 수정, {}>>", id);
    }

    @Override
    @Transactional
    public void resendPush(String deviceRecYn, Date compDate) throws Exception {
        List<PushLog> resendList = this.pushLogRepository.findByDeviceRecYnIsAndModDateLessThan(deviceRecYn, compDate);

        if (resendList == null || resendList.isEmpty()) {
            logger.info("<<푸시 재발송 대상이 없어 작업을 중단합니다>>");
        } else {
            for (PushLog pushLog : resendList) {
                if ("N".equals(pushLog.getDeviceRecYn()) && "N".equals(pushLog.getSmsYn()) && pushLog.getPushSendCount() < 5) { // 푸시발송횟수가 5이하이면...
                    GcmSendForm form = new GcmSendForm();
                    Map<String, String> msg = Maps.newHashMap();
                    msg.put("title", StringUtil.nvl(pushLog.getTitle()));
                    msg.put("value", pushLog.getMessage());
                    form.setUserIds(Lists.newArrayList(pushLog.getUserId()));
                    msg.put("push_check_ids", StringUtils.join(String.valueOf(pushLog.getId()), ",")); // 1,2,3,4 의 형태

                    if ("ANDROID".equals(pushLog.getDeviceType())) {
                        msg.put("push_type", "parcel-ad");
                        msg.put("type", "parcel-ad");
                        form.setMessage(msg);
                        this.gcmService.sendGcm(form);
                        logger.info("<<안드로이드 사용자에게 광고푸쉬 발송>> {}", pushLog.getMessage());

                        this.pushLogRepository.updatePushSendCount(pushLog.getId());
                        logger.info("<<푸쉬 발송 카운트 1 증가>> {}", pushLog.getId());
                    } else if ("IOS".equals(pushLog.getDeviceType())) {
                        msg.put("type", "action");
                        form.setMessage(msg);
                        this.gcmService.sendGcm(form);
                        logger.info("<<아이폰 사용자에게 푸쉬 재발송>> {}", pushLog.getMessage());

                        this.pushLogRepository.updatePushSendCount(pushLog.getId());
                        logger.info("<<푸쉬 발송 카운트 1 증가>> {}", pushLog.getId());
                    }
                } else {
                    // 푸시를 5번 발송했는데 그래도 수신확인 안되면 SMS 발송
                    try {
                        // 기타(무인택배함로그ID)
                        String etc = pushLog.getEtc();

                        if (StringUtils.isNotBlank(etc)) {
                            Long parcelLogId = Long.valueOf(etc);
                            ParcelLog parcelLog = this.parcelLogRepository.findOne(parcelLogId);

                            String status = "sms";
                            String title = this.getMessageTitle(status, parcelLog.getApiNumber());
                            String message = this.getMessage(status, parcelLog.getApiNumber(), parcelLog.getParcelLocker().getName(), parcelLog.getLockerNum(), parcelLog.getPassword(),
                                    parcelLog.getParcelCompany().getName(), parcelLog.getParcelPhone());

                            this.sendPushOrSms(status, title, message, null, parcelLog.getPhone(), parcelLog.getParcelLocker().getApt().id, parcelLog.getApiNumber(), null, null, parcelLogId);

                            this.pushLogRepository.updateSmsYn(pushLog.getId());
                            logger.info("<<푸시를 5번 발송했는데 그래도 수신확인 안되어 SMS 발송>> {}", pushLog.getId());
                        }
                    } catch (Exception e) {
                        logger.error("<<무인택배함 - 재발송 처리 중 SMS 발송 중 오류발생>>", e);
                    }
                }
            }
        }
    }

    @Override
    public ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, Integer count) throws Exception {
        ScrollPage<PushLog> body = null;

        if (lastPushId == null || lastPushId == 0L) {
            lastPushId = Long.MAX_VALUE;
        }

        try {
            List<PushLog> pushLogList = this.pushLogRepository.findByIdLessThanAndUserId(lastPushId, userId, new Sort(new Sort.Order(Sort.Direction.DESC, "modDate")));
            body = new ScrollPage<>();
            body.setContent(pushLogList);

            final int size = pushLogList.size();
            if (size >= count) {
                body.setNextPageToken(String.valueOf(pushLogList.get(size - 1).getId()));
            }
        } catch (Exception e) {
            logger.error("<<푸시로그 목록 조회 중 오류 발생>>", e);
        }

        return body;
    }

    @Override
    public ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, String gubun, Pageable pageable) throws Exception {
        ScrollPage<PushLog> body = null;

        if (lastPushId == null || lastPushId == 0L) {
            lastPushId = Long.MAX_VALUE;
        }

        try {
            List<PushLog> pushLogList = this.pushLogRepository.findByIdLessThanAndUserIdAndGubun(lastPushId, userId, gubun, pageable);
            body = new ScrollPage<>();
            body.setContent(pushLogList);
            body.setPageNumber(pageable.getPageNumber());

            final int size = pushLogList.size();
            if (size >= pageable.getPageSize()) {
                body.setNextPageToken(String.valueOf(pushLogList.get(size - 1).getId()));
            }
        } catch (Exception e) {
            logger.error("<<푸시로그 목록 조회 중 오류 발생>>", e);
        }

        return body;
    }

    @Override
    public ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, String gubun, Integer count) throws Exception {
        ScrollPage<PushLog> body = null;

        if (lastPushId == null || lastPushId == 0L) {
            lastPushId = Long.MAX_VALUE;
        }

        if (StringUtils.isNotBlank(gubun)) {
            gubun = "parcel-ad";
        }

        if (count == null || count == 0) {
            count = 5;
        }

        try {
            List<PushLog> pushLogList = this.pushLogRepository.findByIdLessThanAndUserIdAndGubunOrderByIdDesc(lastPushId, userId, gubun);
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
    @Transactional
    public void sendPush4Admin() throws Exception {
        List<ParcelLog> longParcelList = this.parcelLogRepository.findLongKeepParcel4Admin();

        if (longParcelList == null || longParcelList.isEmpty()) {
            logger.info("<<1일 이상 장기미수취 택배가 없어 관리자 푸시 전송을 중단합니다!>>");
        } else {
            String adTitle = "[경고] 장기 보관 중 택배를 확인하세요.";
            String pushMessageTitle = "e마을 - 장기 보관 중 택배 경고 메시지";

            for (ParcelLog parcelLog : longParcelList) {
                try {
                    ParcelLog log = this.parcelLogRepository.findOne(parcelLog.getId());

                    String lockerName = log.getParcelLocker().getName();
                    String lockerNum = log.getLockerNum();
                    String password = log.getPassword();
                    int dong = log.getDong();
                    int ho = log.getHo();
                    String phone = log.getPhone();

                    String adText = Constants.ADVERT_PUSH_PREFIX + " %s 무인택배함 %s번";
                    String pushMessage = "[%s 택배함 %s번] 장기 보관중인 택배가 있습니다.\n택배를 보관기간 3일 내에 찾아가지 않으실 경우, 정책상 관리사무소에서 임의로 택배를 꺼내 보관 할 수 있음을 꼭 거주자에게 연락 후 택배를 찾아달라고 연락 부탁드립니다.\n택배 수취인: %s동 %s호 %s";

                    adText = String.format(adText, lockerName, lockerNum);
                    pushMessage = String.format(pushMessage, lockerName, lockerNum, password, dong, ho, phone);

                    long aptId = log.getParcelLocker().getApt().id;
                    List<User> adminList = this.userService.getAdminUsers(aptId);

                    if (adminList != null && adminList.size() > 0) {
                        for (User admin : adminList) {
                            this.sendPushOrSms("push", pushMessageTitle, pushMessage, admin, null, aptId, 0, adTitle, adText, 0L);
                        }
                    }
                } catch (Exception e) {
                    logger.error("<<1일 이상 장기미수취 택배 데이터 관리자 푸시 발송 중 오류 발생>>", e);
                }
            }
        }
    }

}
