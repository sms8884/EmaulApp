package com.jaha.server.emaul.service;

import java.util.Date;

import org.springframework.data.domain.Pageable;

import com.jaha.server.emaul.model.ParcelLocker;
import com.jaha.server.emaul.model.ParcelLog;
import com.jaha.server.emaul.model.ParcelNotification;
import com.jaha.server.emaul.model.PushLog;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.util.ScrollPage;

/**
 * Created by doring on 15. 5. 20..
 */
public interface ParcelService {

    public void save(ParcelNotification parcel);

    ScrollPage<ParcelNotification> getParcelNotifyResult(Long aptId, Long lastItemId);

    ScrollPage<ParcelNotification> getParcelNotifications(User user, Long lastItemId);

    ParcelNotification disableParcelItem(User user, Long itemId);



    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 택배기사가 무인택배함에 택배를 보관하는 경우
     *
     * @param uuid 택배함 UUID
     * @param authKey 보안인증키
     * @param lockerNum 보관함번호
     * @param password 비밀번호
     * @param dong 동
     * @param ho 호
     * @param phone 핸드폰번호
     * @param parcelCompanyId 택배회사코드
     * @param parcelPhone 택배기사 핸드폰번호
     * @param date 날짜
     * @return
     */
    ParcelLog keepParcel(String uuid, String authKey, String lockerNum, String password, int dong, int ho, String phone, String parcelCompanyId, String parcelPhone, String date, int apiNumber)
            throws Exception;

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 아파트 입주민이 무인택배함에서 택배를 찾는 경우
     *
     * @param uuid
     * @param authKey
     * @param lockerNum
     * @param dong
     * @param ho
     * @param phone
     * @param date
     * @return
     */
    ParcelLog findUserParcel(String uuid, String authKey, String lockerNum, int dong, int ho, String phone, String date, int apiNumber) throws Exception;

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 아파트 입주민이 무인택배함에 택배를 보관하는 경우
     *
     * @param gubun(new/return)
     * @param uuid
     * @param authKey
     * @param lockerNum
     * @param password
     * @param dong
     * @param ho
     * @param phone
     * @param parcelCompanyId
     * @param parcelPhone
     * @param date
     * @return
     */
    ParcelLog keepUserParcel(String gubun, String uuid, String authKey, String lockerNum, String password, int dong, int ho, String phone, String parcelCompanyId, String parcelPhone, String date,
            int apiNumber) throws Exception;

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description 택배기사가 무인택배함에서 택배를 찾는 경우
     *
     * @param gubun(new/return)
     * @param uuid
     * @param authKey
     * @param lockerNum
     * @param password
     * @param dong
     * @param ho
     * @param phone
     * @param parcelCompanyId
     * @param parcelPhone
     * @param date
     * @return
     */
    ParcelLog findParcel(String gubun, String uuid, String authKey, String lockerNum, String password, int dong, int ho, String phone, String parcelCompanyId, String parcelPhone, String date,
            int apiNumber) throws Exception;

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description uuid로 무인택배함 조회
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    ParcelLocker findParcelLocker(String uuid) throws Exception;

    /**
     *
     * @param id
     * @param deviceType
     * @throws Exception
     */
    void modifyDeviceRecYn(Long id, String deviceType) throws Exception;

    /**
     *
     * @param id
     * @throws Exception
     */
    void modifyPushSendCount(Long id) throws Exception;

    /**
     *
     * @param id
     * @param deviceType
     * @throws Exception
     */
    PushLog modifyPushClickCount(Long id, String deviceType) throws Exception;

    /**
     *
     * @param id
     * @throws Exception
     */
    void modifySmsYn(Long id) throws Exception;

    /**
     *
     * @param deviceRecYn
     * @param compDate
     * @return
     * @throws Exception
     */
    void resendPush(String deviceRecYn, Date compDate) throws Exception;

    /**
     * @param lastPushId
     * @param userId
     * @param count
     * @throws Exception
     */
    ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, Integer count) throws Exception;

    /**
     * @param lastPushId
     * @param userId
     * @param gubun
     * @param pageable
     * @throws Exception
     */
    ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, String gubun, Pageable pageable) throws Exception;

    /**
     * @param lastPushId
     * @param userId
     * @param gubun
     * @param count
     * @throws Exception
     */
    ScrollPage<PushLog> findPushList(Long lastPushId, Long userId, String gubun, Integer count) throws Exception;

    /**
     *
     * @return
     * @throws Exception
     */
    void sendPush4Admin() throws Exception;

}
