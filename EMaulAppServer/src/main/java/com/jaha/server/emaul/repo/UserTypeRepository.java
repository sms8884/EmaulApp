package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by doring on 15. 3. 10..
 */
@Repository
public interface UserTypeRepository extends JpaRepository<UserType, Long> {
}
