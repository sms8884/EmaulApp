package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by doring on 15. 4. 14..
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, String>{
}
