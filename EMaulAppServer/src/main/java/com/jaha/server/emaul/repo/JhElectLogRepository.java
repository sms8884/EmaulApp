package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.JhElectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2015-06-28.
 */
@Repository
public interface JhElectLogRepository extends JpaRepository<JhElectLog, Long> {

}
