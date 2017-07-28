package com.jaha.server.emaul.repo;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.Pollution;

@Repository
public interface PollutionRepository extends JpaRepository<Pollution, Long>{
    
    Pollution findByBaseDate(String baseDate);

    Pollution findByBaseDateAndPushYn(String baseDate, String pushYn);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE pollution p SET p.push_yn = 'Y' WHERE p.seq = :seq", nativeQuery = true)
    int setPushYn(@Param("seq") Long seq);

}
