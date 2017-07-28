package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.UserNickname;

/**
 * Created by doring on 15. 3. 9..
 */
/**
 * <pre>
 * Class Name : UserRepository.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 10. 18.     shavrani      Generation
 * </pre>
 *
 * @author shavrani
 * @since 2016. 10. 18.
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findOneByEmail(String email);

    List<User> findByPhone(String phone);

    List<User> findByHouseId(Long houseId);

    List<User> findByHouseIdIn(List<Long> houseIds);

    List<User> findByIdIn(List<Long> userIds);

    // 닉네임으로 사용자 검색
    User findOneByNickname(UserNickname userNickName);

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 2.
     * @description user 테이블의 마지막 로그인 시간 업데이트
     *
     * @param userId
     */
    @Modifying
    @Query("UPDATE User SET last_login_date = now() WHERE id = :userId")
    void updateLastLoginDate(@Param("userId") long userId);


    /**
     * 마지막 로그인 시간 / 접속지 IP, App Version 업데이트 <br/>
     * cyt
     *
     * @param userId
     * @param remoteIp
     */
    @Modifying
    @Query("UPDATE User SET last_login_date = now() ,remote_ip =:remoteIp ,app_version =:appVersion WHERE id = :userId")
    void modifyLastLoginDateAndRemoteIpAndAppVersion(@Param("userId") long userId, @Param("remoteIp") String remoteIp, @Param("appVersion") String appVersion);


    @Modifying
    @Query("UPDATE User SET last_login_date = now() ,remote_ip =:remoteIp ,app_version =:appVersion ,os_name =:osName ,os_version =:osVersion ,maker =:maker ,model =:model WHERE id = :userId")
    void modifyLastLoginDateAndRemoteIpAndAppVersion(@Param("userId") long userId, @Param("remoteIp") String remoteIp, @Param("appVersion") String appVersion, @Param("osName") String osName,
            @Param("osVersion") String osVersion, @Param("maker") String maker, @Param("model") String model);


    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 8.
     * @description 사용자명과 폰번호로 조회
     *
     * @param fullName
     * @param phoneNumber
     * @return
     */
    User findOneByFullNameAndPhone(String fullName, String phoneNumber);

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 8.
     * @description 이메일, 사용자명과 폰번호로 조회
     *
     * @param fullName
     * @param phoneNumber
     * @return
     */
    User findOneByEmailAndFullNameAndPhone(String email, String fullName, String phoneNumber);

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 9.
     * @description user 테이블의 비밀번호 업데이트
     *
     * @param passwordHash
     * @param email
     */
    @Modifying
    @Query("UPDATE User SET password_hash = :password WHERE email = :email")
    void updatePassword(@Param("password") String passwordHash, @Param("email") String email);

    /**
     * 암호화된 사용자명으로 사용자 목록 조회
     *
     * @param fullName
     * @return
     */
    List<User> findByFullName(String fullName);

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 8.
     * @description 사용자명과 폰번호로 조회
     *
     * @param fullName
     * @param phoneNumber
     * @return
     */
    List<User> findByFullNameAndPhone(String fullName, String phoneNumber);

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 8.
     * @description 이메일, 사용자명과 폰번호로 조회
     *
     * @param fullName
     * @param phoneNumber
     * @return
     */
    List<User> findByEmailAndFullNameAndPhone(String email, String fullName, String phoneNumber);

    /**
     * @author shavrani 2016-10-18
     * @description 이메일, 폰번호로 조회
     */
    List<User> findByEmailAndPhone(String email, String phoneNumber);

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 10. 26
     * @description user 테이블의 kind, gcm_id, app_version, last_login_date 수정
     * @TODO: 로그인 기록은 따로 빼야함
     *
     * @param userId
     */
    @Modifying
    @Query("UPDATE User SET kind = :kind, gcm_id = :gcmId, app_version = :appVersion, last_login_date = now() WHERE id = :userId")
    void updateUserInfo(@Param("userId") long userId, @Param("kind") String kind, @Param("gcmId") String gcmId, @Param("appVersion") String appVersion);


    @Modifying
    @Query("UPDATE User SET os_name = :osName, os_version = :osVersion, maker = :maker, model = :model WHERE id = :userId")
    void updateUserDeviceInfo(@Param("userId") long userId, @Param("osName") String osName, @Param("osVersion") String osVersion, @Param("maker") String maker, @Param("model") String model);



    /**
     * user.gcm_id를 업데이트 한다.
     *
     * @param userId
     * @param gcmId
     */
    @Modifying
    @Query("UPDATE User SET gcm_id = :gcmId WHERE id = :userId")
    void updateUserGcmId(@Param("userId") long userId, @Param("gcmId") String gcmId);


    /**
     * user.unique_device_id를 업데이트 한다.
     *
     * @param userId
     * @param uniqueDeviceId
     */
    @Modifying
    @Query("UPDATE User SET unique_device_id = :uniqueDeviceId WHERE id = :userId")
    void updateUserUniqueDeviceId(@Param("userId") long userId, @Param("uniqueDeviceId") String uniqueDeviceId);



    /**
     * @author 조영태(cyt@jahasmart.com), 2016. 11. 16
     * @description user 테이블의 birth_year, gender 수정 (IOS의 휴대폰 인증 시 입력받은 주민번호를 사용하여 고객 정보 수정)
     *
     * @param userId
     * @param birthYear
     * @param gender
     */
    @Modifying
    @Query("UPDATE User SET birth_year = :birthYear, gender = :gender WHERE id = :userId")
    void updateUserAddInfo(@Param("userId") long userId, @Param("birthYear") String birthYear, @Param("gender") String gender);


    /**
     * @author 조영태(cyt@jahasmart.com), 2016. 11. 22
     * @description 사용자번호 사용하여 해당 고객정보를 조회한다. <br/>
     *
     * @param userId
     * @param passwordHash
     * @return
     */
    User findOneById(Long id);


}
