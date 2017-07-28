package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.Hashtag;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    @Query(value = "SELECT id, name FROM board_post_hashtag WHERE post_id = :postId", nativeQuery = true)
    List<Hashtag> findByPostId(@Param("postId") Long postId) throws DataAccessException;
}
