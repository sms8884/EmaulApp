package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.JhElectLog;
import com.jaha.server.emaul.model.VoteKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Administrator on 2015-06-28.
 */
@Repository
public interface VoteKeyRepository extends JpaRepository<VoteKey, Long> {
    //deleteByHouseIdInAndDate

    @Modifying
    @Transactional
    @Query(value =
            "DELETE FROM vote_key WHERE vk_id = :vkId and apt_id = :aptId",
            nativeQuery = true)
    int deleteVkId(@Param("vkId") int vkId,@Param("aptId") Long aptId);


    @Modifying
    @Transactional
    @Query(value =
            "UPDATE vote_key " +
                    "   set grant_sign_fname=:grantSignFname " +
                    "      ,key_grant_dec = :keyGrantDec " +
                    "      ,key_grant_yn = 'Y' "+
                    "  WHERE vk_id = :vkId and apt_id = :aptId ",
            nativeQuery = true)

    int updateGrantKey(@Param("vkId") int vkId,
                       @Param("aptId") Long aptId,
                       @Param("keyGrantDec") String keyGrantDec,
                       @Param("grantSignFname") String grantSignFname);

}
