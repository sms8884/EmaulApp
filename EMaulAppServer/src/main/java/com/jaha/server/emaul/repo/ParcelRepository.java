package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jaha.server.emaul.model.ParcelNotification;

/**
 * Created by doring on 15. 5. 20..
 */
public interface ParcelRepository extends JpaRepository<ParcelNotification, Long> {
    List<ParcelNotification> findFirst20ByAptIdAndIdLessThan(Long aptId, Long lastItemId, Sort sort);

    List<ParcelNotification> findFirst20ByAptIdAndDongAndHoAndVisibleIsTrueAndIdLessThan(Long aptId, String dong, String ho, Long lastItemId, Sort sort);
}
