package com.jaha.server.emaul.repo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.BoardCommentReply;

/**
 * Created by doring on 2015. 7. 2..
 */
@Repository
public interface BoardCommentReplyRepository extends JpaRepository<BoardCommentReply, Long> {

    List<BoardCommentReply> findFirst20ByCommentIdAndIdLessThan(Long commentId, Long lastCommentReplyId, Sort sort);

    List<BoardCommentReply> findFirst20ByCommentIdAndIdLessThanAndDisplayYn(Long commentId, Long lastCommentReplyId, String displayYn, Sort sort);

    @Modifying
    @Transactional
    @Query("UPDATE BoardCommentReply p SET p.blocked = ?2 WHERE p.id = ?1")
    int setBlocked(Long replyId, Boolean block);

    @Modifying
    @Transactional
    @Query(value = "UPDATE board_comment_reply SET display_yn = 'N', req_ip = :reqIp, mod_id = :modId, mod_date = NOW() WHERE id = :id", nativeQuery = true)
    int updateCommentReplyHide(@Param("id") Long id, @Param("reqIp") String reqIp, @Param("modId") Long modId);

}
