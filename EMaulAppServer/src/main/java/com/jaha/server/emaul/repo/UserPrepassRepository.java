package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.UserPrepass;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by doring on 15. 5. 24..
 */
public interface UserPrepassRepository extends JpaRepository<UserPrepass, Long> {
    UserPrepass findOneByFullNameAndPhoneAndAptIdAndDongAndHo(String fullName, String phone, Long aptId, String dong, String ho);
}
