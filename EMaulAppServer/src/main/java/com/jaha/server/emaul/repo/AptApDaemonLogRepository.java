package com.jaha.server.emaul.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.AptApDaemonLog;

/**
 * Created by shavrani on 16. 09. 01..
 */
@Repository
public interface AptApDaemonLogRepository extends JpaRepository<AptApDaemonLog, Long> {
}
