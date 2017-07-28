package com.jaha.server.emaul.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.ParcelSmsPolicy;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
 * @description 무인택배함 SMS 정책 Repository
 */
@Repository
public interface ParcelSmsPolicyRepository extends JpaRepository<ParcelSmsPolicy, Long> {

    ParcelSmsPolicy findByAptIdAndTestServiceStartDateGreaterThanEqualAndTestServiceEndDateLessThanEqual(Long aptId, String date1, String date2);

}
