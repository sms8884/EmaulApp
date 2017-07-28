package com.jaha.server.emaul.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.ParcelLocker;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
 * @description 무인택배함 Repository
 */
@Repository
public interface ParcelLockerRepository extends JpaRepository<ParcelLocker, Long> {

    /**
     * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
     * @description uuid로 무인택배함 조회
     *
     * @param uuid
     * @return
     */
    ParcelLocker findByUuid(String uuid);

    // /**
    // * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
    // * @description uuid와 secretKey로 무인택배함 조회
    // *
    // * @param uuid
    // * @param secretKey
    // * @return
    // */
    // ParcelLocker findByUuidAndSecretKey(String uuid, String secretKey);

}
