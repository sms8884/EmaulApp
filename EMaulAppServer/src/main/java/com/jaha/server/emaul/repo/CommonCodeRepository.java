package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.CommonCode;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {

    List<CommonCode> findByCodeGroupAndUseYnOrderBySortOrderAsc(String codeGroup, String useYn);

}
