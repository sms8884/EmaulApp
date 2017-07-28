package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.BoardEmpathy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by doring on 15. 6. 29..
 */
@Repository
public interface BoardEmpathyRepository extends JpaRepository<BoardEmpathy, Long> {
    BoardEmpathy findOneByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    List<BoardEmpathy> findByUserIdAndPostId(Long userId, Long postId);
}
