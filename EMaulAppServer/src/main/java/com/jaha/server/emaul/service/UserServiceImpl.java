package com.jaha.server.emaul.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.jaha.server.emaul.constants.Constants;
import com.jaha.server.emaul.mapper.AddressMapper;
import com.jaha.server.emaul.mapper.BoardMapper;
import com.jaha.server.emaul.mapper.HouseMapper;
import com.jaha.server.emaul.mapper.UserMapper;
import com.jaha.server.emaul.model.Address;
import com.jaha.server.emaul.model.Apt;
import com.jaha.server.emaul.model.BaseSecuModel;
import com.jaha.server.emaul.model.BoardCategory;
import com.jaha.server.emaul.model.BoardCategory.UserPrivacy;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.Setting;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.UserLoginLog;
import com.jaha.server.emaul.model.UserNickname;
import com.jaha.server.emaul.model.UserType;
import com.jaha.server.emaul.repo.AddressRepository;
import com.jaha.server.emaul.repo.AptRepository;
import com.jaha.server.emaul.repo.BoardCategoryRepository;
import com.jaha.server.emaul.repo.HouseRepository;
import com.jaha.server.emaul.repo.SettingRepository;
import com.jaha.server.emaul.repo.UserNicknameRepository;
import com.jaha.server.emaul.repo.UserPrepassRepository;
import com.jaha.server.emaul.repo.UserRepository;
import com.jaha.server.emaul.util.AddressConverter;
import com.jaha.server.emaul.util.Locations;
import com.jaha.server.emaul.util.PasswordHash;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.v2.model.user.UserUpdateHistoryVo;

/**
 * Created by doring on 15. 3. 9..
 */
@Service
public class UserServiceImpl extends BaseSecuModel implements UserService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserNicknameRepository userNicknameRepository;
    @Autowired
    private AptRepository aptRepository;
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private BoardCategoryRepository boardCategoryRepository;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private UserPrepassRepository userPrepassRepository;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BoardMapper boardMapper;
    @Autowired
    private Environment env;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Gson gson = new Gson();

    @Override
    public User createUser(HttpServletRequest req, String uid, String addressCode, String dong, String ho, String birthYear, String gender, String email, String name, String password,
            String phoneNumber, Long recommId) throws Exception {

        User user = userRepository.findOneByEmail(encString(email));

        if (user == null) {
            user = new User();
        } else {
            if (!user.type.deactivated) {
                return null;
            }
        }

        user.setEmail(email);

        try {
            user.passwordHash = PasswordHash.createHash(password);
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
        user.deactiveDate = null;
        user.setFullName(name);
        user.hasProfileImage = false;
        user.setPhone(phoneNumber);
        user.regDate = new Date();
        user.uniqueDeviceId = uid;
        user.birthYear = birthYear;
        user.gender = gender;

        user.house = selectOrCreateHouse(addressCode, dong, ho);

        // ////////////////////////////////////////////////////////////////////// 닉네임 등록 ////////////////////////////////////////////////////////////////////////
        // 같은 이름을 가진 사용자 목록
        List<User> userList = userRepository.findByFullName(encString(name));
        String nickname = null;
        int coun = 0;
        if (userList != null && userList.size() > 0) { // 이름이 존재한다.
            coun = userList.size() + 1;
            nickname = name + coun;
        } else { // 이름이 존재하지 않는다.
            nickname = name;
        }

        // 닉네임이 존재하는지 확인한다.
        UserNickname checkNick = this.userNicknameRepository.findOne(nickname);
        if (checkNick != null) {
            coun++;
            nickname = name + coun;
        }

        UserNickname nick = new UserNickname();
        nick.name = nickname;
        user.setNickname(nick);
        // ////////////////////////////////////////////////////////////////////// 닉네임 등록 ////////////////////////////////////////////////////////////////////////

        user = userRepository.saveAndFlush(user);

        user.setting = new Setting(user.id);
        user.type = new UserType(user.id);

        // 추천인 ID
        if (recommId > 0) {
            user.recommId = recommId;
        }

        // 유저 자동 인증
        try {
            Boolean isPrepass = isPrepassUser(user);
            if (isPrepass != null && isPrepass) {
                user.type.user = true;
                user.type.houseHost = true;
            }
        } catch (Exception e) {
            // do nothing
        }

        user = userRepository.saveAndFlush(user);

        SessionAttrs.setUserId(req.getSession(), user.id);

        return convertToPrivateUser(user);
    }

    /**
     * @author shavrani 2016-10-18
     */
    @Override
    public User createUser(HttpServletRequest req, Map<String, Object> params) throws Exception {

        String uid = StringUtil.nvl(params.get("uid"));
        String addressCode = StringUtil.nvl(params.get("addressCode"));
        String dong = StringUtil.nvl(params.get("dong"));
        String ho = StringUtil.nvl(params.get("ho"));
        String birthYear = StringUtil.nvl(params.get("birthYear"));
        String gender = StringUtil.nvl(params.get("gender"));
        String email = StringUtils.trimToEmpty(StringUtil.nvl(params.get("email")));
        String name = StringUtils.trimToEmpty(StringUtil.nvl(params.get("name")));
        String password = StringUtil.nvl(params.get("password"));
        String phoneAuthCode = StringUtil.nvl(params.get("phoneAuthCode"));
        String phoneAuthKey = StringUtil.nvl(params.get("phoneAuthKey"));
        String recommNickName = StringUtil.nvl(params.get("recommNickName"));
        String phoneNumber = StringUtil.nvl(params.get("phoneNumber"));
        Long recommId = StringUtil.nvlLong(params.get("recommId"));

        // addressCode가 없을시 받는 parameter
        String sidoNm = StringUtil.nvl(params.get("sidoNm"));
        String sggNm = StringUtil.nvl(params.get("sggNm"));
        String emdNm = StringUtil.nvl(params.get("emdNm"));
        String addressDetail = StringUtil.nvl(params.get("addressDetail"));

        User user = userRepository.findOneByEmail(encString(email));

        if (user == null) {
            user = new User();
        } else {
            return null;
        }

        user.setEmail(email);

        try {
            user.passwordHash = PasswordHash.createHash(password);
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
        user.deactiveDate = null;
        user.setFullName(name);
        user.hasProfileImage = false;
        user.setPhone(phoneNumber);
        user.regDate = new Date();
        user.uniqueDeviceId = uid;
        user.birthYear = birthYear;
        user.gender = gender;

        if (StringUtil.isBlank(addressCode)) {
            // addressCode가 없으면 동단위의 address 생성후 아파트 생성
            user.house = selectOrCreateAddressAndHouse(sidoNm, sggNm, emdNm);
            user.setAddressDetail(addressDetail);
        } else {
            // addressCode가 있으면 조회하여 house 생성
            user.house = selectOrCreateHouse(addressCode, dong, ho);
        }

        // ////////////////////////////////////////////////////////////////////// 닉네임 등록 ////////////////////////////////////////////////////////////////////////
        // 같은 이름을 가진 사용자 목록
        List<User> userList = userRepository.findByFullName(encString(name));
        String nickname = null;
        int coun = 0;
        if (userList != null && userList.size() > 0) { // 이름이 존재한다.
            coun = userList.size() + 1;
            nickname = name + coun;
        } else { // 이름이 존재하지 않는다.
            nickname = name;
        }

        // 닉네임들이 존재하는지 확인한다.
        List<UserNickname> checkNickList = this.userNicknameRepository.findByNameStartingWith(name);
        if (checkNickList != null && checkNickList.size() > 0) {
            coun = checkNickList.size() + 1;
            nickname = name + coun;
        } else { // 닉네임이 존재하지 않는다.
            nickname = name;
        }

        // 닉네임이 존재하는지 확인한다.
        UserNickname checkNick = this.userNicknameRepository.findOne(nickname);
        if (checkNick != null) {
            coun++;
            nickname = name + " " + coun; // 방어코드
        }

        UserNickname nick = new UserNickname();
        nick.name = nickname;
        user.setNickname(nick);
        // ////////////////////////////////////////////////////////////////////// 닉네임 등록 ////////////////////////////////////////////////////////////////////////

        user = userRepository.saveAndFlush(user);

        user.setting = new Setting(user.id);

        //
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

        // 추천인 ID
        if (recommId > 0) {
            user.recommId = recommId;
        }

        // 유저 자동 인증
        try {
            Boolean isPrepass = isPrepassUser(user);
            if (isPrepass != null && isPrepass) {
                user.type.user = true;
                user.type.houseHost = true;
            }
        } catch (Exception e) {
            // do nothing
        }

        user = userRepository.saveAndFlush(user);

        SessionAttrs.setUserId(req.getSession(), user.id);

        return convertToPrivateUser(user);
    }

    @Override
    public Boolean isPrepassUser(User user) {
        return userPrepassRepository.findOneByFullNameAndPhoneAndAptIdAndDongAndHo(user.getFullName(), user.getPhone(), user.house.apt.id, user.house.dong, user.house.ho) != null;
    }

    @Override
    public House selectOrCreateHouse(String addressCode, String dong, String ho) {
        House house = null;

        Long aptId = jdbcTemplate.query("SELECT id FROM apt WHERE address_code=?", new Object[] {addressCode}, rs -> rs.first() ? rs.getLong("id") : null);

        if (aptId == null) {
            Address address = addressRepository.findOne(addressCode);
            Apt apt = new Apt();
            apt.name = address.시군구용건물명;
            apt.address = address;
            apt.registeredApt = false;
            apt.virtual = false;
            Locations.LatLng latLng = Locations.getLocationFromAddress(AddressConverter.toStringAddressOld(address));
            if (latLng != null) {
                apt.latitude = latLng.lat;
                apt.longitude = latLng.lng;
            }
            apt = aptRepository.saveAndFlush(apt);
            logger.info("[아파트정보생성] 아이디: {}, 아파트명: {}", apt.id, apt.name);

            house = new House();
            house.apt = apt;
            house.dong = dong;
            house.ho = ho;

            house = houseRepository.saveAndFlush(house);

            saveBoardCategory(apt, "오늘", 1, "today", Lists.newArrayList("jaha", "admin", "user", "gasChecker", "anonymous"), Lists.newArrayList("jaha", "admin"));
            saveBoardCategory(apt, "우리이야기", 2, "community", Lists.newArrayList("jaha", "admin", "user", "gasChecker"), Lists.newArrayList("jaha", "admin", "user", "gasChecker"));
            saveBoardCategory(apt, "생활정보", 3, "community", Lists.newArrayList("jaha", "admin", "user", "gasChecker"), Lists.newArrayList("jaha", "admin", "user", "gasChecker"));

            // saveBoardCategory(apt, "공지사항", 2, "notice", Lists.newArrayList("jaha", "admin", "user", "gasChecker"), Lists.newArrayList("jaha", "admin"));
            // saveBoardCategory(apt, "방송 게시판", 3, "tts", Lists.newArrayList("jaha", "admin", "user"), Lists.newArrayList("jaha", "admin"));
            // saveBoardCategory(apt, "민원", 4, "complaint", Lists.newArrayList("jaha", "admin", "user"), Lists.newArrayList("jaha", "admin", "user"));

            logger.info("[하우스정보생성-1] 아이디: {}, 동: {}, 호: {}", house.id, house.dong, house.ho);
        } else {

            try {
                house = houseRepository.findOneByAptIdAndDongAndHo(aptId, dong, ho);
            } catch (Exception e) {
                logger.info("<<하우스가 복수, 하우스 목록 조회, 아파트아이디: {}, 동: {}, 호: {}>>", aptId, dong, ho);

                List<House> houseList = this.houseRepository.findByAptIdAndDongAndHo(aptId, dong, ho);
                if (houseList != null && houseList.size() > 0) {
                    house = houseList.get(0);
                }
            }

            if (house == null) {
                house = new House();
                house.apt = aptRepository.findOne(aptId);
                house.dong = dong;
                house.ho = ho;

                house = houseRepository.saveAndFlush(house);

                logger.info("[하우스정보생성-2] 아이디: {}, 동: {}, 호: {}", house.id, house.dong, house.ho);
            }
        }

        return house;
    }

    @Override
    public House selectOrCreateAddressAndHouse(String sidoNm, String sggNm, String emdNm) {
        House house = null;

        Map<String, Object> params = Maps.newHashMap();
        params.put("sidoNm", sidoNm);
        params.put("sggNm", sggNm);
        params.put("emdNm", emdNm);
        Address virtualAddress = addressMapper.selectVirtualAddress(params);

        if (virtualAddress == null) {
            String addressCode = addressMapper.createVirtualAddressBuildingNo();
            params.put("addressCode", addressCode);
            params.put("buildingNm", emdNm + " 커뮤니티");
            addressMapper.insertVirtualAddress(params);
            virtualAddress = addressMapper.selectVirtualAddress(params);
        }

        params.put("addressCode", virtualAddress.건물관리번호);
        Apt apt = houseMapper.selectApt(params);

        if (apt == null) {

            apt = new Apt();
            apt.name = virtualAddress.시군구용건물명;
            apt.address = virtualAddress;
            apt.registeredApt = false;
            apt.virtual = true;// 가상아파트 flag 설정
            Locations.LatLng latLng = Locations.getLocationFromAddress(AddressConverter.toStringAddressOld(virtualAddress));
            if (latLng != null) {
                apt.latitude = latLng.lat;
                apt.longitude = latLng.lng;
            }
            houseMapper.insertApt(apt);
            logger.info("[아파트정보생성] 아이디: {}, 아파트명: {}", apt.id, apt.name);

            house = new House();
            house.apt = apt;
            house.dong = "0";
            house.ho = "0";

            houseMapper.insertHouse(house);

            createBoardCategory(apt, "오늘", 1, "today", Lists.newArrayList("jaha", "admin", "user", "gasChecker", "anonymous"), Lists.newArrayList("jaha", "admin"));
            createBoardCategory(apt, "공지사항", 2, "notice", Lists.newArrayList("jaha", "admin", "user", "gasChecker"), Lists.newArrayList("jaha", "admin"));
            createBoardCategory(apt, "우리이야기", 3, "community", Lists.newArrayList("jaha", "admin", "user", "gasChecker"), Lists.newArrayList("jaha", "admin", "user", "gasChecker"));
            createBoardCategory(apt, "생활정보", 4, "community", Lists.newArrayList("jaha", "admin", "user", "gasChecker"), Lists.newArrayList("jaha", "admin", "user", "gasChecker"));

            logger.info("[하우스정보생성-1] 아이디: {}, 동: {}, 호: {}", house.id, house.dong, house.ho);

        } else {

            params.clear();
            params.put("aptId", apt.id);
            params.put("dong", "0");
            params.put("ho", "0");
            house = houseMapper.selectHouse(params);

            if (house == null) {
                house = new House();
                house.apt = apt;
                house.dong = "0";
                house.ho = "0";

                houseMapper.insertHouse(house);

                logger.info("[하우스정보생성-2] 아이디: {}, 동: {}, 호: {}", house.id, house.dong, house.ho);
            }

            apt.address = virtualAddress;
            house.apt = apt;


        }

        return house;
    }

    private void saveBoardCategory(Apt apt, String name, int ord, String type, List<String> readable, List<String> writable) {
        BoardCategory boardCategory = new BoardCategory();
        boardCategory.apt = apt;
        boardCategory.name = name;
        boardCategory.ord = ord;
        boardCategory.type = type;
        boardCategory.contentMode = ("today".equals(type)) ? "html" : "text";
        boardCategory.pushAfterWrite = "N";
        boardCategory.jsonArrayReadableUserType = gson.toJson(readable);
        boardCategory.jsonArrayWritableUserType = gson.toJson(writable);
        boardCategory.setUserPrivacy(UserPrivacy.ALIAS);
        boardCategoryRepository.save(boardCategory);
    }

    /**
     * @author shavrani 2016-10-25
     * @desc board category 생성 ( saveBoardCategory의 mybatis 버전 )
     */
    private void createBoardCategory(Apt apt, String name, int ord, String type, List<String> readable, List<String> writable) {
        BoardCategory boardCategory = new BoardCategory();
        boardCategory.apt = apt;
        boardCategory.name = name;
        boardCategory.ord = ord;
        boardCategory.type = type;
        boardCategory.contentMode = ("today".equals(type)) ? "html" : "text";
        boardCategory.pushAfterWrite = "N";
        boardCategory.jsonArrayReadableUserType = gson.toJson(readable);
        boardCategory.jsonArrayWritableUserType = gson.toJson(writable);
        boardCategory.setUserPrivacy(UserPrivacy.ALIAS);
        boardMapper.insertBoardCategory(boardCategory);
    }

    // @Override
    // @Transactional
    // public User login(HttpServletRequest req, String email, String password) {
    // /*
    // * HttpSession session = req.getSession();
    // *
    // * User user = userRepository.findOneByEmail(encString(email)); if (user == null || user.type.deactivated) { session.invalidate(); return null; }
    // *
    // * try { if (PasswordHash.validatePassword(password, user.passwordHash)) { //기본 접속 경로를 안드로이드로 설정 String kind = "android"; SessionAttrs.setUserId(session, user.id);
    // * SessionAttrs.setKind(session, kind); //기본 접속 경로를 안드로이드로 설정 if(!user.kind.equals(kind)){ user.kind = kind; userRepository.saveAndFlush(user); } return convertToPrivateUser(user); } } catch
    // * (Exception e) { logger.error("", e); }
    // *
    // * session.invalidate(); return null;
    // */
    // String kind = "android";
    // return login(req, email, password, kind);
    // }

    // 접속경로 추가
    @Override
    @Transactional
    // public User login(HttpServletRequest req, String email, String password, String kind) {
    public User login(HttpServletRequest req, String email, String password, String kind, String gcmId, String appVersion, String deviceId, String osName, String osVersion, String maker, String model) {

        // login(req, email, password) 병합용
        if (StringUtils.isEmpty(kind)) {
            kind = "android";
        }

        HttpSession session = req.getSession();

        User user = userRepository.findOneByEmail(encString(email));
        if (user == null || user.type.deactivated) {
            session.invalidate();
            return null;
        }

        try {
            if (PasswordHash.validatePassword(password, user.passwordHash)) {

                // logger.info(">>> login 외부기기 로그인 허용여부 : " + user.multiLoginYn + " / userId : " + user.id + " / gcmId : " + gcmId + " / appVersion : " + appVersion + " / deviceId : " + deviceId
                // + " / osName : " + osName + "/ osVersion : " + osVersion + " / maker : " + maker + " / model : " + model);

                // 로직 수정 : 2016..12.06
                // 1. 중복 로그인 허용여부 변경시 해당 기기를 제외한 기기에 로그아웃 푸시 발송
                // 2. 로그인시 : 로그인 시 중복기기 비 허용설정상태에서는 로그인 하는 기기 외의 기기에
                // 로그아웃 푸시 발송
                // 즉 최종 로그인 기기를 제외한 기존 로그인 기기를 로그아웃 하여 1개의 기기만 유지한다.
                if (StringUtils.isNotEmpty(deviceId)) {
                    // logger.info(">>> login 사용자 uniqueDeviceId 변경 : user.uniqueDeviceId : " + user.uniqueDeviceId + " / deviceId : " + deviceId);
                    this.updateUserUniqueDeviceId(user.id, deviceId);
                    user.uniqueDeviceId = deviceId;
                }

                // if (StringUtils.isNotEmpty(gcmId)) {
                //
                // logger.info(">>> login 사용자 gcmId 변경 : user.gcmId : " + user.gcmId + "/ gcmId : " + gcmId);
                // this.updateUserGcmId(user.id, gcmId);
                // user.gcmId = gcmId;
                //
                // try {
                //
                // if (!"Y".equalsIgnoreCase(user.multiLoginYn)) {
                // // 기존 접속의 GCM_ID에 로그아웃 푸시를 발송한다.
                // Sender sender = new Sender(env.getProperty("multilogin.push.key"));
                // Message.Builder Builders = new Message.Builder();
                // Builders.addData("push_check_ids", "1");
                // Builders.addData("push_type", "function-execute");
                // Builders.addData("type", "function-execute");
                // Builders.addData("function", "[\"logout\"]");
                // Builders.addData("userId", user.id.toString());
                // Builders.addData("title", "외부기기 로그아웃");
                // Builders.addData("value", "외부기기 사용으로 로그아웃 합니다.");
                //
                // Message message = Builders.build();
                // Map<String, Object> map = new HashMap<String, Object>();
                // map.put("userId", user.id);
                // map.put("gcmId", gcmId);
                // List<String> list = userMapper.selectUserGcmHistory(map); // new ArrayList<String>();
                //
                // for (String a : list) {
                // logger.info(">>> login 로그아웃 푸시발송 gcm : " + a);
                // }
                //
                // if (!(list == null || list.isEmpty())) {
                // MulticastResult multiResult;
                //
                // multiResult = sender.send(message, list, 5);
                // if (multiResult != null) {
                // List<Result> resultList = multiResult.getResults();
                // for (Result result : resultList) {
                // logger.info(">>> login push result : " + result.getMessageId());
                // }
                // }
                // }
                // }
                // } catch (IOException e) {
                // logger.error(">>> login 외부기기 로그아웃 푸시발송중 오류", e);
                // }
                // }

                SessionAttrs.setUserId(session, user.id);
                SessionAttrs.setKind(session, kind);

                // logger.info("* 사용자아이디[{}], 최종 로그인 시간 수정", user.id);

                String ip = req.getHeader("X-FORWARDED-FOR");
                if (ip == null) {
                    ip = req.getRemoteAddr();
                }
                user.remoteIp = ip;
                user.appVersion = StringUtil.nvl(appVersion, "");
                user.gcmId = StringUtil.nvl(gcmId, "");

                if (user.kind == null || !user.kind.equals(kind)) {
                    user.kind = kind;
                    user.osName = osName;
                    user.osVersion = osVersion;
                    user.maker = maker;
                    user.model = model;
                    user.lastLoginDate = new Date();
                    user = userRepository.saveAndFlush(user);
                } else {
                    this.modifyLastLoginDateAndRemoteIpAndAppVersion(user.id, ip, appVersion, osName, osVersion, maker, model);
                }

                // 아파트 관리소 번호가 없는 경우
                try {
                    if (StringUtils.isBlank(user.house.apt.aptOfficePhoneNumberInner)) {
                        user.house.apt.aptOfficePhoneNumberInner = StringUtils.replace(user.house.apt.aptInfo.aptOfficePhoneNumber, "-", StringUtils.EMPTY);
                    }
                } catch (Exception e) {
                    // 관리소 번호가 없으면 그냥 안함
                    // logger.info("<<아파트 관리소 번호가 없는 경우 오류, {}>>", user.id);
                }

                // 로그인 히스토리 start
                try {
                    // 별도 Repository 를 생성하지 않는다. (1개 서비스, 1개 테이블에서만 사용)
                    List<Long> historys = this.selectUserLoginHistory(user.id, user.remoteIp, user.kind, gcmId, appVersion, osName, osVersion, maker, model);
                    if (historys == null || historys.isEmpty()) {
                        this.insertUserLoginHistory(user.id, user.remoteIp, user.kind, gcmId, appVersion, osName, osVersion, maker, model);
                    } else {
                        this.updateUserLoginHistory(user.id, user.remoteIp, user.kind, gcmId, appVersion, osName, osVersion, maker, model);
                    }

                } catch (Exception e) {
                    // 히스토리 적재시 Exception 은 로그인에 영향을 주지 않아야 한다.
                    logger.error(">>> login() Exception (로그인 히스토리 적재) : " + e.getMessage());
                }
                // 로그인 히스토리 end


                return convertToPrivateUser(user);
            }
        } catch (

        Exception e) {
            logger.error("<<로그인 중 오류 발생>>", e);
        }

        session.invalidate();
        return null;
    }

    /**
     * 사용자 로그인 히스토리 적재
     */
    private void insertUserLoginHistory(Long userId, String remoteIp, String kind, String gcmId, String appVersion, String osName, String osVersion, String maker, String model) throws Exception {
        String sql = "INSERT INTO user_login_history (user_id,remote_ip,kind,gcm_id,app_version, os_name, os_version, maker, model, login_date) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, now())";
        this.jdbcTemplate.update(sql, userId, remoteIp, kind, gcmId, appVersion, osName, osVersion, maker, model);
    }

    /**
     * 사용자 로그인 히스토리 수정
     */
    private void updateUserLoginHistory(Long userId, String remoteIp, String kind, String gcmId, String appVersion, String osName, String osVersion, String maker, String model) throws Exception {
        String sql =
                "UPDATE user_login_history SET login_date=now() WHERE user_id = ? AND remote_ip = ? AND kind = ? AND gcm_id = ? AND app_version = ?  AND os_name = ? AND os_version = ? AND maker = ? AND model = ?";
        this.jdbcTemplate.update(sql, userId, remoteIp, kind, gcmId, appVersion, osName, osVersion, maker, model);
    }

    /**
     * 사용자 로그인 히스토리 중복 조회
     */
    private List<Long> selectUserLoginHistory(Long userId, String remoteIp, String kind, String gcmId, String appVersion, String osName, String osVersion, String maker, String model) throws Exception {

        String sql =
                "SELECT user_id FROM user_login_history WHERE user_id = ? AND remote_ip = ? AND kind = ? AND gcm_id = ? AND app_version = ? AND os_name = ? AND os_version = ? AND maker = ? AND model = ?";
        List<Long> list = jdbcTemplate.query(sql, new Object[] {userId, remoteIp, kind, gcmId, appVersion, osName, osVersion, maker, model}, (rs, rowNum) -> {
            return rs.getLong("user_id");
        });
        if (list == null || list.isEmpty()) {
            return null;
        }

        return list;
    }

    @Override
    public void logout(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.invalidate();
    }

    @Override
    public void deactivate(HttpServletRequest req) {
        Long userId = SessionAttrs.getUserId(req.getSession());

        User user = userRepository.findOne(userId);
        user.type.deactivated = true;
        user.deactiveDate = new Date();

        // -- 사용자 설정변경 HISTORY --
        try {
            saveUserUpdateHistory(user, user, UserUpdateHistoryVo.TYPE_DEACTIVE, null);
        } catch (Exception e) {
            logger.error(">>> 사용자 설정변경 히스토리 오류 [ 탈퇴 ]", e);
        }
        // -- 사용자 설정변경 HISTORY --

        userRepository.saveAndFlush(user);

        req.getSession().invalidate();
    }

    @Override
    public User saveAndFlush(User user) {
        return convertToPrivateUser(userRepository.saveAndFlush(user));
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findOne(userId);
    }

    @Override
    public User getUser(String email) {
        return userRepository.findOneByEmail(encString(email));
    }

    @Override
    public List<User> getUsersByPhone(String phone) {
        return userRepository.findByPhone(encString(phone));
    }

    @Override
    public List<User> getUsersByHouseId(Long houseId) {
        return userRepository.findByHouseId(houseId);
    }

    @Override
    public List<User> getUsersByHouseIn(List<Long> houseIds) {
        return userRepository.findByHouseIdIn(houseIds);
    }

    @Override
    public List<User> getAdminUsers(Long aptId) {
        List<Long> userIds =
                jdbcTemplate.query("SELECT u.id FROM user u, user_type ut, house h WHERE " + "h.apt_id=? AND u.house_id=h.id AND u.id=ut.user_id AND ut.admin=1", new Object[] {aptId},
                        (rs, rowNum) -> {
                            return rs.getLong("id");
                        });

        return Lists.newArrayList(userRepository.findByIdIn(userIds));
    }

    @Override
    @Transactional
    public User changeUserNickname(User user, String nickname) {
        if (nickname == null || "".equals(nickname.trim())) {
            user.setNickname(null);
            return userRepository.saveAndFlush(user);
        }
        UserNickname nick = userNicknameRepository.findOne(nickname);
        if (nick != null) {
            logger.info("<<닉네임 변경 중 닉네임 중복>> {}", nickname);
            return null;
        }
        if (user.getNickname() != null) {
            logger.info("<<기존의 닉네임({}) 삭제>> {}", user.getNickname());
            userNicknameRepository.delete(user.getNickname());
            user.setNickname(null);
            userRepository.saveAndFlush(user);
        }
        nick = new UserNickname();
        nick.name = nickname;
        user.setNickname(nick);

        try {
            return convertToPrivateUser(userRepository.saveAndFlush(user));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public User convertToPublicUser(User user) {
        // logger.info("* 게시판 사용자정보 초기화, 사용자아이디: {}, 이메일: {}, 휴대전화번호: {}, 생년: {}, 성별: {}", user.id, user.getEmail(), user.getPhone(), user.birthYear, user.gender);

        // user.house = null; // 게시판에 동 표시를 위해서 주석처리함
        user.setPhone(null);
        user.type = null;
        user.setting = null;
        user.setEmail(null);
        user.birthYear = null;
        user.gender = null;

        return user;
    }

    @Override
    public User convertToPrivateUser(User user) {
        user.house.apt.strAddress = AddressConverter.toStringAddress(user.house.apt.address);
        user.house.apt.strAddressOld = AddressConverter.toStringAddressOld(user.house.apt.address);

        user.house.apt.regionState = user.house.apt.address.시도명;
        user.house.apt.regionCity = user.house.apt.address.시군구명;
        user.house.apt.regionDong = user.house.apt.address.행정동명;

        return user;
    }

    @Override
    public Setting getSetting(Long userId) {
        Setting ret = settingRepository.findOne(userId);
        if (ret == null) {
            ret = settingRepository.saveAndFlush(new Setting(userId));
        }

        return ret;
    }

    // 닉네임으로 사용자 검색
    @Override
    public User getUserByNickName(String nickName) {
        UserNickname userNickName = userNicknameRepository.findOne(nickName);

        if (userNickName == null) {
            return null;
        }

        return userRepository.findOneByNickname(userNickName);
    }

    @Override
    public Setting saveAndFlush(Setting setting) {
        return settingRepository.saveAndFlush(setting);
    }

    // 게시판용 유저 변환하기 (동 추가표시 및 닉네임 없는 경우 풀네임 설정 등의 데이터 가공을 할 경우)
    @Override
    public User convertUserForPost(User user, Long aptId, String dong, BoardCategory.UserPrivacy userPrivacy) {
        if (user == null) {
            return user;
        }

        user.setNickname(convertNickNameForPost(user.getNickname(), user.getFullName(), aptId, dong, userPrivacy));

        return user;
    }

    // 게시판용 닉네임 변환하기 (동 추가표시 및 닉네임 없는 경우 풀네임 설정 등의 데이터 가공을 할 경우)
    private UserNickname convertNickNameForPost(UserNickname nickName, String fullName, Long aptId, String dong, BoardCategory.UserPrivacy userPrivacy) {
        if (fullName == null) {
            return nickName;
        }

        String postFixDong = "동)";
        String strDong = " (" + dong + postFixDong;
        String newNickName = "";
        String oldNickName = null;
        UserNickname newUserNickName = new UserNickname();

        if (nickName != null) {
            oldNickName = nickName.name;
        }

        newNickName = oldNickName;

        // 0동인 경우 동표시 안함 (일단은 혼란의 여지가 있으니 0동으로 표시)
        // if ("0".equals(dong)) {
        // strDong = "";
        // }

        // !nickName.endsWith(postFixDong) ==> 중복 처리를 방지하기 위해서
        if (oldNickName != null && !oldNickName.endsWith(postFixDong)) {
            newNickName = oldNickName + strDong;
        }

        // 닉네임이 없는 경우는 이름 + 동으로 표시
        // 위시티5차의 경우 실명 표시를 위해서 nickname에 fullname 저장함, 동 표시 추가
        // 2017.01.19 오석민 팀장 요청으로 위시티블루밍5단지아파트 하드코딩 제거
        // if (oldNickName == null || (aptId == 255 && !oldNickName.endsWith(postFixDong)) || userPrivacy == BoardCategory.UserPrivacy.NAME) {
        // if (oldNickName == null || (!oldNickName.endsWith(postFixDong)) || userPrivacy == BoardCategory.UserPrivacy.NAME) {
        if (oldNickName == null || userPrivacy == BoardCategory.UserPrivacy.NAME) {
            newNickName = fullName + strDong;
        }

        newUserNickName.name = newNickName;

        return newUserNickName;
    }

    @Override
    @Transactional
    public void modifyLastLoginDate(long userId) {
        this.userRepository.updateLastLoginDate(userId);
    }

    @Override
    @Transactional
    public void modifyLastLoginDateAndRemoteIpAndAppVersion(long userId, String ip, String appVersion, String osName, String osVersion, String maker, String model) {
        this.userRepository.modifyLastLoginDateAndRemoteIpAndAppVersion(userId, ip, appVersion, osName, osVersion, maker, model);
    }


    @Override
    public User checkUserInfo(String email, String fullName, String phoneNumber) throws Exception {
        BaseSecuModel bsm = new BaseSecuModel();

        List<User> userList = null;

        if (StringUtils.isBlank(email)) {
            userList = this.userRepository.findByFullNameAndPhone(bsm.encString(fullName), bsm.encString(phoneNumber));
        } else {
            userList = this.userRepository.findByEmailAndFullNameAndPhone(bsm.encString(email), bsm.encString(fullName), bsm.encString(phoneNumber));
        }

        if (userList == null || userList.isEmpty()) {
            return null;
        } else {
            return userList.get(0);
        }
    }

    @Override
    public List<SimpleUser> checkUserInfo(String email, String phone) throws Exception {
        BaseSecuModel bsm = new BaseSecuModel();

        Map<String, Object> params = Maps.newHashMap();

        params.put("phone", bsm.encString(phone));
        if (!StringUtils.isBlank(email)) {
            params.put("email", bsm.encString(email));
        }

        List<SimpleUser> userList = userMapper.selectUserList(params);

        return userList;
    }

    @Override
    @Transactional
    public boolean resetPassword(String password, String email) {
        try {
            BaseSecuModel bsm = new BaseSecuModel();

            this.userRepository.updatePassword(PasswordHash.createHash(password), bsm.encString(email));

            return true;
        } catch (Exception e) {
            logger.error("", e);
        }

        return false;
    }

    @Override
    public Map<String, Object> phoneAccountSearch(String phone) {
        Map<String, Object> result = Maps.newHashMap();

        BaseSecuModel bsm = new BaseSecuModel();
        String encPhone = bsm.encString(phone);
        List<User> userList = userRepository.findByPhone(encPhone);

        // userList.sort((User x, User y) -> y.regDate.compareTo(x.regDate)); // 2016-10-24 카운트만 필요해서 list는 같이 전달안하여 정렬필요없음.

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

        int size = userList.size();

        result.put("currCnt", size);
        result.put("maxCnt", Constants.PHONE_USER_ACCOUNT_MAX);

        // 2016-10-24 카운트만 필요해서 list는 같이 전달안하여 정렬필요없음.
        /*
         * List<Map<String, Object>> resultList = Lists.newArrayList(); for (int i = 0; i < size; i++) { User _user = userList.get(i); Map<String, Object> _item = Maps.newHashMap();
         * 
         * _item.put("id", _user.id); _item.put("email", _user.getEmail()); _item.put("fullName", _user.getFullName()); _item.put("nickname", _user.getNickname());
         * 
         * String lastLoginDate = _user.lastLoginDate == null ? "" : sdf.format(_user.lastLoginDate); String deactiveDate = _user.deactiveDate == null ? "" : sdf.format(_user.deactiveDate); String
         * regDate = _user.regDate == null ? "" : sdf.format(_user.regDate);
         * 
         * _item.put("lastLoginDate", lastLoginDate); _item.put("deactiveDate", deactiveDate); _item.put("regDate", regDate); resultList.add(_item); } result.put("accountList", resultList);
         */

        return result;
    }

    @Override
    @Transactional
    public void modifyUserInfo(long userId, String kind, String gcmId, String appVersion) {
        this.userRepository.updateUserInfo(userId, kind, gcmId, appVersion);
    }

    @Override
    @Transactional
    public void updateUserGcmId(long userId, String gcmId) {
        this.userRepository.updateUserGcmId(userId, gcmId);
    }


    @Override
    @Transactional
    public void updateUserUniqueDeviceId(long userId, String uniqueDeviceId) {
        this.userRepository.updateUserUniqueDeviceId(userId, uniqueDeviceId);
    }

    @Override
    @Transactional
    public void updateUserDeviceInfo(long userId, String osName, String osVersion, String maker, String model) {
        this.userRepository.updateUserDeviceInfo(userId, osName, osVersion, maker, model);
    }



    @Override
    @Transactional
    public void modifyUserAddInfo(long userId, String birthYear, String gender) {
        this.userRepository.updateUserAddInfo(userId, birthYear, gender);
    }

    @Override
    @Transactional
    public User login(String email, String kind, String gcmId, String appVersion) {
        User user = this.getUser(email);

        if (user == null || user.type == null || user.type.deactivated) {
            logger.info("<<사용자가 없거나 탈퇴한 회원이란다~>> {}", user.id);
            return null;
        }

        try {
            this.userRepository.updateUserInfo(user.id, StringUtils.defaultString(kind, "android"), gcmId, appVersion);

            if (StringUtils.isBlank(user.house.apt.aptOfficePhoneNumberInner)) { // 아파트 관리소 번호가 없는 경우
                if ((user.house.apt.aptInfo != null) && StringUtils.isNotBlank(user.house.apt.aptInfo.aptOfficePhoneNumber)) {
                    user.house.apt.aptOfficePhoneNumberInner = StringUtils.replace(user.house.apt.aptInfo.aptOfficePhoneNumber, "-", StringUtils.EMPTY);
                }
            }
        } catch (Exception e) {
            // 관리소 번호가 없으면 그냥 안함
            logger.error("<<로그인 서비스 처리 중 오류, {}>>", e);
        }

        return convertToPrivateUser(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jaha.server.emaul.service.UserService#checkUserPassword(com.jaha.server.emaul.model.User)
     */
    @Override
    @Transactional(readOnly = true)
    public User checkUserPassword(User user) throws Exception {
        User checkUser = userRepository.findOneById(user.id);
        if (checkUser == null) {
            return null;
        } else {
            if (PasswordHash.validatePassword(user.passwordHash, checkUser.passwordHash)) {
                return checkUser;
            } else {
                return null;
            }
        }

    }


    /*
     * (non-Javadoc)
     * 
     * @see com.jaha.server.emaul.service.UserService#updateUserNickname(java.lang.Long, java.lang.String)
     */
    @Override
    @Transactional
    public int updateUserNickname(Long userId, String nickName) throws Exception {
        logger.debug(">>> userId : " + userId + "/nickname : " + nickName);

        User user = userRepository.findOneById(userId);
        if (user == null) {
            return 0;
        }
        UserNickname userNickName = userNicknameRepository.findOne(nickName);
        if (userNickName == null) {
            // INSERT UserNickname
            userNickName = new UserNickname();
            userNickName.name = nickName;
        }
        // 중복체크
        // userRepository.findOneByNickname(userNickName);
        user.setNickname(userNickName);
        user = userRepository.saveAndFlush(user);

        return 1;
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.jaha.server.emaul.service.UserService#updateUserMultiLogin(java.lang.Long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional
    public int updateUserMultiLogin(Long userId, String multiLoginYn, String gcmId, String deviceId) throws Exception {

        User user = userRepository.findOneById(userId);
        if (user == null) {
            return 0;
        }

        if (StringUtils.isNotEmpty(gcmId)) {
            user.gcmId = gcmId;
        }

        if (StringUtils.isNotEmpty(deviceId)) {
            user.uniqueDeviceId = deviceId;
        }

        user.multiLoginYn = multiLoginYn.toUpperCase();
        user = userRepository.saveAndFlush(user);

        // 외부기기 접속 비허용 시 기존 로그아웃을 위하여 푸시 발송
        // if (!"Y".equalsIgnoreCase(multiLoginYn)) {
        //
        // try {
        // // 기존 접속의 GCM_ID에 로그아웃 푸시를 발송한다.
        // Sender sender = new Sender(env.getProperty("multilogin.push.key"));
        // Message.Builder Builders = new Message.Builder();
        // Builders.addData("push_check_ids", "1");
        // Builders.addData("push_type", "function-execute");
        // Builders.addData("type", "function-execute");
        // Builders.addData("function", "[\"logout\"]");
        // Builders.addData("userId", user.id.toString());
        // Builders.addData("title", "외부기기 로그아웃");
        // Builders.addData("value", "외부기기 사용으로 로그아웃 합니다.");
        //
        // Message message = Builders.build();
        // Map<String, Object> map = new HashMap<String, Object>();
        // map.put("userId", userId);
        // map.put("gcmId", gcmId);
        // List<String> list = userMapper.selectUserGcmHistory(map); // new ArrayList<String>();
        //
        // if (!(list == null || list.isEmpty())) {
        // MulticastResult multiResult;
        //
        // multiResult = sender.send(message, list, 5);
        // if (multiResult != null) {
        // List<Result> resultList = multiResult.getResults();
        // for (Result result : resultList) {
        // logger.debug(">>> push result : " + result.getMessageId());
        // }
        // }
        // }
        //
        // } catch (IOException e) {
        // logger.debug(">>> 외부기기 로그아웃 푸시발송중 오류", e);
        // }
        //
        // }

        return 1;
    }

    @Override
    @Transactional
    public int insertUserUpdateHistory(UserUpdateHistoryVo history) throws Exception {
        return userMapper.insertUserUpdateHistory(history);
    }

    @Override
    public int saveUserUpdateHistory(User user, User targetUser, String type, String data) {

        UserUpdateHistoryVo history = new UserUpdateHistoryVo();

        history.setUserId(targetUser.id);
        history.setType(type);
        history.setModId(user.id);
        history.setData(data);

        history.setAuth(targetUser.type.getTrueTypes().toString());
        history.setUserName(targetUser.getFullNameRawData());
        history.setEmail(targetUser.getEmailRawData());
        history.setPhone(targetUser.getPhoneRawData());
        history.setBirthYear(targetUser.birthYear);
        history.setGender(targetUser.gender);
        if (targetUser.getNickname() != null) {
            history.setNickname(targetUser.getNickname().name);
        }
        history.setHouseId(targetUser.house.id);

        return userMapper.insertUserUpdateHistory(history);
    }

    @Override
    public int saveUserLoginLog(User user, String type) {

        UserLoginLog userLoginLog = new UserLoginLog();
        userLoginLog.type = type;
        userLoginLog.userId = user.id;
        userLoginLog.maker = user.maker;
        userLoginLog.model = user.model;
        userLoginLog.appVersion = user.appVersion;

        return userMapper.insertUserLoginLog(userLoginLog);
    }


}
