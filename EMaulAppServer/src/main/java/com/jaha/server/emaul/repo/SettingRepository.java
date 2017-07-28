package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by doring on 15. 4. 28..
 */
@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

}
