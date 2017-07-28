package com.jaha.server.emaul.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.ApAccessLocationLog;

/**
 * Created by shavrani on 16. 05. 11..
 */
@Repository
public interface ApAccessLocationLogRepository extends JpaRepository<ApAccessLocationLog, Long> {

    ApAccessLocationLog findByApIdAndAccessDeviceIdAndDisAppearIsNull(Long aptApId, Long accessDeviceId);

    ApAccessLocationLog findByApIdAndUserIdAndDisAppearIsNull(Long aptApId, Long userId);

}
