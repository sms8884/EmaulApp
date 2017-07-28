package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.AptFeeAvr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by doring on 15. 5. 4..
 */
@Repository
public interface AptFeeAvrRepository extends JpaRepository<AptFeeAvr, Long> {
    AptFeeAvr findOneByAptIdAndDateAndHouseSize(Long aptId, String date, String houseSize);
}
