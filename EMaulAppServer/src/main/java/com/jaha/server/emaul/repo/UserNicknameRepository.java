package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jaha.server.emaul.model.UserNickname;

/**
 * Created by doring on 15. 3. 16..
 */
public interface UserNicknameRepository extends JpaRepository<UserNickname, String> {
    List<UserNickname> findByNameStartingWith(String name);
}
