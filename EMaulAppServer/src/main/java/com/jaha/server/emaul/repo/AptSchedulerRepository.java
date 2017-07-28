package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.AptScheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by shavrani on 16. 05. 11..
 */
@Repository
public interface AptSchedulerRepository extends JpaRepository<AptScheduler, Long> {

    public AptScheduler findByIdAndAptId(Long id, Long aptId);
    
}
