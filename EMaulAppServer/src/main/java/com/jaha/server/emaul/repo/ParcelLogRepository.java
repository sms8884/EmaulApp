package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.ParcelLog;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
 * @description 무인택배함사용기록 Repository
 */
@Repository
public interface ParcelLogRepository extends JpaRepository<ParcelLog, Long> {

    @Query(value = "SELECT IFNULL(MAX(id), 0) FROM parcel_log WHERE uuid = :uuid AND locker_num = :lockerNum AND dong = :dong AND ho = :ho AND api_number = 1 AND find_date IS NULL",
            nativeQuery = true)
    long findKeepParcelJustBefore(@Param("uuid") String uuid, @Param("lockerNum") String lockerNum, @Param("dong") Integer dong, @Param("ho") Integer ho);

    @Query(value = "SELECT IFNULL(MAX(id), 0) FROM parcel_log WHERE uuid = :uuid AND locker_num = :lockerNum AND dong = :dong AND ho = :ho AND api_number = 6 AND find_date IS NULL",
            nativeQuery = true)
    long findLongKeepParcel(@Param("uuid") String uuid, @Param("lockerNum") String lockerNum, @Param("dong") Integer dong, @Param("ho") Integer ho);

    @Modifying
    @Query(value = "UPDATE parcel_log SET find_date = NOW() WHERE id = :id", nativeQuery = true)
    int updateFindDate(@Param("id") Long id);

    @Query(value = "SELECT * FROM parcel_log WHERE reg_date >= DATE_ADD(NOW(), INTERVAL -3 DAY) AND reg_date <= DATE_ADD(NOW(), INTERVAL -1 DAY) AND api_number = 1 AND find_date IS NULL AND office_keep_date IS NULL",
            nativeQuery = true)
    List<ParcelLog> findLongKeepParcel4Admin();

}
