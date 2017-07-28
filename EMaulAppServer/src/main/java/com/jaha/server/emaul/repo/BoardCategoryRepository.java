package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.BoardCategory;

/**
 * Created by doring on 15. 3. 9..
 */
@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
    // List<BoardCategory> findByTypeAndAptId(String type, Long aptId, Sort sort);
    List<BoardCategory> findByTypeAndAptIdAndDelYn(String type, Long aptId, String delYn, Sort sort);

    // List<BoardCategory> findByTypeAndAptId(String type, Long aptId);
    List<BoardCategory> findByTypeAndAptIdAndDelYn(String type, Long aptId, String delYn);

    // List<BoardCategory> findByType(String type, Sort sort);
    List<BoardCategory> findByTypeAndDelYn(String type, String delYn, Sort sort);
}
