package com.jaha.server.emaul.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.BoardPost;

/**
 * Created by doring on 15. 3. 9..
 */
@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    List<BoardPost> findFirst16ByCategoryIdIn(List<Long> categoryIds, Sort sort);

    List<BoardPost> findFirst16ByCategoryIdInAndDisplayYn(List<Long> categoryIds, Sort sort, String displayYn);

    @Modifying
    @Transactional
    @Query("UPDATE BoardPost p SET p.blocked = ?2 WHERE p.id = ?1")
    int setBlocked(Long postId, Boolean block);

    BoardPost findByIdAndDisplayYn(long postId, String displayYn);

    @Modifying
    @Transactional
    @Query(value = "UPDATE board_post SET comment_count = comment_count + 1 WHERE id = :id", nativeQuery = true)
    int updateCommentCountPlus(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE board_post SET comment_count = comment_count - 1 WHERE id = :id", nativeQuery = true)
    int updateCommentCountMinus(@Param("id") Long id);

}
