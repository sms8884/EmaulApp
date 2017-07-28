package com.jaha.server.emaul.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.PushLog;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
 * @description 택배 회사 Repository
 */
@Repository
public interface PushLogRepository extends JpaRepository<PushLog, Long> {

    List<PushLog> findByUserId(Long userId);

    List<PushLog> findByDeviceRecYnIsAndModDateLessThan(String deviceRecYn, Date compDate);

    List<PushLog> findByIdLessThanAndUserId(Long lastPushId, Long userId, Sort sort);

    List<PushLog> findByIdLessThanAndUserIdAndGubun(Long lastPushId, Long userId, String gubun, Pageable pagealbe);

    List<PushLog> findByIdLessThanAndUserIdAndGubunOrderByIdDesc(Long lastPushId, Long userId, String gubun);

    List<PushLog> findByIdLessThanAndUserIdOrderByIdDesc(Long lastPushId, Long userId);

    @Modifying
    @Query("UPDATE PushLog SET device_rec_yn = 'Y', device_type = :deviceType, mod_date = NOW() WHERE id = :id")
    int updateDeviceRecYn(@Param("id") Long id, @Param("deviceType") String deviceType);

    @Modifying
    @Query("UPDATE PushLog SET push_send_count = push_send_count + 1, mod_date = NOW() WHERE id = :id")
    int updatePushSendCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PushLog SET push_click_count = push_click_count + 1, device_type = :deviceType, mod_date = NOW() WHERE id = :id")
    int updatePushClickCount(@Param("id") Long id, @Param("deviceType") String deviceType);

    @Modifying
    @Query("UPDATE PushLog SET sms_yn = 'Y', mod_date = NOW() WHERE id = :id")
    int updateSmsYn(@Param("id") Long id);

}
