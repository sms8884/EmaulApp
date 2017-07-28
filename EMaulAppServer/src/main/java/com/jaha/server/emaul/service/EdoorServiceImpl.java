package com.jaha.server.emaul.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaha.server.emaul.common.code.AdvertCode;
import com.jaha.server.emaul.common.code.Code;
import com.jaha.server.emaul.constants.Constants;
import com.jaha.server.emaul.mapper.AptApMapper;
import com.jaha.server.emaul.model.ApAccessLocationLog;
import com.jaha.server.emaul.model.AptAp;
import com.jaha.server.emaul.model.AptApAccessDevice;
import com.jaha.server.emaul.model.AptApAccessLog;
import com.jaha.server.emaul.model.AptApBrokenLog;
import com.jaha.server.emaul.model.AptApDaemonLog;
import com.jaha.server.emaul.model.AptApMonitoring;
import com.jaha.server.emaul.model.AptApMonitoringAlive;
import com.jaha.server.emaul.model.AptApMonitoringNoti;
import com.jaha.server.emaul.model.GcmSendForm;
import com.jaha.server.emaul.model.PushLog;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.repo.ApAccessLocationLogRepository;
import com.jaha.server.emaul.repo.AptApAccessDeviceRepository;
import com.jaha.server.emaul.repo.AptApAccessLogRepository;
import com.jaha.server.emaul.repo.AptApDaemonLogRepository;
import com.jaha.server.emaul.repo.AptApRepository;
import com.jaha.server.emaul.repo.PushLogRepository;
import com.jaha.server.emaul.repo.UserRepository;
import com.jaha.server.emaul.util.PoiUtil;
import com.jaha.server.emaul.util.RestFulUtil;
import com.jaha.server.emaul.util.StringUtil;

/**
 * @author shavrani
 * @since 2016. 9. 2.
 * @version 1.0
 */
@Service
public class EdoorServiceImpl implements EdoorService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Environment env;
    @Autowired
    private GcmService gcmService;
    @Autowired
    private RestFulUtil restFulUtil;
    @Autowired
    private AptApMapper aptApMapper;
    @Autowired
    private AptApRepository aptApRepository;
    @Autowired
    private AptApAccessLogRepository aptApAccessLogRepository;
    @Autowired
    private AptApAccessDeviceRepository aptApAccessDeviceRepository;
    @Autowired
    ApAccessLocationLogRepository apAccessLocationLogRepository;
    @Autowired
    private PushLogRepository pushLogRepository;
    @Autowired
    private AptApDaemonLogRepository aptApDaemonLogRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisOperations<Object, Object> redisOperations;
    @Autowired
    public JavaMailSender mailSender;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private CommonService commonService;

    @Override
    public List<AptAp> selectAptApAccessList(Map<String, Object> params) {
        return aptApMapper.selectAptApAccessList(params);
    }

    @Override
    public AptAp selectAptApAccess(Map<String, Object> params) {
        return aptApMapper.selectAptApAccess(params);
    }

    @Override
    public AptAp selectAptAp(Map<String, Object> params) {
        return aptApMapper.selectAptAp(params);
    }


    @Override
    public List<AptAp> selectAptApList(Map<String, Object> params) {
        return aptApMapper.selectAptApList(params);
    }

    @Override
    public void saveAptApAccessLog(AptApAccessLog aptApAccessLog) {
        aptApAccessLogRepository.save(aptApAccessLog);
    }

    @Override
    public List<AptApAccessLog> findAptApAccessRequestExist(Map<String, Object> params) {

        String aptApId = StringUtil.nvl(params.get("aptApId"));
        Long userId = StringUtil.nvlLong(params.get("userId"));
        String waitingYn = StringUtil.nvl(params.get("waitingYn"));

        return aptApAccessLogRepository.findByApIdAndUserIdAndWaitingYn(aptApId, userId, waitingYn);
    }

    @Override
    public List<Map<String, Object>> selectAptApAccessDeviceAuthList(Map<String, Object> params) {
        return aptApMapper.selectAptApAccessDeviceAuthList(params);
    }

    @Override
    public AptApAccessDevice findByAccessKeyAndDeactiveDateIsNull(String accessKey) {
        return aptApAccessDeviceRepository.findByAccessKeyAndDeactiveDateIsNull(accessKey);
    }

    @Override
    public String saveApAccessLocationLogBeacon(Map<String, Object> params) {

        String result = "N";

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String accessKey = StringUtil.nvl(params.get("accessKey"));
        String state = StringUtil.nvl(params.get("state"));

        params.put("_active", true);
        AptAp aptAp = selectAptAp(params);

        if (aptAp == null) {
            logger.info("ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
        } else {

            AptApAccessDevice aaad = findByAccessKeyAndDeactiveDateIsNull(accessKey);

            if (aaad == null) {
                logger.info("access device [ " + accessKey + " ] 가 존재하지않음");
            } else {
                ApAccessLocationLog existAall = apAccessLocationLogRepository.findByApIdAndAccessDeviceIdAndDisAppearIsNull(aptAp.id, aaad.id);
                ApAccessLocationLog aall = null;
                if (existAall == null) {
                    aall = new ApAccessLocationLog();
                } else {
                    aall = existAall;
                }

                // 현재 비콘의 사용자도 입력해준다. ( 로그이기때문에 나중에 해당 비콘의 사용자도 바뀔수있기때문에 현재 비콘의 사용자도 입력 )
                if (aaad.user != null) {
                    aall.userId = aaad.user.id;
                }

                aall.apId = aptAp.id;
                aall.accessDeviceId = aaad.id;
                if ("appear".equals(state)) {
                    aall.appear = new Date();
                }
                if ("disappear".equals(state) && existAall == null) {
                    logger.info(" # ap beacon uuid [ " + apBeaconUuid + " ] 로  location log에 disappear요청되었지만 appear된 상태의 accessKey [" + accessKey + "] 가 없음.");
                    return result;
                }
                if ("disappear".equals(state) && existAall != null) {
                    aall.disAppear = new Date();
                }
                ApAccessLocationLog saveResult = apAccessLocationLogRepository.save(aall);
                if (saveResult.id != null) {
                    result = "Y";
                }
            }
        }

        return result;
    }

    @Override
    public String saveApAccessLocationLogApp(Map<String, Object> params) {

        String result = "N";

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String mobileDeviceModel = StringUtil.nvl(params.get("mobileDeviceModel"));
        String mobileDeviceOs = StringUtil.nvl(params.get("mobileDeviceOs"));
        String state = StringUtil.nvl(params.get("state"));
        Long userId = StringUtil.nvlLong(params.get("userId"));

        params.put("_active", true);
        AptAp aptAp = selectAptAp(params);

        if (aptAp == null) {
            logger.info(" # ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
        } else {
            ApAccessLocationLog existAall = apAccessLocationLogRepository.findByApIdAndUserIdAndDisAppearIsNull(aptAp.id, userId);
            ApAccessLocationLog aall = null;
            if (existAall == null) {
                aall = new ApAccessLocationLog();
            } else {
                aall = existAall;
            }

            aall.userId = userId;
            aall.apId = aptAp.id;
            aall.mobileDeviceModel = mobileDeviceModel;
            aall.mobileDeviceOs = mobileDeviceOs;
            if ("appear".equals(state)) {
                aall.appear = new Date();
            }
            if ("disappear".equals(state) && existAall == null) {
                logger.info(" # ap beacon uuid [ " + apBeaconUuid + " ] 로  location log에 disappear요청되었지만 appear된 상태의 userId [" + userId + "] 가 없음.");
                return result;
            }
            if ("disappear".equals(state) && existAall != null) {
                aall.disAppear = new Date();
            }
            ApAccessLocationLog saveResult = apAccessLocationLogRepository.save(aall);
            if (saveResult.id != null) {
                result = "Y";
            }
        }

        return result;
    }

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
     * Created by shavrani on 16-08-17
     */
    @Override
    public String aptApAccessRequest(User user, Map<String, Object> params) {

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String mobileDeviceModel = StringUtil.nvl(params.get("mobileDeviceModel"));
        String mobileDeviceOs = StringUtil.nvl(params.get("mobileDeviceOs"));
        String inOut = StringUtil.nvl(params.get("inOut"));

        Date dbDate = aptApMapper.selectDate();// database server 시간

        AptApAccessLog aptApAccessLog = new AptApAccessLog();
        aptApAccessLog.userId = user.id;
        aptApAccessLog.mobileDeviceModel = mobileDeviceModel;
        aptApAccessLog.mobileDeviceOs = mobileDeviceOs;
        aptApAccessLog.accessDate = dbDate;// database server 시간으로 설정
        aptApAccessLog.inOut = inOut;
        aptApAccessLog.appVersion = user.appVersion;
        aptApAccessLog.openType = Code.APP_AP_OPEN_TYPE_SERVER.getCode();

        String result = "N";

        // user의 현재상태확인
        if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
            aptApAccessLog.success = "N";
            aptApAccessLog.memo = "유저의 현재 user.type e-door 이용불가 !! [ user id : " + user.id + ", name : " + user.getFullName() + " ] [ apBeaconUuid : " + apBeaconUuid + " ]";
            logger.info("<< " + aptApAccessLog.memo + " >>");
        } else {

            // edoor 알림 설정을 활성화해야만 서비스 이용가능처리.
            if (user.setting.notiEdoor) {

                params.put("_active", true);
                AptAp aptAp = selectAptAp(params);
                // ap가 존재하는지 확인
                if (aptAp == null) {
                    aptApAccessLog.success = "N";
                    aptApAccessLog.memo = "존재하지않는 AP]";
                    logger.info(" # " + aptApAccessLog.memo);
                } else {
                    aptApAccessLog.apId = StringUtil.nvl(aptAp.id);
                    aptApAccessLog.expIp = StringUtil.nvl(aptAp.expIp);

                    params.clear();
                    params.put("id", aptAp.id);
                    params.put("aptId", user.house.apt.id);
                    params.put("dong", user.house.dong);
                    params.put("ho", user.house.ho);
                    params.put("userId", user.id);
                    params.put("_active", true);

                    List<String> skipAuths = getSkipAuthUser(user);
                    if (skipAuths.size() > 0) {
                        params.put("skipAuths", skipAuths);
                    }

                    AptAp aptApAuth = aptApMapper.selectAptApAccess(params);

                    // ap에 출입권한 확인
                    if (aptApAuth == null) {
                        aptApAccessLog.success = "N";
                        aptApAccessLog.memo = "출입권한없음, [ user id : " + user.id + " ]";
                        logger.info(" # " + aptApAccessLog.memo);
                    } else {
                        String aptApId = StringUtil.nvl(aptAp.id);
                        List<AptApAccessLog> existList = aptApAccessLogRepository.findByApIdAndUserIdOrderByAccessDateDesc(aptApId, user.id);
                        // 연속 호출을 막기위한 코드
                        if (existList != null && existList.size() > 0) {
                            AptApAccessLog existLog = existList.get(0);// 0번째가 가장 최근의 기록
                            Date existLogDate = existLog.accessDate;
                            if (existLogDate != null) {
                                Long gap = dbDate.getTime() - existLogDate.getTime();
                                gap = gap / 1000;
                                if (gap <= 3) {
                                    // 임시 방편 코드 ( ios 예전 앱의 버전에서 연속 대량 호출이 발생하여 3초내에 요청이 있을경우는 skip 처리함. )
                                    return result;
                                }
                            }
                        }

                        aptApAccessLog.delayTime = dbDate.getTime();// database server 시간으로 millisecond 설정
                        aptApAccessLog.waitingYn = "Y";// 오픈성공하면 ap에서 성공 api를 호출할때 waitingYn 플래그를 삭제처리.
                        result = restFulUtil.edoorOpenRequest(user.id, aptAp.expIp);// ap에 open 요청
                        if ("N".equals(result)) {
                            aptApAccessLog.waitingYn = null;
                            aptApAccessLog.success = "N";
                            aptApAccessLog.memo = "AP connection fail";
                            logger.info(" # {} [ip : {}]", aptApAccessLog.memo, aptAp.expIp);
                        }

                        // AP 상태가 고장이면 고장표기 return ( 고장이어도 ap에 open 시도는 해본후에 고장 상태를 return )
                        if ("2".equals(StringUtil.nvl(aptAp.status))) {
                            aptApAccessLog.memo = StringUtil.nvl(aptApAccessLog.memo) + " [ AP 상태 : 고장 ]";
                            result = "X";
                        }
                    }
                }

            } else {
                aptApAccessLog.success = "N";
                aptApAccessLog.memo = "유저의 알림 미수신 설정으로 e-door 이용불가 !! [ user id : " + user.id + " , apBeaconUuid : " + apBeaconUuid + " ]";
                logger.info("<< " + aptApAccessLog.memo + " >>");
            }
        }

        saveAptApAccessLog(aptApAccessLog);

        return result;
    }

    /**
     * Created by shavrani on 16-08-17
     */
    @Override
    public String aptApAccessRequestExist(Map<String, Object> params) {

        String result = "N";

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        AptAp aptAp = selectAptAp(params);

        if (aptAp == null) {
            logger.info("ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
        } else {
            String aptApId = StringUtil.nvl(aptAp.id);
            String waitingYn = "Y";
            params.put("aptApId", aptApId);
            params.put("waitingYn", waitingYn);
            List<AptApAccessLog> existList = findAptApAccessRequestExist(params);

            if (existList == null || existList.size() == 0) {
                logger.info(" # " + "ap beacon uuid [ " + apBeaconUuid + " ] 의 요청목록이 없음");
            } else {
                // redisOperations.boundValueOps(apBeaconUuid).set(existList); redis에 ap open 요청 목록저장

                /** 임시코드 실제 빌드시 for문 전체 삭제 */
                for (int i = 0; i < existList.size(); i++) {
                    AptApAccessLog aaaLog = existList.get(i);
                    aaaLog.success = "Y";
                    aaaLog.waitingYn = null;
                    if (aaaLog.delayTime > 0 && System.currentTimeMillis() > aaaLog.delayTime) {
                        aaaLog.delayTime = System.currentTimeMillis() - aaaLog.delayTime;// 딜레이시간 계산 ( 현재시간에서 입력했던 시간을 뺀다. )
                    }
                    // AptApAccessLog jpa로 select한 데이터는 transaction 어노테이션으로인해 메소드 종료후에 변경된 데이터는 자동commit된다.

                    // send Gcm ( 광고포함 Gcm )
                    User user = userRepository.findOne(aaaLog.userId);
                    if (user != null) {
                        String title = aptAp.apName + "의 문을 열었습니다.";
                        String message = "좋은일만 가득하세요 ~";
                        String jsonTmp = restFulUtil.getAdvertListJsonString("9", user.id, "Y");

                        if (!StringUtil.isBlank(jsonTmp)) {
                            JSONObject json = new JSONObject();
                            json.put("content", jsonTmp);
                            json.put("push_message", message);
                            message = json.toString();
                        }

                        GcmSendForm form = new GcmSendForm();
                        Map<String, String> msg = Maps.newHashMap();
                        msg.put("push_type", "eDoor-ad");
                        msg.put("type", "eDoor-ad");
                        msg.put("title", StringUtil.nvl(title));
                        msg.put("value", message);
                        form.setUserIds(Lists.newArrayList(user.id));
                        form.setMessage(msg);

                        gcmService.sendGcm(form);

                        logger.info("[GCM발송] " + user.id + " {}", message);
                    }
                }

                result = "Y";
            }
        }

        return result;
    }

    /**
     * Created by shavrani on 16-08-23
     */
    @Override
    @Transactional
    public String aptApAccessOpenSuccess(Map<String, Object> params) {

        String result = "N";

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        Long userId = StringUtil.nvlLong(params.get("userId"));// 앱의 요청에 ap가 결과줄경우 존재
        String accessKey = StringUtil.nvl(params.get("userBeaconUuid"));// 비콘의 요청에 ap가 결과줄경우 존재
        String inOut = StringUtil.nvl(params.get("inOut"));
        String firstYn = StringUtil.nvl(params.get("firstYn"));

        params.put("_active", true);
        AptAp aptAp = selectAptAp(params);

        if (aptAp == null) {
            logger.info(" # " + "ap beacon uuid [ {} ] 가 존재하지않음", apBeaconUuid);
        } else {

            if (StringUtil.isBlank(accessKey)) {
                /** 앱에서 호출한 open을 ap에서 결과전송 */

                User user = userRepository.findOne(userId);
                if (user == null) {
                    logger.info(" # " + "userId [  ] 가 존재하지않음", userId);
                } else {
                    // redis에 저장한 목록이용하지않고 새로 조회해서 현시점까지 요청한 목록은 모두 update 처리
                    String aptApId = StringUtil.nvl(aptAp.id);
                    String waitingYn = "Y";
                    List<AptApAccessLog> existList = aptApAccessLogRepository.findByApIdAndUserIdAndWaitingYnOrderByAccessDateDescIdDesc(aptApId, userId, waitingYn);

                    if (existList == null || existList.size() == 0) {
                        logger.info(" # " + "ap beacon uuid [ {} ] 의 요청 aptApId [  ] userId [  ] 목록이 없음", apBeaconUuid, aptApId, userId);
                    } else {

                        Date dbDate = aptApMapper.selectDate();// database server 시간

                        for (int i = 0; i < existList.size(); i++) {
                            AptApAccessLog aaaLog = existList.get(i);
                            aaaLog.success = (i == 0 ? "Y" : "S"); // waitingYn이 Y인 항목중 가장 최근항목만 success Y로 표기하고 기존 항목들은 S로( skip ) 표기한다.
                            aaaLog.waitingYn = null;

                            long currTime = dbDate.getTime();// database server 시간으로 계산
                            // if (aaaLog.delayTime > 0 && currTime >= aaaLog.delayTime) {
                            aaaLog.delayTime = currTime - aaaLog.delayTime;// 딜레이시간 계산 ( 현재시간에서 입력했던 시간을 뺀다. )
                            // }
                            // AptApAccessLog jpa로 select한 데이터는 transaction 어노테이션으로인해 메소드 종료후에 변경된 데이터는 자동commit된다.
                        }

                        // firstYn이 업데이트되면 gcm은 Y일경우만 보내게 소스이동
                        if ("Y".equals(firstYn)) {
                            // gcm send source here
                        }

                        // Gcm 은 같은 유저에게 여러번 전송하지 않게 중복 유저 제거후 보낸다
                        HashSet<Long> hashset = new HashSet<Long>();
                        for (int i = 0; i < existList.size(); i++) {
                            AptApAccessLog aaaLog = existList.get(i);
                            hashset.add(aaaLog.userId);
                        }
                        Iterator<Long> iter = hashset.iterator();
                        while (iter.hasNext()) {
                            User checkUser = userRepository.findOne(iter.next());
                            if (checkUser != null) {
                                sendAdvertGcm(aptAp, checkUser, null);
                            }
                        }

                        result = "Y";

                        logger.info(" # " + "userId [ {} ] name [ {} ] apBeaconUuid [ {} ] e-door open success", userId, user.getFullName(), apBeaconUuid);
                    }

                    // ap open 요청목록저장한 항목 redis에서 조회후 삭제
                    // Object object = redisOperations.opsForValue().get(apBeaconUuid);
                    // if (object == null) {
                    // LOGGER.info(" # apBeaconUuid [" + apBeaconUuid + "] 로 저장된 항목이 redis에 없음.");
                    // } else {
                    // List<AptApAccessLog> list2 = (List<AptApAccessLog>) object;
                    // for (int i = 0; i < list2.size(); i++) {
                    // AptApAccessLog aaa = list2.get(i);
                    // }
                    // redisOperations.delete(apBeaconUuid);
                    // }
                }

            } else {
                /** 비콘에서 호출한 open을 ap에서 결과전송 */

                AptApAccessDevice aptApAccessDevice = findByAccessKeyAndDeactiveDateIsNull(accessKey);

                if (aptApAccessDevice == null) {
                    logger.info(" # access device [ {} ] 가 존재하지않음", accessKey);
                } else {

                    String logUser = "";

                    User user = aptApAccessDevice.user;

                    if (user == null) {
                        logger.info(" # " + "access device [ {} ] empty user, eDoor fail", accessKey);
                    } else {

                        AptApAccessLog aaaLog = new AptApAccessLog();
                        aaaLog.apId = StringUtil.nvl(aptAp.id);
                        aaaLog.expIp = StringUtil.nvl(aptAp.expIp);
                        aaaLog.accessDate = new Date();
                        aaaLog.inOut = inOut;
                        aaaLog.accessDeviceId = aptApAccessDevice.id;
                        aaaLog.userId = aptApAccessDevice.user.id;
                        logUser = "userId [ " + aptApAccessDevice.user.id + " ] name [ " + aptApAccessDevice.user.getFullName() + " ] ";

                        List<Map<String, Object>> aptApAccessDeviceAuthList = aptApMapper.selectAptApAccessDeviceAuthList(params);

                        // ap에 출입권한 확인
                        if (aptApAccessDeviceAuthList == null || aptApAccessDeviceAuthList.size() == 0) {
                            aaaLog.success = "N";
                            aaaLog.memo = "ap beacon uuid [ " + apBeaconUuid + " ] 에 출입권한없음";
                            logger.info(" # " + aaaLog.memo);
                        } else {
                            aaaLog.success = "Y";
                            // 비콘 사용자의 핸드폰으로 gcm 발송
                            sendAdvertGcm(aptAp, user, aptApAccessDevice);
                            result = "Y";
                        }

                        // 비콘 오픈요청 성공
                        aptApAccessLogRepository.save(aaaLog);

                        logger.info(" # access device [ {} ] " + logUser + " apBeaconUuid [ {} ] e-door open success", accessKey, apBeaconUuid);
                    }
                }
            }

        }

        return result;
    }

    /**
     * 광고 Gcm ( 앱, 비콘 공통 사용 aptApAccessOpenSuccess에 사용 )
     */
    private void sendAdvertGcm(AptAp aptAp, User user, AptApAccessDevice aptApAccessDevice) {

        String title = aptAp.apName + " 문을 통과하셨습니다.";
        String message = "";
        GcmSendForm form = new GcmSendForm();
        Map<String, String> msg = Maps.newHashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 (E) a h:mm", Locale.KOREAN);
        String currDate = sdf.format(Calendar.getInstance().getTime());
        String adText = Constants.ADVERT_PUSH_PREFIX + " ";
        String secondUser = "";
        if (aptApAccessDevice != null) {
            secondUser = aptApAccessDevice.secondUser + "님";
        }

        // 유저마다 광고가 달라질 확률이 있기때문에 각 유저마다 광고 message를 생성 ( 유저모두 모아서 한번에 보내는 방식은 안됨. )
        if ("android".equals(user.kind)) {
            // 안드로이드만 push 광고함.
            String jsonTmp = restFulUtil.getAdvertListJsonString(AdvertCode.PUSH_EDOOR_001.name(), user.id, "Y");
            JSONArray joTmp = new JSONArray(jsonTmp);

            JSONObject json = new JSONObject();
            json.put("content", joTmp);
            json.put("push_message", message);
            json.put("push_title", StringUtil.nvl(title));// 앱버전에 따라 다른 제목
            json.put("push_title2", (StringUtil.isBlank(secondUser) ? "" : "[" + secondUser + "]") + StringUtil.nvl(title));// 앱버전에 따라 다른 제목
            json.put("prefix_text", Constants.ADVERT_PUSH_PREFIX);// 2017-01-26 광고 푸쉬의 (후원) prefix글자.

            if (joTmp.length() > 0) {
                json.put("ad_text", adText + secondUser + " 출입 " + currDate);
            } else {
                json.put("ad_text", secondUser + " 출입 " + currDate);
            }

            // if (joTmp.length() > 0) {
            // JSONObject jsonTmpObj = joTmp.getJSONObject(0);
            // String advertDescription = StringUtil.nvl(jsonTmpObj.get("description"));
            // if (StringUtil.isBlank(advertDescription)) {
            // json.put("ad_text", message);
            // } else {
            // json.put("ad_text", advertDescription);
            // }
            // } // 광고설명제외 2016-09-09

            msg.put("type", "eDoor-ad");
            msg.put("title", StringUtil.nvl(title));
            json.put("api_number", 1); // 광고 push 알림 포맷이 여러개일경우로 구현될때를 대비해서 api_number를 지정함.
            message = json.toString();
            msg.put("value", message);
            msg.put("push_type", "eDoor-ad");
            form.setUserIds(Lists.newArrayList(user.id));
            form.setMessage(msg);
        } else if ("ios".equals(user.kind)) {
            // ios 는 push 광고없음, 인앱으로 앱내 상세페이지 이동후 별도 광고지면으로 광고함.
            message = title + "\n" + secondUser + " 출입 " + (StringUtil.isBlank(secondUser) ? "" : "\n") + currDate; // ios 메시지는 secondUser가 있으면 3줄 없으면 2줄로 표현
            msg.put("value", message);
            msg.put("type", "action");
            form.setMessage(msg);
            form.setUserIds(Lists.newArrayList(user.id));
        }

        // 푸쉬로그 저장후 gcmSend ( ios에는 action항목에 푸쉬로그 id를 인앱으로 처리. )
        PushLog pushLog = new PushLog();
        pushLog.setAptId(user.house.apt.id);
        pushLog.setUserId(user.id);
        pushLog.setTitle(title);
        pushLog.setMessage(message);
        pushLog.setGubun("eDoor-ad");
        pushLog.setDeviceType(user.kind);
        pushLog.setDeviceRecYn("Y");
        pushLog.setPushSendCount(1);
        pushLog.setPushClickCount(0);
        pushLog.setSmsYn("N");

        PushLog resultPushLog = pushLogRepository.saveAndFlush(pushLog);

        // android와 ios 별로 각 message 정리후 gcmSend 처리.
        if ("android".equals(user.kind)) {
            this.gcmService.sendGcm(form);
            logger.info(" # " + "userId [ {} ] name [ {} ] android gcm 발송", user.id, user.getFullName());
        } else if ("ios".equals(user.kind)) {

            // ios는 푸쉬로그를 먼저 저장후 action에 인앱으로 푸쉬로그의 id를 연결해준다.
            if (resultPushLog != null && resultPushLog.getId() > 0) {
                msg.put("action", "emaul://push-detail?id=" + resultPushLog.getId());
            }

            this.gcmService.sendGcm(form);
            logger.info(" # " + "userId [ {} ] name [ {} ] ios gcm 발송", user.id, user.getFullName());
        } else {
            logger.error(" # Gcm발송실패 android, ios 이외 사용자 [ userId : " + user.id + "] [ kind : " + user.kind + "]");
        }
    }

    /**
     * Created by shavrani on 16-08-17 ( 사용안함 )
     */
    @Override
    public String aptApAccessDevice(Map<String, Object> params) {

        String accessKey = StringUtil.nvl(params.get("accessKey"));
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String inOut = StringUtil.nvl(params.get("inOut"));// 출입 구분 (나간건지 들어온건지)

        params.put("_active", true);
        AptAp aptAp = selectAptAp(params);

        // 로그는 상황별 내용에 맞게 저장
        AptApAccessLog aptApAccessLog = new AptApAccessLog();
        aptApAccessLog.accessDate = new Date();
        aptApAccessLog.success = "N";
        aptApAccessLog.inOut = inOut;

        if (aptAp == null) {
            aptApAccessLog.memo = "ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음";
            logger.info(" # " + aptApAccessLog.memo);
        } else {

            aptApAccessLog.apId = StringUtil.nvl(aptAp.id);

            AptApAccessDevice aptApAccessDevice = findByAccessKeyAndDeactiveDateIsNull(accessKey);

            if (aptApAccessDevice == null) {
                aptApAccessLog.memo = "access device [ " + accessKey + " ] 가 존재하지않음";
                logger.info(" # " + aptApAccessLog.memo);
            } else {

                aptApAccessLog.accessDeviceId = aptApAccessDevice.id;

                // 아파트 ap 를 직접 권한준경우인지 체크
                String aptIds = aptApAccessDevice.aptApIds;
                if (!StringUtil.isBlank(aptIds)) {
                    String[] aptId = aptIds.split(",");
                    List<String> aptIdList = Arrays.asList(aptId);
                    if (aptIdList.contains(StringUtil.nvl(aptAp.id))) {
                        aptApAccessLog.success = "Y";
                    }
                }

                // 아파트 ap 직접권한이 아니면 access device에 맵핑된 사용자의 권한체크
                if ("N".equals(aptApAccessLog.success)) {
                    if (aptApAccessDevice.user != null) {

                        aptApAccessLog.userId = aptApAccessDevice.user.id;

                        Long id = StringUtil.nvlLong(aptAp.id);
                        Long userId = StringUtil.nvlLong(aptApAccessDevice.user.id);
                        String dong = StringUtil.nvl(aptApAccessDevice.user.house.dong);
                        String ho = StringUtil.nvl(aptApAccessDevice.user.house.ho);
                        List<String> skipAuths = getSkipAuthUser(aptApAccessDevice.user);

                        params.put("id", id);
                        params.put("userId", userId);
                        params.put("dong", dong);
                        params.put("ho", ho);
                        params.put("skipAuths", skipAuths);


                        AptAp aptApAuth = aptApMapper.selectAptApAccess(params);

                        if (aptApAuth != null) {
                            aptApAccessLog.success = "Y";
                        }
                    }
                }
            }

            if ("N".equals(aptApAccessLog.success)) {
                aptApAccessLog.memo = "ap beacon uuid [ " + apBeaconUuid + " ] 에 access device [ " + accessKey + " ] 의 출입권한없음";
                logger.info(" # " + aptApAccessLog.memo);
            }

        }

        aptApAccessLogRepository.save(aptApAccessLog);

        return aptApAccessLog.success;
    }

    /**
     * Created by shavrani on 16-08-22
     */
    @Override
    public String saveAptApExpIp(Map<String, Object> params) {

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));

        String result = "N";

        params.put("_active", true);
        AptAp aptAp = selectAptAp(params);
        // ap가 존재하는지 확인
        if (aptAp == null) {
            logger.info(" # ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
        } else {
            int save = aptApMapper.saveAptApExpIp(params);
            if (save > 0) {
                result = "Y";
            }
        }

        return result;
    }

    private List<String> getSkipAuthList() {
        /** authSkipList 코드성데이터가 없어 직접기입. */
        List<String> skipAuths = new ArrayList<String>();
        skipAuths.add("admin");
        skipAuths.add("gasChecker");
        return skipAuths;
    }

    /**
     * Created by shavrani on 16-08-26 AP에서 ( 자기자신을 자동저장 ) 호출하는 AP정보 등록/수정
     */
    @Override
    public Map<String, Object> saveAptAp(Map<String, Object> params) {

        Map<String, Object> result = new HashMap<String, Object>();

        long aptId = StringUtil.nvlLong(params.get("aptId"));
        String apUuid = StringUtil.nvl(params.get("uuidAp"));
        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String apBeaconMajor = StringUtil.nvl(params.get("apBeaconMajor"));
        String apBeaconMinor = StringUtil.nvl(params.get("apBeaconMinor"));
        String apId = StringUtil.nvl(params.get("apId"));
        String apName = StringUtil.nvl(params.get("apName"));
        String apPassword = StringUtil.nvl(params.get("apPassword"));
        String sshPassword = StringUtil.nvl(params.get("sshPassword"));
        String apExpIp = StringUtil.nvl(params.get("apExpIp"));
        String apRssi = StringUtil.nvl(params.get("apRssi"));
        String operationMode = StringUtil.nvl(params.get("operationMode"));
        String apReopenDelay = StringUtil.nvl(params.get("apReopenDelay"));
        String apModem = StringUtil.nvl(params.get("apModem"));
        String apNatWay = StringUtil.nvl(params.get("apNATWay"));
        String apFirmwareVersion = StringUtil.nvl(params.get("apFirmwareVersion"));
        String gpiodelay = StringUtil.nvl(params.get("gpiodelay"));
        String wifiMac = StringUtil.nvl(params.get("wifiMac"));

        if ("".equals(apBeaconUuid)) {
            logger.info(" # parameter 'apBeaconUuid' is null");
            result.put("resultCode", "98");
            result.put("resultMessage", "parameter 'apBeaconUuid' is null");
            return result;
        }

        AptAp aptAp = aptApRepository.findByApBeaconUuidAndDeactiveDateIsNull(apBeaconUuid);
        AptAp tempAptAp = null;

        if (aptAp == null) {

            // 2016-10-04 parameter는 apUuid, apBeaconUuid 두개만 받아서 저장 ( 다른 parameter는 일단 그대로 유지 )
            String missingParamList = "";
            if ("".equals(apUuid)) {
                logger.info(" # parameter 'apUuid' is null");
                missingParamList += "uuidAp ";
            }

            // parameter가 빠진게 있으면 fail
            if (!"".equals(missingParamList)) {
                result.put("resultCode", "98");
                result.put("resultMessage", "parameter [" + missingParamList + "] is null");
                return result;
            }

            tempAptAp = new AptAp();
            tempAptAp.aptId = aptId == 0 ? null : aptId;
            tempAptAp.apUuid = apUuid;
            tempAptAp.apBeaconUuid = apBeaconUuid;
            tempAptAp.apBeaconMajor = apBeaconMajor;
            tempAptAp.apBeaconMinor = apBeaconMinor;
            // tempAptAp.apId = apId; // ap_id와 apName는 저장후 생성된 id를 입력해 다시 저장한다.
            // tempAptAp.apName = apName;
            tempAptAp.apPassword = apPassword;
            tempAptAp.sshPassword = sshPassword;
            tempAptAp.expIp = apExpIp;
            tempAptAp.rssi = StringUtil.nvlInt(apRssi);
            tempAptAp.operationMode = StringUtil.defaultIfBlank(operationMode, "1");// 등록시에만 값이 없을경우 서버연동형으로 기본설정.
            tempAptAp.status = "1";
            tempAptAp.keepon = StringUtil.nvlInt(apReopenDelay);
            tempAptAp.modem = apModem;
            tempAptAp.firmwareVersion = apFirmwareVersion;
            tempAptAp.natWay = apNatWay;
            tempAptAp.gpiodelay = StringUtil.nvlInt(gpiodelay, StringUtil.nvlInt(env.getProperty("ap.config.gpiodelay")));
            tempAptAp.wifiMac = wifiMac;

            // 자동 ap 저장시에는 skipAuth 체크는 모두 기본체크된것으로 처리.
            List<String> skipAuthList = getSkipAuthList();
            tempAptAp.skipAuth = skipAuthList.stream().collect(Collectors.joining(","));

        } else {
            tempAptAp = aptAp;
            if (aptId > 0) {
                tempAptAp.aptId = aptId;
            }
            if (!StringUtil.isBlank(apUuid)) {
                tempAptAp.apUuid = apUuid;
            }
            if (!StringUtil.isBlank(apBeaconMajor)) {
                tempAptAp.apBeaconMajor = apBeaconMajor;
            }
            if (!StringUtil.isBlank(apBeaconMinor)) {
                tempAptAp.apBeaconMinor = apBeaconMinor;
            }
            if (!StringUtil.isBlank(apPassword)) {
                tempAptAp.apPassword = apPassword;
            }
            if (!StringUtil.isBlank(sshPassword)) {
                tempAptAp.sshPassword = sshPassword;
            }
            if (!StringUtil.isBlank(apExpIp)) {
                tempAptAp.expIp = apExpIp;
            }
            if (!StringUtil.isBlank(apRssi)) {
                tempAptAp.rssi = StringUtil.nvlInt(apRssi);
            }
            if (!StringUtil.isBlank(apReopenDelay)) {
                tempAptAp.keepon = StringUtil.nvlInt(apReopenDelay);
            }
            if (!StringUtil.isBlank(operationMode)) {
                tempAptAp.operationMode = operationMode;
            }
            if (!StringUtil.isBlank(apModem)) {
                tempAptAp.modem = apModem;
            }
            if (!StringUtil.isBlank(apFirmwareVersion)) {
                tempAptAp.firmwareVersion = apFirmwareVersion;
            }
            if (!StringUtil.isBlank(apNatWay)) {
                tempAptAp.natWay = apNatWay;
            }
            if (!StringUtil.isBlank(gpiodelay)) {
                tempAptAp.gpiodelay = StringUtil.nvlInt(gpiodelay);
            }
            if (!StringUtil.isBlank(wifiMac)) {
                tempAptAp.wifiMac = wifiMac;
            }
        }

        AptAp resultAptAp = aptApRepository.save(tempAptAp);
        if (resultAptAp == null) {
            result.put("resultCode", "99");
            result.put("resultMessage", "FAIL");
        } else {

            // 저장한후에 ap_id는 생성된 id를 입력해준다.
            resultAptAp.apId = StringUtil.nvl(resultAptAp.id);
            resultAptAp.apName = StringUtil.nvl(resultAptAp.id);
            aptApRepository.save(resultAptAp);

            result.put("resultCode", "00");
            result.put("id", resultAptAp.id);
        }

        return result;
    }

    @Override
    public AptApDaemonLog saveAptApDaemonLog(AptApDaemonLog aadl) {
        return aptApDaemonLogRepository.save(aadl);
    }

    @Override
    public List<Map<String, Object>> selectAptApAccessDeviceList(Map<String, Object> params) {
        return aptApMapper.selectAptApAccessDeviceList(params);
    }

    /**
     * 테스트용 mock up 비콘의 push
     */
    @Override
    public String apAccessBeaconMockupPush(Map<String, Object> params) {

        String result = "N";

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String accessKey = StringUtil.nvl(params.get("accessKey"));
        String state = StringUtil.nvl(params.get("state"));

        params.put("_active", true);
        AptAp aptAp = selectAptAp(params);

        if (aptAp == null) {
            logger.info("ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
        } else {

            AptApAccessDevice aaad = findByAccessKeyAndDeactiveDateIsNull(accessKey);

            if (aaad == null) {
                logger.info("access device [ " + accessKey + " ] 가 존재하지않음");
            } else {
                sendMockupAdvertGcm(aptAp, aaad, state);
                result = "Y";

            }
        }

        return result;
    }

    /**
     * 테스트용 mock up 비콘의 push 광고 method
     *
     * @param aptAp
     * @param aptApAccessDevice
     */
    private void sendMockupAdvertGcm(AptAp aptAp, AptApAccessDevice aptApAccessDevice, String state) {

        User user = aptApAccessDevice.user;
        String title = "";
        String message = "";
        GcmSendForm form = new GcmSendForm();
        Map<String, String> msg = Maps.newHashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 (E) a h:mm", Locale.KOREAN);
        String currDate = sdf.format(Calendar.getInstance().getTime());
        String adText = Constants.ADVERT_PUSH_PREFIX + " ";
        String secondUser = "";
        if (aptApAccessDevice != null) {
            secondUser = aptApAccessDevice.secondUser + "님";
        }

        if ("".equals(secondUser)) {
            title += aptAp.apName;
        } else {
            title += aptAp.apName + ("appear".equals(state) ? "에 " : "에서 ") + secondUser;
        }
        if ("appear".equals(state)) {
            title += "이 접근했습니다.";
        } else {
            title += "이 이탈했습니다.";
        }

        // 유저마다 광고가 달라질 확률이 있기때문에 각 유저마다 광고 message를 생성 ( 유저모두 모아서 한번에 보내는 방식은 안됨. )
        if ("android".equals(user.kind)) {
            // 안드로이드만 push 광고함.
            String jsonTmp = restFulUtil.getAdvertListJsonString(AdvertCode.PUSH_EDOOR_001.name(), user.id, "N");
            JSONArray joTmp = new JSONArray(jsonTmp);

            JSONObject json = new JSONObject();
            json.put("content", joTmp);
            json.put("push_message", message);
            json.put("push_title", StringUtil.nvl(title, ""));

            if (joTmp.length() > 0) {
                json.put("ad_text", adText + currDate);
            } else {
                json.put("ad_text", currDate);
            }

            msg.put("type", "eKinder-ad");
            msg.put("title", StringUtil.nvl(title));
            json.put("api_number", 1); // 광고 push 알림 포맷이 여러개일경우로 구현될때를 대비해서 api_number를 지정함.
            message = json.toString();
            msg.put("value", message);
            msg.put("push_type", "eKinder-ad");
            form.setUserIds(Lists.newArrayList(user.id));
            form.setMessage(msg);
        }

        if ("android".equals(user.kind)) {
            this.gcmService.sendGcm(form);
            logger.info("[ eKinder mock up android GCM발송] {}", msg);
        } else if ("ios".equals(user.kind)) {
            logger.info("[ eKinder mock up ios GCM발송은 테스트 안함.]");
        } else {
            logger.error(" # Gcm발송실패 android, ios 이외 사용자 [ userId : " + user.id + "] [ kind : " + user.kind + "]");
        }
    }

    @Override
    public AptApMonitoring selectAptApMonitoring(Map<String, Object> params) {
        return aptApMapper.selectAptApMonitoring(params);
    }

    @Override
    public int insertAptApMonitoring(AptApMonitoring aptApMonitoring) {
        return aptApMapper.insertAptApMonitoring(aptApMonitoring);
    }

    @Override
    public int insertAptApMonitoringNoti(AptApMonitoringNoti aptApMonitoringNoti) {
        return aptApMapper.insertAptApMonitoringNoti(aptApMonitoringNoti);
    }

    private List<Integer> getBaseDateList(int baseDays) {
        List<Integer> baseDateList = Lists.newArrayList();
        for (int i = 1; i <= baseDays; i++) {
            baseDateList.add(i);
        }
        return baseDateList;
    }

    @Override
    public int monitoringSendMailUserList() {

        int result = 1;

        List<SimpleUser> userList = aptApMapper.selectAptApInspAccountList("MAIL");

        if (userList == null || userList.isEmpty()) {
            result = -1;
            logger.info("<< AP Daily 모니터링 메일발송, 메일보낼 대상이 없어 종료함. >>");
        } else {

            File dailyEmptyFile = null;
            File dailyDataLimitFile = null;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            String baseDate = sdf.format(cal.getTime());
            Integer baseDays = 1;

            Map<String, Object> params = Maps.newHashMap();
            params.put("baseDate", baseDate);
            params.put("testAptId", Constants.AP_TEST_APT_ID);
            params.put("excludeAptId", Constants.AP_EXCLUDE_APT_ID);
            params.put("baseDateList", getBaseDateList(baseDays));
            params.put("includedOperationMode", Code.APP_AP_OPEN_TYPE_SERVER.getCode());// AP목록은 서버연동형이 포함된 AP만 검색한다.

            List<Map<String, Object>> warningSummaryAptList = aptApMapper.selectAptApInspWarningAptList(params);
            List<Map<String, Object>> dailyEmptyList = aptApMapper.selectAptApInspDailyList(params);
            List<Map<String, Object>> dailyDataLimitList = aptApMapper.selectAptApInspDataLimitList(params);

            cal.add(Calendar.DATE, -1);
            String yesterday = sdf.format(cal.getTime());
            DecimalFormat df = new DecimalFormat("#,##0");

            StringBuilder htmlContent = new StringBuilder();
            String theadCss = "font-size:10pt;background:#DFE0E0;border-top:2px solid #2185CF;border-bottom:1px solid #E5E5E5;";
            String tbodyCss = "font-size:9pt;text-align:center;border-bottom: 1px solid #E5E5E5;";

            String tempPath = env.getProperty("file.path.temp");

            if (warningSummaryAptList == null || warningSummaryAptList.isEmpty()) {
                logger.info("<< AP Daily 모니터링 메일발송, Daily Data Apt List 가 없습니다. >>");
            } else {

                htmlContent.append("<a href='http://emaul.co.kr/' >이마을로 이동</a>");
                htmlContent.append("<br /><br />");

                htmlContent.append("<h4><b>[ " + yesterday + " ] AP Daily Data Apt List 데이터 입니다. ( 총 " + df.format(warningSummaryAptList.size()) + "개 아파트에서 발생했습니다. ) </b></h4>");

                htmlContent.append("<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; width:800px; font-size:13px;'>");
                htmlContent.append("<thead>");
                htmlContent.append("<tr style='height:35px;'>");
                htmlContent.append("<th style='width:400px;" + theadCss + "'>아파트</th>");
                htmlContent.append("<th style='width:200px;" + theadCss + "'>미수신</th>");
                htmlContent.append("<th style='width:200px;" + theadCss + "'>데이터 사용량 경고</th>");
                htmlContent.append("</tr>");
                htmlContent.append("</thead>");

                htmlContent.append("<tbody>");
                for (Map<String, Object> item : warningSummaryAptList) {
                    htmlContent.append("<tr style='height:30px;'>");
                    htmlContent.append("<td style='width:400px;" + tbodyCss + "'>" + item.get("aptName") + "</td>");
                    htmlContent.append("<td style='width:200px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("dailyEmptyApCnt"))) + "</td>");
                    htmlContent.append("<td style='width:200px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("dataWarningApCnt"))) + "</td>");
                    htmlContent.append("</tr>");
                }
                htmlContent.append("</tbody>");

                htmlContent.append("</table>");
                htmlContent.append("<br /><br />");

            }

            if (dailyEmptyList == null || dailyEmptyList.isEmpty()) {
                logger.info("<< AP Daily 모니터링 메일발송, 미수신 List가 없습니다. >>");
                htmlContent.append("<h4><b>Daily 모니터링, 미수신 List가 없습니다.</b></h4><br />");
            } else {

                try {

                    htmlContent.append("<h4><b>Daily 모니터링 미수신 List가 있습니다. ( 총 " + df.format(dailyEmptyList.size()) + "건 ) 첨부파일을 참조해주세요.</b></h4>");

                    /** Excel로 첨부 **/
                    Workbook wb = new HSSFWorkbook();

                    int currRow = 0;
                    int currCell = 0;

                    Sheet sheet = wb.createSheet("Data Empty List");

                    sheet.setDefaultColumnWidth(19);

                    Row row = null;
                    Cell cell = null;

                    Map<String, CellStyle> cellStyleMap = PoiUtil.getCellStyle(wb);// 공통정의한 cell style

                    row = sheet.createRow(currRow++);// 한칸띄기용 row

                    row = sheet.createRow(currRow++);
                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                    cell.setCellValue("");
                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                    cell.setCellStyle(cellStyleMap.get("defaultStyleStringValueLeft"));
                    cell.setCellValue("Daily 모니터링 미수신 List 입니다. ( 총 " + df.format(dailyEmptyList.size()) + "건 )");
                    currCell = 0;

                    row = sheet.createRow(currRow++);// 한칸띄기용 row

                    row = sheet.createRow(currRow++);
                    row.setHeightInPoints(20);

                    List<String> titles = Lists.newArrayList();
                    titles.add("아파트");
                    titles.add("ID");
                    titles.add("AP BEACON UUID");
                    titles.add("AP ID");
                    titles.add("AP 이름");
                    titles.add(yesterday);

                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                    cell.setCellValue("");
                    sheet.setColumnWidth(currCell, 10000);
                    for (int i = 0; i < titles.size(); i++) {
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleHeader"));
                        cell.setCellValue(titles.get(i));
                    }
                    currCell = 1;

                    int size = dailyEmptyList.size();
                    for (int i = 0; i < size; i++) {

                        Map<String, Object> item = dailyEmptyList.get(i);

                        row = sheet.createRow(currRow++);
                        row.setHeightInPoints(17);

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("aptName")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("id")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("apBeaconUuid")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("apId")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("apName")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("day1")));

                        currCell = 1;
                    }

                    dailyEmptyFile = new File(tempPath + File.separator + "AP Daily Data Empty " + yesterday + ".xls");
                    if (dailyEmptyFile.exists()) {
                        dailyEmptyFile.delete();
                    }
                    dailyEmptyFile.createNewFile();

                    FileOutputStream fs = null;
                    try {
                        fs = new FileOutputStream(dailyEmptyFile);
                        wb.write(fs);
                    } catch (Exception e) {
                        result = -1;
                        e.printStackTrace();
                    } finally {
                        if (fs != null) {
                            fs.close();
                        }
                    }


                    /** 메일내용에 table로 표현 **/
                    // htmlContent.append("<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; width:1150px; font-size:13px;'>");
                    // htmlContent.append("<thead>");
                    // htmlContent.append("<tr style='height:35px;'>");
                    // htmlContent.append("<th style='width:400px;" + theadCss + "'>아파트</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "'>ID</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "'>AP BEACON UUID</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "'>AP ID</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "'>AP 이름</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "'>" + yesterday + "</th>");
                    // htmlContent.append("</tr>");
                    // htmlContent.append("</thead>");
                    //
                    // htmlContent.append("<tbody>");
                    // for (Map<String, Object> item : dailyList) {
                    // htmlContent.append("<tr style='height:30px;'>");
                    // htmlContent.append("<td style='width:400px;" + tbodyCss + "'>" + item.get("aptName") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + item.get("id") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + item.get("apBeaconUuid") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + item.get("apId") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + item.get("apName") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + " color:red;'>" + item.get("day1") + "</td>");
                    // htmlContent.append("</tr>");
                    // }
                    // htmlContent.append("</tbody>");
                    //
                    // htmlContent.append("</table>");
                    htmlContent.append("<br />");

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (dailyDataLimitList == null || dailyDataLimitList.isEmpty()) {
                logger.info("<< AP Daily 모니터링 메일발송, 데이터 사용량 경고 List가 없습니다. >>");
                htmlContent.append("<h4><b>Daily 모니터링 데이터 사용량 경고 List가 없습니다.</b></h4><br />");
            } else {

                try {

                    htmlContent.append("<h4><b>Daily 모니터링 데이터 사용량 경고 List가 있습니다. ( 총 " + df.format(dailyDataLimitList.size()) + "건 ) 첨부파일을 참조해주세요.</b></h4>");

                    /** Excel로 첨부 **/
                    Workbook wb = new HSSFWorkbook();

                    int currRow = 0;
                    int currCell = 0;

                    Sheet sheet = wb.createSheet("Telecom Data Limit List");

                    sheet.setDefaultColumnWidth(19);

                    Row row = null;
                    Cell cell = null;

                    Map<String, CellStyle> cellStyleMap = PoiUtil.getCellStyle(wb);// 공통정의한 cell style

                    row = sheet.createRow(currRow++);// 한칸띄기용 row

                    row = sheet.createRow(currRow++);
                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                    cell.setCellValue("");
                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                    cell.setCellStyle(cellStyleMap.get("defaultStyleStringValueLeft"));
                    cell.setCellValue("Daily 모니터링 데이터 사용량 경고 List 입니다. ( 총 " + df.format(dailyDataLimitList.size()) + "건 )");
                    currCell = 0;

                    row = sheet.createRow(currRow++);// 한칸띄기용 row

                    List<String> titles = Lists.newArrayList();
                    titles.add("아파트");
                    titles.add("ID");
                    titles.add("AP BEACON UUID");
                    titles.add("AP ID");
                    titles.add("AP 이름");
                    titles.add("데이터용량(byte)");
                    titles.add("경고%");

                    // 두번 제목줄 생성해서 merge한다
                    row = sheet.createRow(currRow++);
                    row.setHeightInPoints(17);

                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                    cell.setCellValue("");
                    sheet.setColumnWidth(currCell, 10000);
                    for (int i = 0; i < titles.size(); i++) {
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleHeader"));
                        cell.setCellValue(titles.get(i));
                    }

                    int widthMergeCnt = 4;
                    for (int i = 0; i < widthMergeCnt; i++) {
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleHeader"));
                        cell.setCellValue(yesterday);// 오른쪽 3개 cell을 merge할 cell이어서 따로 cell생성
                    }
                    sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), cell.getColumnIndex() - (widthMergeCnt - 1), cell.getColumnIndex()));

                    // 위 제목줄의 처음 생성한 cell은 위아래로 merge하기위해 같은 스타일로 다음 row에 생성
                    row = sheet.createRow(currRow++);
                    row.setHeightInPoints(17);
                    currCell = 0;
                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                    cell.setCellValue("");
                    sheet.setColumnWidth(currCell, 10000);
                    for (int i = 0; i < titles.size(); i++) {
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleHeader"));
                        cell.setCellValue(titles.get(i));

                        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum() - 1, row.getRowNum(), cell.getColumnIndex(), cell.getColumnIndex()));
                    }

                    // 추가 title ( 첫번째 제목줄의 가로merge된 cell의 아래 row에 입력될 제목 )
                    List<String> appendTitles = Lists.newArrayList();
                    appendTitles.add("Tx");
                    appendTitles.add("Rx");
                    appendTitles.add("Total");
                    appendTitles.add("%");
                    for (int i = 0; i < appendTitles.size(); i++) {
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleHeader"));
                        cell.setCellValue(appendTitles.get(i));
                    }

                    currCell = 1;

                    int size = dailyDataLimitList.size();
                    for (int i = 0; i < size; i++) {

                        Map<String, Object> item = dailyDataLimitList.get(i);

                        row = sheet.createRow(currRow++);
                        row.setHeightInPoints(17);

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("aptName")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("id")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("apBeaconUuid")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("apId")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("apName")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(df.format(StringUtil.nvlInt(item.get("limitData")))));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("dataWarningPer")));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(df.format(StringUtil.nvlInt(item.get("dayTotalTxBytes1")))));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(df.format(StringUtil.nvlInt(item.get("dayTotalRxBytes1")))));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(df.format(StringUtil.nvlInt(item.get("dayTotalBytes1")))));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.get("dataTotalBytesPer1")));

                        currCell = 1;
                    }

                    dailyDataLimitFile = new File(tempPath + File.separator + "AP Telecom Data Limit " + yesterday + ".xls");
                    if (dailyDataLimitFile.exists()) {
                        dailyDataLimitFile.delete();
                    }
                    dailyDataLimitFile.createNewFile();

                    FileOutputStream fs = null;
                    try {
                        fs = new FileOutputStream(dailyDataLimitFile);
                        wb.write(fs);
                    } catch (Exception e) {
                        result = -1;
                        e.printStackTrace();
                    } finally {
                        if (fs != null) {
                            fs.close();
                        }
                    }

                    // cell.setCellStyle(cellStyleMap.get("styleNumberValue"));

                    // htmlContent.append("<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; width:1500px; font-size:13px;'>");
                    // htmlContent.append("<thead>");
                    // htmlContent.append("<tr style='height:30px;'>");
                    // htmlContent.append("<th style='width:300px;" + theadCss + "' rowspan='2'>아파트</th>");
                    // htmlContent.append("<th style='width:100px;" + theadCss + "' rowspan='2'>ID</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "' rowspan='2'>AP BEACON UUID</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "' rowspan='2'>AP ID</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "' rowspan='2'>AP 이름</th>");
                    // htmlContent.append("<th style='width:150px;" + theadCss + "' rowspan='2'>데이터용량(byte)</th>");
                    // htmlContent.append("<th style='width:100px;" + theadCss + "' rowspan='2'>경고%</th>");
                    // htmlContent.append("<th style='width:100px;" + theadCss + "' colspan='4'>" + yesterday + "</th>");
                    // htmlContent.append("</tr>");
                    //
                    // htmlContent.append("<tr style='height:30px;'>");
                    // htmlContent.append("<th style='width:100px;" + theadCss + "'>Tx</th>");
                    // htmlContent.append("<th style='width:100px;" + theadCss + "'>Rx</th>");
                    // htmlContent.append("<th style='width:100px;" + theadCss + "'>Total</th>");
                    // htmlContent.append("<th style='width:100px;" + theadCss + "'>%</th>");
                    // htmlContent.append("</tr>");
                    //
                    // htmlContent.append("</thead>");
                    //
                    // htmlContent.append("<tbody>");
                    // for (Map<String, Object> item : dailyDataLimitList) {
                    // htmlContent.append("<tr style='height:30px;'>");
                    // htmlContent.append("<td style='width:300px;" + tbodyCss + "'>" + item.get("aptName") + "</td>");
                    // htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + item.get("id") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + item.get("apBeaconUuid") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + item.get("apId") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + item.get("apName") + "</td>");
                    // htmlContent.append("<td style='width:150px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("limitData"))) + "</td>");
                    // htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + item.get("dataWarningPer") + "</td>");
                    // htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("dayTotalTxBytes1"))) + "</td>");
                    // htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("dayTotalRxBytes1"))) + "</td>");
                    // htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("dayTotalBytes1"))) + "</td>");
                    // htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + item.get("dataTotalBytesPer1") + "</td>");
                    // htmlContent.append("</tr>");
                    // }
                    // htmlContent.append("</tbody>");
                    //
                    // htmlContent.append("</table>");

                    htmlContent.append("<br />");

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (warningSummaryAptList != null & !warningSummaryAptList.isEmpty()) {

                if (warningSummaryAptList.size() > 20) {
                    // 메일에서 스크롤이 생길정도로 row가 있으면 메일내용 맨아래쪽에도 이마을 이동 링크를 생성한다.
                    htmlContent.append("<br /><a href='http://emaul.co.kr/' >이마을로 이동</a>");
                }

                String msgTitle = "[ AP Daily Monitoring ] AP Monitoring 경고 데이터가 발생하였습니다. [ " + yesterday + " ]";

                MimeMessage message = mailSender.createMimeMessage();
                try {

                    String sendUser = env.getProperty("spring.mail.username");

                    message.setSubject(msgTitle, "UTF-8");
                    // message.setText(htmlContent.toString(), "UTF-8", "html");
                    message.setFrom(new InternetAddress(sendUser));
                    message.setSentDate(new Date());

                    for (SimpleUser user : userList) {
                        logger.info("<< AP Monitoring Mail 발송대상 : {} {} {} >>", user.id, user.getFullName(), user.getEmail());
                        message.addRecipient(RecipientType.TO, new InternetAddress(user.getEmail()));
                    }

                    MimeBodyPart bodypart = new MimeBodyPart();
                    bodypart.setContent(htmlContent.toString(), "text/html;charset=utf-8");

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(bodypart);

                    if (dailyEmptyFile != null) {
                        MimeBodyPart attachPart = new MimeBodyPart();
                        attachPart.setDataHandler(new DataHandler(new FileDataSource(dailyEmptyFile)));
                        attachPart.setFileName(MimeUtility.encodeText(dailyEmptyFile.getName()));
                        multipart.addBodyPart(attachPart);
                    }

                    if (dailyDataLimitFile != null) {
                        MimeBodyPart attachPart = new MimeBodyPart();
                        attachPart.setDataHandler(new DataHandler(new FileDataSource(dailyDataLimitFile)));
                        attachPart.setFileName(MimeUtility.encodeText(dailyDataLimitFile.getName()));
                        multipart.addBodyPart(attachPart);
                    }

                    message.setContent(multipart);
                    mailSender.send(message);

                    // file을 mail에 첨부한후 삭제한다.
                    if (dailyEmptyFile != null && dailyEmptyFile.exists()) {
                        dailyEmptyFile.delete();
                    }

                    if (dailyDataLimitFile != null && dailyDataLimitFile.exists()) {
                        dailyDataLimitFile.delete();
                    }

                } catch (Exception e) {
                    result = -1;
                    e.printStackTrace();
                }

                List<Long> userIds = Lists.newArrayList();
                for (SimpleUser user : userList) {
                    userIds.add(user.id);
                    logger.info("<< AP Monitoring GCM 발송 {} {} >>", user.id, user.getFullName());
                }

                GcmSendForm form = new GcmSendForm();
                Map<String, String> msg = Maps.newHashMap();
                msg.put("type", "action");
                msg.put("title", StringUtil.nvl(msgTitle, ""));
                msg.put("value", "확인바랍니다.");
                form.setUserIds(userIds);
                form.setMessage(msg);

                gcmService.sendGcm(form);

            }

        }

        return result;

    }

    @Override
    public int monitoringNoHistoryApSendMail(Integer hour) {

        int result = 1;

        List<SimpleUser> userList = aptApMapper.selectAptApInspAccountList("MAIL");

        if (userList == null || userList.isEmpty()) {
            result = -1;
            logger.info("<< AP Daily 모니터링 메일발송, 메일보낼 대상이 없어 종료함. >>");
        } else {

            File emptyFile = null;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            String baseDate1 = sdf.format(cal.getTime());

            sdf.applyPattern("yyyy-MM-dd HH:mm");
            String baseDate2 = sdf.format(cal.getTime());

            int _hour = StringUtil.nvlInt(hour, 5);// 최근 조회 범위 시간

            Map<String, Object> params = Maps.newHashMap();
            params.put("testAptId", Constants.AP_TEST_APT_ID);
            params.put("excludeAptId", Constants.AP_EXCLUDE_APT_ID);
            params.put("_searchType", "day");
            params.put("_hour", _hour);
            List<AptAp> dayApList = aptApMapper.selectNoHistoryApList(params);
            List<Map<String, Object>> dayAptCntList = aptApMapper.selectNoHistoryApAptList(params);

            params.put("_searchType", "time");
            List<AptAp> timeApList = aptApMapper.selectNoHistoryApList(params);
            List<Map<String, Object>> timeAptCntList = aptApMapper.selectNoHistoryApAptList(params);

            DecimalFormat df = new DecimalFormat("#,##0");

            StringBuilder htmlContent = new StringBuilder();
            String theadCss = "font-size:10pt;background:#DFE0E0;border-top:2px solid #2185CF;border-bottom:1px solid #E5E5E5;";
            String tbodyCss = "font-size:9pt;text-align:center;border-bottom: 1px solid #E5E5E5;";

            String tempPath = env.getProperty("file.path.temp");

            // 최근 (지정된 시간)의 기록없는 AP의 아파트가 없으면 모두 skip한다.
            if (timeAptCntList == null || timeAptCntList.isEmpty()) {
                logger.info("<< AP 최근 출입기록 모니터링 메일발송, 최근출입기록이 없는 아파트가 없습니다. >>");
            } else {

                htmlContent.append("<a href='http://emaul.co.kr/' >이마을로 이동</a>");
                htmlContent.append("<br /><br />");

                // 최근 지정된 시간이내 출입기록성공이력 없는 AP 데이터
                htmlContent.append("<h4><b>[ " + baseDate2 + " ] 최근 ( " + _hour + "시간 이내 ) 출입성공기록이 없는 AP 데이터 입니다. ( 총 " + df.format(timeAptCntList.size()) + "개 아파트가 해당됩니다. ) </b></h4>");

                htmlContent.append("<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; width:700px; font-size:13px;'>");
                htmlContent.append("<thead>");
                htmlContent.append("<tr style='height:35px;'>");
                htmlContent.append("<th style='width:400px;" + theadCss + "'>아파트</th>");
                htmlContent.append("<th style='width:300px;" + theadCss + "'>출입성공기록없는 AP Count</th>");
                htmlContent.append("</tr>");
                htmlContent.append("</thead>");

                htmlContent.append("<tbody>");
                for (Map<String, Object> item : timeAptCntList) {
                    htmlContent.append("<tr style='height:30px;'>");
                    htmlContent.append("<td style='width:400px;" + tbodyCss + "'>" + item.get("aptName") + "</td>");
                    htmlContent.append("<td style='width:200px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("apCnt"))) + "</td>");
                    htmlContent.append("</tr>");
                }
                htmlContent.append("</tbody>");

                htmlContent.append("</table>");
                htmlContent.append("<br /><br />");


                // 금일 출입기록성공이력 없는 AP 데이터
                htmlContent.append("<h4><b>[ " + baseDate1 + " ] 금일 출입성공기록이 없는 AP 데이터 입니다. ( 총 " + df.format(dayAptCntList.size()) + "개 아파트가 해당됩니다. ) </b></h4>");

                htmlContent.append("<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; width:700px; font-size:13px;'>");
                htmlContent.append("<thead>");
                htmlContent.append("<tr style='height:35px;'>");
                htmlContent.append("<th style='width:400px;" + theadCss + "'>아파트</th>");
                htmlContent.append("<th style='width:300px;" + theadCss + "'>출입성공기록없는 AP Count</th>");
                htmlContent.append("</tr>");
                htmlContent.append("</thead>");

                htmlContent.append("<tbody>");
                for (Map<String, Object> item : dayAptCntList) {
                    htmlContent.append("<tr style='height:30px;'>");
                    htmlContent.append("<td style='width:400px;" + tbodyCss + "'>" + item.get("aptName") + "</td>");
                    htmlContent.append("<td style='width:200px;" + tbodyCss + "'>" + df.format(StringUtil.nvlInt(item.get("apCnt"))) + "</td>");
                    htmlContent.append("</tr>");
                }
                htmlContent.append("</tbody>");

                htmlContent.append("</table>");
                htmlContent.append("<br /><br />");

            }

            if (timeApList == null || timeApList.isEmpty()) {
                logger.info("<< AP 최근 출입기록 모니터링 메일발송, 데이터가 없습니다. >>");
                htmlContent.append("<h4><b>최근 ( " + _hour + "시간 이내 ) 출입성공기록이 없는 AP가 없습니다.</b></h4><br />");
            } else {

                try {

                    htmlContent.append("<h4><b>최근 ( " + _hour + "시간 이내 ) 출입성공기록이 없는 AP가 있습니다. ( 총 " + df.format(timeApList.size()) + "건 ) 첨부파일을 참조해주세요.</b></h4>");

                    /** Excel로 첨부 **/
                    Workbook wb = new HSSFWorkbook();

                    int currRow = 0;
                    int currCell = 0;

                    // 최근 출입성공기록이 없는 AP sheet
                    Sheet sheet = wb.createSheet("최근 ( " + _hour + "시간 이내 ) 출입성공기록이 없는 AP");

                    sheet.setDefaultColumnWidth(19);

                    Row row = null;
                    Cell cell = null;

                    Map<String, CellStyle> cellStyleMap = PoiUtil.getCellStyle(wb);// 공통정의한 cell style

                    row = sheet.createRow(currRow++);// 한칸띄기용 row

                    row = sheet.createRow(currRow++);
                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                    cell.setCellValue("");
                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                    cell.setCellStyle(cellStyleMap.get("defaultStyleStringValueLeft"));
                    cell.setCellValue("[ " + baseDate2 + " ] 최근 ( " + _hour + "시간 이내 ) 출입성공기록이 없는 AP 입니다. ( 총 " + df.format(timeApList.size()) + "건 )");
                    currCell = 0;

                    row = sheet.createRow(currRow++);// 한칸띄기용 row

                    row = sheet.createRow(currRow++);
                    row.setHeightInPoints(20);

                    List<String> titles = Lists.newArrayList();
                    titles.add("아파트");
                    titles.add("ID");
                    titles.add("AP BEACON UUID");
                    titles.add("AP ID");
                    titles.add("AP 이름");

                    cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                    cell.setCellValue("");
                    sheet.setColumnWidth(currCell, 10000);
                    for (int i = 0; i < titles.size(); i++) {
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleHeader"));
                        cell.setCellValue(titles.get(i));
                    }
                    currCell = 1;

                    int size = timeApList.size();
                    for (int i = 0; i < size; i++) {

                        AptAp item = timeApList.get(i);

                        row = sheet.createRow(currRow++);
                        row.setHeightInPoints(17);

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.aptName));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.id));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.apBeaconUuid));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.apId));

                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                        cell.setCellValue(StringUtil.nvl(item.apName));

                        currCell = 1;
                    }


                    // 금일 출입성공기록이 없는 AP sheet
                    if (dayApList == null || dayApList.isEmpty()) {
                        logger.info("<< AP 금일 출입기록 모니터링 메일발송, 데이터가 없습니다. >>");
                    } else {
                        currRow = 0;
                        currCell = 0;
                        sheet = wb.createSheet("금일 출입성공기록이 없는 AP");

                        sheet.setDefaultColumnWidth(19);

                        row = null;
                        cell = null;

                        row = sheet.createRow(currRow++);// 한칸띄기용 row

                        row = sheet.createRow(currRow++);
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                        cell.setCellValue("");
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                        cell.setCellStyle(cellStyleMap.get("defaultStyleStringValueLeft"));
                        cell.setCellValue("[ " + baseDate1 + " ] 금일 출입성공기록이 없는 AP 입니다. ( 총 " + df.format(dayApList.size()) + "건 )");
                        currCell = 0;

                        row = sheet.createRow(currRow++);// 한칸띄기용 row

                        row = sheet.createRow(currRow++);
                        row.setHeightInPoints(20);

                        // titles는 위에 정의한것과 동일하게 사용
                        cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);// 한칸띄기용 cell
                        cell.setCellValue("");
                        sheet.setColumnWidth(currCell, 10000);
                        for (int i = 0; i < titles.size(); i++) {
                            cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(cellStyleMap.get("styleHeader"));
                            cell.setCellValue(titles.get(i));
                        }
                        currCell = 1;

                        size = dayApList.size();
                        for (int i = 0; i < size; i++) {

                            AptAp item = dayApList.get(i);

                            row = sheet.createRow(currRow++);
                            row.setHeightInPoints(17);

                            cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                            cell.setCellValue(StringUtil.nvl(item.aptName));

                            cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                            cell.setCellValue(StringUtil.nvl(item.id));

                            cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                            cell.setCellValue(StringUtil.nvl(item.apBeaconUuid));

                            cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                            cell.setCellValue(StringUtil.nvl(item.apId));

                            cell = row.createCell(currCell++, Cell.CELL_TYPE_STRING);
                            cell.setCellStyle(cellStyleMap.get("styleStringValue"));
                            cell.setCellValue(StringUtil.nvl(item.apName));

                            currCell = 1;
                        }
                    }


                    // 파일 생성
                    emptyFile = new File(tempPath + File.separator + "출입성공기록이 없는 AP " + baseDate1 + ".xls");
                    if (emptyFile.exists()) {
                        emptyFile.delete();
                    }
                    emptyFile.createNewFile();

                    FileOutputStream fs = null;
                    try {
                        fs = new FileOutputStream(emptyFile);
                        wb.write(fs);
                    } catch (Exception e) {
                        result = -1;
                        e.printStackTrace();
                    } finally {
                        if (fs != null) {
                            fs.close();
                        }
                    }

                    htmlContent.append("<br />");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (timeAptCntList != null & !timeAptCntList.isEmpty()) {

                if (timeAptCntList.size() + dayAptCntList.size() > 15) {
                    // 메일에서 스크롤이 생길정도로 row가 있으면 메일내용 맨아래쪽에도 이마을 이동 링크를 생성한다.
                    htmlContent.append("<br /><a href='http://emaul.co.kr/' >이마을로 이동</a>");
                }

                String msgTitle = "[ 최근출입성공기록없는 AP ] AP Monitoring 경고 데이터가 발생하였습니다.";

                MimeMessage message = mailSender.createMimeMessage();
                try {

                    String sendUser = env.getProperty("spring.mail.username");

                    message.setSubject(msgTitle, "UTF-8");
                    // message.setText(htmlContent.toString(), "UTF-8", "html");
                    message.setFrom(new InternetAddress(sendUser));
                    message.setSentDate(new Date());

                    for (SimpleUser user : userList) {
                        logger.info("<< [ 최근출입성공기록없는 AP ] AP Monitoring Mail 발송대상 : {} {} {} >>", user.id, user.getFullName(), user.getEmail());
                        message.addRecipient(RecipientType.TO, new InternetAddress(user.getEmail()));
                    }

                    MimeBodyPart bodypart = new MimeBodyPart();
                    bodypart.setContent(htmlContent.toString(), "text/html;charset=utf-8");

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(bodypart);

                    if (emptyFile != null) {
                        MimeBodyPart attachPart = new MimeBodyPart();
                        attachPart.setDataHandler(new DataHandler(new FileDataSource(emptyFile)));
                        attachPart.setFileName(MimeUtility.encodeText(emptyFile.getName()));
                        multipart.addBodyPart(attachPart);
                    }

                    message.setContent(multipart);
                    mailSender.send(message);

                    // file을 mail에 첨부한후 삭제한다.
                    if (emptyFile != null && emptyFile.exists()) {
                        emptyFile.delete();
                    }

                } catch (Exception e) {
                    result = -1;
                    e.printStackTrace();
                }

                List<Long> userIds = Lists.newArrayList();
                for (SimpleUser user : userList) {
                    userIds.add(user.id);
                    logger.info("<< [ 최근출입성공기록없는 AP ] AP Monitoring GCM 발송 {} {} >>", user.id, user.getFullName());
                }

                GcmSendForm form = new GcmSendForm();
                Map<String, String> msg = Maps.newHashMap();
                msg.put("type", "action");
                msg.put("title", StringUtil.nvl(msgTitle, ""));
                msg.put("value", "확인바랍니다.");
                form.setUserIds(userIds);
                form.setMessage(msg);

                gcmService.sendGcm(form);

            }

        }

        return result;

    }

    private Map<String, Object> setAptApResultData(AptAp aptAp, String successYn, String message) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("aptAp", aptAp);
        result.put("successYn", successYn);
        result.put("message", message);
        return result;
    }

    /**
     *
     * @param user
     * @param apList 선택된 ap 목록
     * @param storagePeriod 삭제시 제외될 일수
     * @param type schedule & user
     * @return
     */
    private Map<String, Object> aptApHealthCheck(List<AptAp> apList, Integer storagePeriod, String type) {

        Map<String, Object> resultMap = Maps.newHashMap();
        List<Map<String, Object>> successList = Lists.newArrayList();
        List<Map<String, Object>> failList = Lists.newArrayList();

        long s = System.currentTimeMillis();
        logger.info("<< aptApMonitoringHealthCheck start : {} >>" + s);

        int targetCnt = 0;
        if (apList != null) {
            targetCnt = apList.size();
        }

        int timeout = 10;
        RequestConfig httpConfig = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        CloseableHttpAsyncClient httpclient = HttpAsyncClientBuilder.create().setDefaultRequestConfig(httpConfig).setMaxConnTotal(targetCnt).setMaxConnPerRoute(targetCnt).build();

        try {
            httpclient.start();

            CountDownLatch latch = new CountDownLatch(targetCnt);
            String protocol = "http://%s/door/action/healthchk";

            for (int i = 0; i < targetCnt; i++) {

                AptAp aptAp = apList.get(i);

                if (StringUtil.isBlank(aptAp.expIp)) {
                    failList.add(setAptApResultData(aptAp, "N", "ap ip 미설정"));
                    latch.countDown();
                    continue;
                }

                HttpGet httpGet = new HttpGet(String.format(protocol, aptAp.expIp));
                httpclient.execute(httpGet, new FutureCallback<HttpResponse>() {

                    @Override
                    public void completed(final HttpResponse response) {
                        try {

                            int httpStatus = response.getStatusLine().getStatusCode();
                            // logger.info("<<HTTP 응답({})>>", httpStatus);

                            if (HttpStatus.SC_OK == httpStatus) {
                                HttpEntity httpEntity = response.getEntity();
                                String responseJson = EntityUtils.toString(httpEntity);

                                if (!StringUtil.isBlank(responseJson)) {
                                    JSONObject jo = new JSONObject(responseJson);
                                    String successYn = jo.getString("result");
                                    if ("Y".equals(successYn)) {
                                        successList.add(setAptApResultData(aptAp, "Y", "success"));// 성공
                                        // logger.info("<< ap response is success, health check success, id : {}, apBeaconUuid : {} >>", aptAp.id, aptAp.apBeaconUuid);
                                    } else {
                                        // logger.info("<< ap response is empty, health check fail, id : {}, apBeaconUuid : {} >>", aptAp.id, aptAp.apBeaconUuid);
                                        failList.add(setAptApResultData(aptAp, "N", successYn));
                                    }
                                }
                            } else {
                                // logger.error("<<HTTP response 에러>> status : {} ", httpStatus);
                                failList.add(setAptApResultData(aptAp, "N", "error status " + httpStatus));
                            }

                            latch.countDown();
                        } catch (Exception e) {
                            failed(e);
                        }
                    }

                    @Override
                    public void failed(final Exception ex) {
                        latch.countDown();
                        failList.add(setAptApResultData(aptAp, "N", "http client execute failed : " + ex));
                        // logger.error("<< http client execute failed : >> {} ", ex);
                    }

                    @Override
                    public void cancelled() {
                        latch.countDown();
                        failList.add(setAptApResultData(aptAp, "N", "http client execute cancelled"));
                        // logger.error("<< http client execute cancelled >>");
                    }

                });
            }
            logger.info("<< aptApMonitoringHealthCheck async send data. waiting... >>");
            latch.await();
            logger.info("<< aptApMonitoringHealthCheck Receive all responses. >>");


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        long e = System.currentTimeMillis();
        logger.info("<< aptApMonitoringHealthCheck end : {} >>" + e);
        logger.info("<< aptApMonitoringHealthCheck total time : {} >>" + (e - s));

        // check 결과 저장
        Date dbDate = commonService.selectDate();
        List<Map<String, Object>> healthCheckList = Lists.newArrayList();
        healthCheckList.addAll(successList);
        healthCheckList.addAll(failList);
        int resultSize = insertApMonitoringAliveBatch(healthCheckList, dbDate, type);

        // 데이터 유지기간 ( 유지기간 이전 데이터 삭제 )
        int size = healthCheckList.size();
        if (size > 0) {
            Map<String, Object> storagePeriodParam = Maps.newHashMap();
            storagePeriodParam.put("storagePeriod", storagePeriod);
            aptApMapper.deleteApMonitoringAlive(storagePeriodParam);
        }

        resultMap.put("successList", successList);
        resultMap.put("failList", failList);

        // fail 데이터는 mail & gcm 전송
        if (failList != null && failList.size() > 0) {

            // failList는 그대로 두고 아파트이름으로 soting만 재정렬한 list 생성
            List<Map<String, Object>> tempFaieList = Lists.newArrayList(failList);
            tempFaieList.sort((p1, p2) -> {
                AptAp aptAp1 = (AptAp) p1.get("aptAp");
                AptAp aptAp2 = (AptAp) p2.get("aptAp");
                return aptAp1.aptName.compareTo(aptAp2.aptName);
            });

            List<SimpleUser> userList = aptApMapper.selectAptApInspAccountList("MAIL");

            if (userList == null || userList.isEmpty()) {
                logger.info("<< AP Health Check 모니터링 메일발송, 메일보낼 대상이 없어 메일&Gcm 전송부분만 종료함. >>");
            } else {

                StringBuilder htmlContent = new StringBuilder();
                String theadCss = "font-size:10pt;background:#DFE0E0;border-top:2px solid #2185CF;border-bottom:1px solid #E5E5E5;";
                String tbodyCss = "font-size:9pt;text-align:center;border-bottom: 1px solid #E5E5E5;";

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
                Calendar cal = Calendar.getInstance();
                String baseDate = sdf.format(cal.getTime());

                DecimalFormat df = new DecimalFormat("#,##0");

                htmlContent.append("<a href='http://emaul.co.kr/' >이마을로 이동</a>");
                htmlContent.append("<br /><br />");

                // 최근 지정된 시간이내 출입기록성공이력 없는 AP 데이터
                htmlContent.append("<h4><b>[ " + baseDate + " ] AP Health Check Fail 데이터가 발생하였습니다. ( 총 " + df.format(failList.size()) + "개 AP 입니다. ) </b></h4>");

                htmlContent.append("<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; width:1200px; font-size:13px;'>");
                htmlContent.append("<thead>");
                htmlContent.append("<tr style='height:35px;'>");
                htmlContent.append("<th style='width:400px;" + theadCss + "'>아파트</th>");
                htmlContent.append("<th style='width:100px;" + theadCss + "'>ID</th>");
                htmlContent.append("<th style='width:100px;" + theadCss + "'>AP ID</th>");
                htmlContent.append("<th style='width:100px;" + theadCss + "'>AP 이름</th>");
                htmlContent.append("<th style='width:200px;" + theadCss + "'>AP BEACON UUID</th>");
                htmlContent.append("<th style='width:500px;" + theadCss + "'>MESSAGE</th>");
                htmlContent.append("</tr>");
                htmlContent.append("</thead>");

                htmlContent.append("<tbody>");
                for (Map<String, Object> item : tempFaieList) {
                    if (item.get("aptAp") != null) {
                        AptAp aptAp = (AptAp) item.get("aptAp");

                        htmlContent.append("<tr style='height:30px;'>");
                        htmlContent.append("<td style='width:400px;" + tbodyCss + "'>" + StringUtil.nvl(aptAp.aptName) + "</td>");
                        htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + StringUtil.nvl(aptAp.id) + "</td>");
                        htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + StringUtil.nvl(aptAp.apId) + "</td>");
                        htmlContent.append("<td style='width:100px;" + tbodyCss + "'>" + StringUtil.nvl(aptAp.apName) + "</td>");
                        htmlContent.append("<td style='width:200px;" + tbodyCss + "'>" + StringUtil.nvl(aptAp.apBeaconUuid) + "</td>");
                        htmlContent.append("<td style='width:500px;" + tbodyCss + "'>" + StringUtil.nvl(item.get("message")) + "</td>");
                        htmlContent.append("</tr>");
                    }
                }
                htmlContent.append("</tbody>");

                htmlContent.append("</table>");
                htmlContent.append("<br /><br />");


                if (failList.size() > 15) {
                    // 메일에서 스크롤이 생길정도로 row가 있으면 메일내용 맨아래쪽에도 이마을 이동 링크를 생성한다.
                    htmlContent.append("<br /><a href='http://emaul.co.kr/' >이마을로 이동</a>");
                }

                String msgTitle = "[ AP Health Check ] AP Monitoring 경고 데이터가 발생하였습니다.";

                MimeMessage message = mailSender.createMimeMessage();
                try {

                    String sendUser = env.getProperty("spring.mail.username");

                    message.setSubject(msgTitle, "UTF-8");
                    // message.setText(htmlContent.toString(), "UTF-8", "html");
                    message.setFrom(new InternetAddress(sendUser));
                    message.setSentDate(new Date());

                    for (SimpleUser user : userList) {
                        logger.info("<< [ AP Health Check ] AP Monitoring Mail 발송대상 : {} {} {} >>", user.id, user.getFullName(), user.getEmail());
                        message.addRecipient(RecipientType.TO, new InternetAddress(user.getEmail()));
                    }

                    MimeBodyPart bodypart = new MimeBodyPart();
                    bodypart.setContent(htmlContent.toString(), "text/html;charset=utf-8");

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(bodypart);

                    message.setContent(multipart);
                    mailSender.send(message);

                } catch (Exception ee) {
                    ee.printStackTrace();
                }

                List<Long> userIds = Lists.newArrayList();
                for (SimpleUser user : userList) {
                    userIds.add(user.id);
                    logger.info("<< [ AP Health Check ] AP Monitoring GCM 발송 {} {} >>", user.id, user.getFullName());
                }

                GcmSendForm form = new GcmSendForm();
                Map<String, String> msg = Maps.newHashMap();
                msg.put("type", "action");
                msg.put("title", StringUtil.nvl(msgTitle, ""));
                msg.put("value", "확인바랍니다.");
                form.setUserIds(userIds);
                form.setMessage(msg);

                gcmService.sendGcm(form);

            }
        }

        return resultMap;
    }

    /**
     * health check monitoring batch insert
     */
    private int insertApMonitoringAliveBatch(List<Map<String, Object>> healthCheckList, Date dbDate, String type) {

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);

        int result = 0;
        int size = healthCheckList.size();

        try {

            for (int i = 0; i < size; i++) {
                Map<String, Object> item = healthCheckList.get(i);

                AptAp ap = (AptAp) item.get("aptAp");
                String successYn = StringUtil.nvl(item.get("successYn"));
                String message = StringUtil.nvl(item.get("message"));

                AptApMonitoringAlive aptApMonitoringAlive = new AptApMonitoringAlive();
                aptApMonitoringAlive.type = type;
                aptApMonitoringAlive.apId = ap.id;
                aptApMonitoringAlive.expIp = ap.expIp;
                aptApMonitoringAlive.success = successYn;
                aptApMonitoringAlive.regDate = dbDate;
                aptApMonitoringAlive.memo = message;

                // aptApMonitoringAlive.apBeaconUuid = ap.apBeaconUuid;
                // aptApMonitoringAlive.aptApId = ap.apId;
                // aptApMonitoringAlive.apName = ap.apName;

                result += sqlSession.insert("com.jaha.server.emaul.mapper.AptApMapper.insertApMonitoringAlive", aptApMonitoringAlive);

                // result += aptApMapper.insertApMonitoringAlive(aptApMonitoringAlive);
            }

            sqlSession.commit();

        } catch (Exception e) {
            sqlSession.rollback();
            e.printStackTrace();
        } finally {
            sqlSession.close();
        }

        return result;
    }

    @Override
    public Map<String, Object> aptApMonitoringHealthCheck(List<AptAp> apList, Integer storagePeriod) {
        return aptApHealthCheck(apList, storagePeriod, "schedule");
    }


    /**
     * 이도어 사용자 모니터링 배치용 리스트
     */
    @Override
    public List<Map<String, Object>> selectAptApUserMonitoringBatch() {
        Map<String, Object> params = new HashMap<String, Object>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = commonService.selectDate();
        params.put("sDate", sdf.format(date));
        params.put("type", "EXCEPT_MONITORING");
        Map<String, Object> item = aptApMapper.selectExceptApt(params);
        List<String> itemList = new ArrayList<String>();
        String tmp = (String) item.get("data_1");
        String[] tmpList = tmp.split(",");
        for (String trimStr : tmpList) {
            itemList.add(trimStr.trim());
        }
        params.put("exceptAptList", itemList);

        List<Map<String, Object>> result = aptApMapper.selectAptApUserMonitoringListBatch(params);

        saveAptApUserMonitroing(result);
        return result;
    }

    /**
     * 이도어 사용자 모니터링 데이터 insert
     */
    @Override
    public int saveAptApUserMonitroing(List<Map<String, Object>> params) {
        int result = 0;
        if (params.size() > 0) {
            for (int i = 0; i < params.size(); i++) {
                Map<String, Object> item = params.get(i);
                item.put("totalUser", aptApMapper.selectAptApMonitoringTotalUser(item));
                result += aptApMapper.insertAptApUserMonitroing(item);
            }
        }
        return result;
    }


    /**
     * ##############################################################################################################################################################################
     * #################################################################### 2017-02-01 이후 e-door api 개발 버전 ####################################################################
     * ##############################################################################################################################################################################
     */

    /**
     * Created by shavrani on 17-02-02
     */
    @Override
    public String apOpenRequest(User user, Map<String, Object> params) {

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String mobileDeviceModel = StringUtil.nvl(params.get("mobileDeviceModel"));
        String mobileDeviceOs = StringUtil.nvl(params.get("mobileDeviceOs"));
        String inOut = StringUtil.nvl(params.get("inOut"));

        Date dbDate = aptApMapper.selectDate();// database server 시간

        AptApAccessLog aptApAccessLog = new AptApAccessLog();
        aptApAccessLog.userId = user.id;
        aptApAccessLog.mobileDeviceModel = mobileDeviceModel;
        aptApAccessLog.mobileDeviceOs = mobileDeviceOs;
        aptApAccessLog.accessDate = dbDate;// database server 시간으로 설정
        aptApAccessLog.inOut = inOut;
        aptApAccessLog.appVersion = user.appVersion;
        aptApAccessLog.openType = Code.APP_AP_OPEN_TYPE_SERVER.getCode();

        String result = "N";

        // user의 현재상태확인
        if (user.type.deactivated || user.type.blocked || user.type.anonymous) {
            aptApAccessLog.success = "N";
            aptApAccessLog.memo = "유저의 현재 user.type e-door 이용불가 !! [ user id : " + user.id + ", name : " + user.getFullName() + " ] [ apBeaconUuid : " + apBeaconUuid + " ]";
            logger.info("<< " + aptApAccessLog.memo + " >>");
        } else {

            // edoor 알림 설정을 활성화해야만 서비스 이용가능처리.
            if (user.setting.notiEdoor) {

                params.put("_active", true);
                AptAp aptAp = selectAptAp(params);
                // ap가 존재하는지 확인
                if (aptAp == null) {
                    aptApAccessLog.success = "N";
                    aptApAccessLog.memo = "존재하지않는 AP]";
                    logger.info(" # " + aptApAccessLog.memo);
                } else {

                    aptApAccessLog.apId = StringUtil.nvl(aptAp.id);
                    aptApAccessLog.expIp = StringUtil.nvl(aptAp.expIp);

                    params.clear();
                    params.put("id", aptAp.id);
                    params.put("aptId", user.house.apt.id);
                    params.put("dong", user.house.dong);
                    params.put("ho", user.house.ho);
                    params.put("userId", user.id);
                    params.put("_active", true);

                    List<String> skipAuths = getSkipAuthUser(user);
                    if (skipAuths.size() > 0) {
                        params.put("skipAuths", skipAuths);
                    }

                    AptAp aptApAuth = aptApMapper.selectAptApAccess(params);

                    // ap에 출입권한 확인
                    if (aptApAuth == null) {
                        aptApAccessLog.success = "N";
                        aptApAccessLog.memo = "출입권한없음, [ user id : " + user.id + " ]";
                        logger.info(" # " + aptApAccessLog.memo);
                    } else {
                        String aptApId = StringUtil.nvl(aptAp.id);
                        List<AptApAccessLog> existList = aptApAccessLogRepository.findByApIdAndUserIdOrderByAccessDateDesc(aptApId, user.id);
                        // 연속 호출을 막기위한 코드
                        if (existList != null && existList.size() > 0) {
                            AptApAccessLog existLog = existList.get(0);// 0번째가 가장 최근의 기록
                            Date existLogDate = existLog.accessDate;
                            if (existLogDate != null) {
                                Long gap = dbDate.getTime() - existLogDate.getTime();
                                gap = gap / 1000;
                                if (gap <= 3) {
                                    // 임시 방편 코드 ( ios 예전 앱의 버전에서 연속 대량 호출이 발생하여 3초내에 요청이 있을경우는 skip 처리함. )
                                    return result;
                                }
                            }
                        }

                        aptApAccessLog.delayTime = dbDate.getTime();// database server 시간으로 millisecond 설정
                        aptApAccessLog.waitingYn = "Y";// 오픈성공하면 ap에서 성공 api를 호출할때 waitingYn 플래그를 삭제처리.
                        result = restFulUtil.edoorOpenRequest(user.id, aptAp.expIp);// ap에 open 요청

                        if ("N".equals(result)) {
                            aptApAccessLog.waitingYn = null;
                            aptApAccessLog.success = "N";
                            aptApAccessLog.memo = "AP connection fail";
                            logger.info(" # {} [ip : {}]", aptApAccessLog.memo, aptAp.expIp);
                        }

                        // AP 상태가 고장이면 고장표기 return ( 고장이어도 ap에 open 시도는 해본후에 고장 상태를 return )
                        if ("2".equals(StringUtil.nvl(aptAp.status))) {
                            aptApAccessLog.memo = StringUtil.nvl(aptApAccessLog.memo) + " [ AP 상태 : 고장 ]";
                            result = "X";
                        }
                    }
                }

            } else {
                aptApAccessLog.success = "N";
                aptApAccessLog.memo = "유저의 알림 미수신 설정으로 e-door 이용불가 !! [ user id : " + user.id + " , apBeaconUuid : " + apBeaconUuid + " ]";
                logger.info("<< " + aptApAccessLog.memo + " >>");
            }
        }

        saveAptApAccessLog(aptApAccessLog);

        return result;
    }

    @Override
    public String saveApOpenResult(User user, Map<String, Object> params) {

        String result = "N";

        String data = StringUtil.nvl(params.get("data"));

        if (StringUtil.isBlank(data)) {
            return "N";
        } else {

            JSONObject jo = new JSONObject(data);

            String apBeaconUuid = StringUtil.nvl(jo.optString("apBeaconUuid"));

            params.clear();
            params.put("apBeaconUuid", apBeaconUuid);
            params.put("_active", true);
            AptAp aptAp = selectAptAp(params);

            if (aptAp == null) {
                logger.info("<< 앱의 e-door open result save : " + "ap beacon uuid [ {} ] 로 검색된 AP가 없어서 저장실패  >>", apBeaconUuid);
            } else {

                Date dbDate = aptApMapper.selectDate();// database server 시간

                AptApAccessLog aaaLog = new AptApAccessLog();
                aaaLog.apId = StringUtil.nvl(aptAp.id);
                aaaLog.userId = user.id;
                aaaLog.accessDate = dbDate;
                aaaLog.mobileDeviceModel = StringUtil.nvl(jo.optString("mobileDeviceModel"));
                aaaLog.mobileDeviceOs = StringUtil.nvl(jo.optString("mobileDeviceOs"));
                aaaLog.appVersion = user.appVersion;
                aaaLog.openType = StringUtil.nvl(jo.optString("openType"));// 코드그룹 AP_OPERATION_MODE
                aaaLog.delayTime = StringUtil.nvlLong(jo.optString("delayTime"), 0);
                aaaLog.success = "Y";
                aaaLog.expIp = aptAp.expIp;
                // aaaLog.inOut = StringUtil.nvl(jo.optString("inOut"));
                aaaLog.memo = StringUtil.nvl(jo.optString("memo"));

                aptApMapper.insertAptApAccessLog(aaaLog);
                sendAdvertGcm(aptAp, user, null);

                result = "Y";

                logger.info("<< " + "userId [ {} ] name [ {} ] apBeaconUuid [ {} ] e-door open result save success >>", user.id, user.getFullName(), apBeaconUuid);

            }

        }

        return result;
    }

    @Override
    public String batchSaveApOpenResult(User user, Map<String, Object> params) {

        String data = StringUtil.nvl(params.get("data"));

        String result = "N";

        if (StringUtil.isBlank(data)) {
            return "N";
        } else {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);

            JSONArray ja = new JSONArray(data);

            int count = 0;
            int size = ja.length();

            try {

                Date dbDate = aptApMapper.selectDate();// database server 시간

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                for (int i = 0; i < size; i++) {

                    JSONObject jo = ja.getJSONObject(i);

                    AptApAccessLog aaaLog = new AptApAccessLog();
                    aaaLog.apId = StringUtil.nvl(jo.optString("id"));
                    aaaLog.userId = user.id;
                    aaaLog.accessDate = sdf.parse(StringUtil.nvl(jo.optString("accessDate")));
                    aaaLog.mobileDeviceModel = StringUtil.nvl(jo.optString("mobileDeviceModel"));
                    aaaLog.mobileDeviceOs = StringUtil.nvl(jo.optString("mobileDeviceOs"));
                    aaaLog.appVersion = StringUtil.nvl(jo.optString("appVersion"));
                    aaaLog.openType = StringUtil.nvl(jo.optString("openType"));// 코드그룹 AP_OPERATION_MODE
                    aaaLog.delayTime = StringUtil.nvlLong(jo.optString("delayTime"), 0);
                    aaaLog.success = "Y";
                    // aaaLog.expIp = null;// BLE모드로는 ap의 ip를 저장할 필요가 없을듯..
                    // aaaLog.inOut = StringUtil.nvl(jo.optString("inOut"));
                    aaaLog.memo = "앱의 과거 출입기록 일괄 저장 ( 저장일시 : " + sdf.format(dbDate) + " )";

                    count += sqlSession.insert("com.jaha.server.emaul.mapper.AptApMapper.insertAptApAccessLog", aaaLog);
                }

                sqlSession.commit();

                result = "Y";

            } catch (Exception e) {
                sqlSession.rollback();
                e.printStackTrace();
            } finally {
                sqlSession.close();
            }
        }

        return result;
    }

    @Override
    public String saveApBrokenLog(User user, Map<String, Object> params) {

        String result = "N";

        String apBeaconUuid = StringUtil.nvl(params.get("apBeaconUuid"));
        String memo = StringUtil.nvl(params.get("memo"));

        params.put("_active", true);
        AptAp aptAp = selectAptAp(params);

        if (aptAp == null) {
            logger.info("ap beacon uuid [ " + apBeaconUuid + " ] 가 존재하지않음");
        } else {
            AptApBrokenLog aptApBrokenLog = new AptApBrokenLog();
            aptApBrokenLog.setApId(aptAp.id);
            aptApBrokenLog.setExpIp(aptAp.expIp);
            aptApBrokenLog.setModem(aptAp.modem);
            aptApBrokenLog.setFirmwareVersion(aptAp.firmwareVersion);
            aptApBrokenLog.setAppVersion(user.appVersion);
            aptApBrokenLog.setRegUser(user.id);
            aptApBrokenLog.setMemo(memo);

            int saveResult = aptApMapper.insertApBrokenLog(aptApBrokenLog);
            if (saveResult > 0) {
                result = "Y";
            }
        }

        return result;

    }
}
