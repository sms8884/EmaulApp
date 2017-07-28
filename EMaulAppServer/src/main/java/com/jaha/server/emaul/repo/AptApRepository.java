package com.jaha.server.emaul.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.AptAp;

/**
 * Created by shavrani on 16-06-17
 */
@Repository
public interface AptApRepository extends JpaRepository<AptAp, Long> {

    AptAp findById(Long id);

    AptAp findByApBeaconUuid(String apBeaconUuid);

    AptAp findByApBeaconUuidAndDeactiveDateIsNull(String apBeaconUuid);

}
