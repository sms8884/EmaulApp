package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.TrafficCache;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by doring on 15. 5. 15..
 */
public interface TrafficRepository extends JpaRepository<TrafficCache, String> {

}
