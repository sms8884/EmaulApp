package com.jaha.server.emaul.repo;

import com.jaha.server.emaul.model.VoterSecurity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface VoterSecurityRepository extends JpaRepository<VoterSecurity, String> {
    List<VoterSecurity> findByVoteIdAndItemId(Long voteId, Long itemId, Sort sort);
}
