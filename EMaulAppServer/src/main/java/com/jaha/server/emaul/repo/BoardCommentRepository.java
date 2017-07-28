package com.jaha.server.emaul.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.BoardComment;

/**
 * Created by doring on 15. 3. 9..
 */
@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    List<BoardComment> findFirst20ByPostIdAndIdLessThan(Long postId, Long lastCommentId, Sort sort);

    List<BoardComment> findFirst20ByPostIdAndIdLessThanAndDisplayYn(Long postId, Long lastCommentId, String displayYn, Sort sort);

    @Modifying
    @Transactional
    @Query("UPDATE BoardComment p SET p.blocked = ?2 WHERE p.id = ?1")
    int setBlocked(Long commentId, Boolean block);

    @Modifying
    @Transactional
    @Query(value = "UPDATE board_comment SET display_yn = 'N', req_ip = :reqIp, mod_id = :modId, mod_date = NOW() WHERE id = :id", nativeQuery = true)
    int updateCommentHide(@Param("id") Long id, @Param("reqIp") String reqIp, @Param("modId") Long modId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE board_comment SET reply_count = reply_count + 1 WHERE id = :id", nativeQuery = true)
    int updateReplyCountPlus(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE board_comment SET reply_count = reply_count - 1 WHERE id = :id", nativeQuery = true)
    int updateReplyCountMinus(@Param("id") Long id);

}
