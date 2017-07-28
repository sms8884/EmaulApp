package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.Vote;

/**
 * Created by doring on 15. 2. 25..
 */
@Repository
public interface VoteGroupRepository extends JpaRepository<Vote, Long> {

    List<Vote> findFirst10ByIdLessThanAndTypeIdInAndVisibleIsTrueAndTargetApt(Long lastId, List<Long> typeIds, Long targetApt, Sort sort);

    List<Vote> findBySecurityCheckStateGreaterThanAndSecurityNoticeState(Integer securityCheckState, Boolean securityNoticeState);
}
