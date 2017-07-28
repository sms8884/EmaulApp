package com.jaha.server.emaul.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.AptApAccessDevice;

/**
 * Created by shavrani on 16-08-11
 */
@Repository
public interface AptApAccessDeviceRepository extends JpaRepository<AptApAccessDevice, Long> {

    AptApAccessDevice findByAccessKeyAndDeactiveDateIsNull(String accessKey);
}
