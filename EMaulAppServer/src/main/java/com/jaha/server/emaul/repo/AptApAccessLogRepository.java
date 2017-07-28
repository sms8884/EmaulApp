package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.AptApAccessLog;

/**
 * Created by shavrani on 16. 05. 11..
 */
@Repository
public interface AptApAccessLogRepository extends JpaRepository<AptApAccessLog, Long> {

    List<AptApAccessLog> findByApIdAndWaitingYn(String aptApId, String waitingYn);

    List<AptApAccessLog> findByApIdAndUserIdAndWaitingYn(String aptApId, Long userId, String waitingYn);

    List<AptApAccessLog> findByApIdAndUserIdAndWaitingYnOrderByAccessDateDescIdDesc(String aptApId, Long userId, String waitingYn);

    List<AptApAccessLog> findByApIdAndUserIdOrderByAccessDateDesc(String aptApId, Long userId);

}
