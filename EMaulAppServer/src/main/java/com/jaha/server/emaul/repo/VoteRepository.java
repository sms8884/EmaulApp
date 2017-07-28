package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.Vote;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by doring on 15. 2. 25..
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findFirst10ByIdLessThanAndVisibleIsTrue(Long lastVoteId, Sort sort);

    List<Vote> findFirst10ByIdLessThanAndTypeIdInAndVisibleIsTrueAndTargetAptAndTargetDong(
            Long lastVoteId, List<Long> voteTypeIds, Sort sort, Long targetApt, String targetDong);

    List<Vote> findFirst10ByIdLessThanAndTypeIdInAndVisibleIsTrueAndTargetApt(
            Long lastId, List<Long> typeIds, Long targetApt, Sort sort);

    List<Vote> findBySecurityCheckStateGreaterThanAndSecurityNoticeState(Integer securityCheckState, Boolean securityNoticeState);
}
