package com.jaha.server.emaul.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.ParcelCompany;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 26.
 * @description 택배 회사 Repository
 */
@Repository
public interface ParcelCompanyRepository extends JpaRepository<ParcelCompany, String> {

}
