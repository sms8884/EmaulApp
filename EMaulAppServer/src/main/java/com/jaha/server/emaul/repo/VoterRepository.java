package com.jaha.server.emaul.repo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jaha.server.emaul.model.Voter;

/**
 * Created by doring on 15. 3. 9..
 */
@Repository
public interface VoterRepository extends JpaRepository<Voter, Long> {
    Voter findOneByUserIdAndVoteId(Long userId, Long voteId);

    List<Voter> findByUserIdAndVoteId(Long userId, Long voteId);

    Voter findOneByUserIdAndVoteIdAndVoteItemId(Long userId, Long voteId, Long voteItemId);

    List<Voter> findByVoteId(Long voteId, Sort sort);

    List<Voter> findByVoteIdAndVoteItemId(Long voteId, Long voteItemId, Sort sort);

    List<Voter> findByVoteIdAndAptIdAndDongAndHo(Long voteId, Long aptId, String dong, String ho);


}
