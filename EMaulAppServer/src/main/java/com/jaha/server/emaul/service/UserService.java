package com.jaha.server.emaul.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.jaha.server.emaul.model.BoardCategory;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.Setting;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.v2.model.user.UserUpdateHistoryVo;


/**
 * Created by doring on 15. 3. 9..
 */
public interface UserService {
    User createUser(HttpServletRequest req, String uid, String addressCode, String dong, String ho, String birthYear, String gender, String email, String name, String password, String phoneNumber,
            Long recommId) throws Exception;

    /**
     * 개편된 계정생성
     *
     * @author shavrani 2016-10-18
     */
    User createUser(HttpServletRequest req, Map<String, Object> params) throws Exception;

    Boolean isPrepassUser(User user);

    House selectOrCreateHouse(String addressCode, String dong, String ho);

    /**
     * address, apt, house 생성
     *
     * @author shavrani 2016-10-18
     */
    House selectOrCreateAddressAndHouse(String sidoNm, String sggNm, String emdNm);

    // User login(HttpServletRequest req, String email, String password);
    // User login(HttpServletRequest req, String email, String password, String kind);
    // 2016.11.17 cyt : 추가
    /**
     * 상위 선언된 <br/>
     * login(HttpServletRequest req, String email, String password) <br />
     * login(HttpServletRequest req, String email, String password, String kind) <br/>
     * method를 일원화 하고 gcmId, appVersion 을 user_login_history 까지 적재하도록 프로세스 수정
     *
     * @param req
     * @param email
     * @param password
     * @param kind
     * @param gcmId
     * @param appVersion
     * @return
     */
    User login(HttpServletRequest req, String email, String password, String kind, String gcmId, String appVersion, String deviceId, String osName, String osVersion, String maker, String model);

    void logout(HttpServletRequest req);

    void deactivate(HttpServletRequest req);

    User saveAndFlush(User user);

    User getUser(Long userId);

    User getUser(String email);

    List<User> getUsersByPhone(String phone);

    List<User> getUsersByHouseId(Long houseId);

    List<User> getUsersByHouseIn(List<Long> houseIds);

    List<User> getAdminUsers(Long aptId);

    User changeUserNickname(User user, String nickname);

    User convertToPublicUser(User user);

    User convertToPrivateUser(User user);

    Setting getSetting(Long userId);

    // 닉네임으로 사용자 검색
    User getUserByNickName(String nickName);

    Setting saveAndFlush(Setting setting);

    User convertUserForPost(User user, Long aptId, String dong, BoardCategory.UserPrivacy userPrivacy);

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 2.
     * @description 사용자의 마지막 로그인 시간을 수정한다.
     * @deprecated 20161026, realsnake
     *
     * @param userId
     */
    @Deprecated
    void modifyLastLoginDate(long userId);


    /**
     * @author cyt@jahasmart.com), 2016. 11. 21
     * @description modifyLastLoginDate 에 RemoteIp, appVersion를 추가하여 메소드 생성
     *
     * @param userId
     * @param ip
     * @param appVersion
     */
    // void modifyLastLoginDateAndRemoteIpAndAppVersion(long userId, String ip, String appVersion);
    void modifyLastLoginDateAndRemoteIpAndAppVersion(long userId, String ip, String appVersion, String osName, String osVersion, String maker, String model);


    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 8.
     * @description 아이디/비밀번호 찾기 시 사용자 정보 조회
     *
     * @param email
     * @param fullName
     * @param phoneNumber
     * @return
     */
    User checkUserInfo(String email, String fullName, String phoneNumber) throws Exception;

    /**
     * @author shavrani 2016-10-20
     */
    List<SimpleUser> checkUserInfo(String email, String phoneNumber) throws Exception;

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 9.
     * @description 비밀번호 재설정
     *
     * @param password
     * @param email
     * @return
     */
    boolean resetPassword(String password, String email);

    /**
     * @author shavrani 2016-10-20
     * @desc 휴대폰번호로 등록된 계정목록, 개수, 등록할수있는 계정 총개수
     */
    Map<String, Object> phoneAccountSearch(String phone);

    /**
     * 로그인 시 사용자 정보를 업데이트한다.
     *
     * @param userId
     * @param kind
     * @param gcmId
     * @param appVersion
     */
    void modifyUserInfo(long userId, String kind, String gcmId, String appVersion);

    /**
     * 휴대폰 인증시 입력받은 주민번호를 사용하여 <br/>
     * 사용자의 생년 / 성별을 수정한다.
     *
     * @param userId
     * @param birthYear
     * @param gender
     */
    void modifyUserAddInfo(long userId, String birthYear, String gender);


    /**
     * user.gcm_id를 업데이트 한다.
     *
     * @param userId
     * @param gcmId
     */
    void updateUserGcmId(long userId, String gcmId);


    /**
     * user.unique_device_id를 업데이트 한다.
     *
     * @param userId
     * @param uniqueDeviceId
     */
    void updateUserUniqueDeviceId(long userId, String uniqueDeviceId);

    /**
     * 사용자 휴대폰 부가정보 저장
     *
     * @param userId
     * @param osName
     * @param osVersion
     * @param maker
     * @param model
     */
    void updateUserDeviceInfo(long userId, String osName, String osVersion, String maker, String model);


    /**
     * 로그인한다.(v2에서 사용)<br />
     *
     * @param email
     * @param kind
     * @param gcmId
     * @param appVersion
     * @return
     */
    User login(String email, String kind, String gcmId, String appVersion);



    /**
     * 사용자 아이디 + 비밀번호를 사용하여 사용자 정보를 조회한다.
     *
     * @param user
     * @return
     */
    User checkUserPassword(User user) throws Exception;

    /**
     * 사용자 닉네임 정보만 변경
     *
     * @param userId
     * @param nickName
     * @return
     */
    int updateUserNickname(Long userId, String nickName) throws Exception;


    /**
     * 외부 기기 로그인 허용 여부 변경
     *
     * @param userId
     * @param multiLoginYn
     * @param gcmId
     * @param deviceId
     * @return
     * @throws Exception
     */
    int updateUserMultiLogin(Long userId, String multiLoginYn, String gcmId, String deviceId) throws Exception;

    /**
     * 사용자 설정변경 히스토리 저장
     *
     * @param history
     * @return
     * @throws Exception
     */
    int insertUserUpdateHistory(UserUpdateHistoryVo history) throws Exception;

    /**
     * 사용자 설정변경 히스토리 저장 ( 저장당시의 유저의 정보를 입력 )
     */
    int saveUserUpdateHistory(User user, User targetUser, String type, String data);

    /**
     * 사용자가 직접 로그인 로그아웃한 기록저장
     */
    int saveUserLoginLog(User user, String type);

}
