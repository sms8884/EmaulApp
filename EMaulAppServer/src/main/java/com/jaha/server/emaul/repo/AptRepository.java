package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.Address;
import com.jaha.server.emaul.model.Apt;

/**
 * Created by doring on 15. 3. 31..
 */
@Repository
public interface AptRepository extends JpaRepository<Apt, Long> {
    List<Apt> findByNameContainingAndVirtualFalse(String name);

    List<Apt> findByNameContainingAndRegisteredAptIsTrueAndVirtualFalse(String name);

    Apt findByAddress(Address address);
}
