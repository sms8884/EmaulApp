package com.jaha.server.emaul.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.Vote;
import com.jaha.server.emaul.model.VoteGroup;
import com.jaha.server.emaul.model.VoteItem;
import com.jaha.server.emaul.model.VoteOfflineResult;
import com.jaha.server.emaul.model.VoteType;
import com.jaha.server.emaul.model.Voter;
import com.jaha.server.emaul.model.VoterOffline;
import com.jaha.server.emaul.model.VoterSecurity;
import com.jaha.server.emaul.util.ScrollPage;

/**
 * Created by doring on 15. 2. 25..
 */
public interface VoteService {
    ScrollPage<Vote> getVotes(User user, Long lastVoteId);

    ScrollPage<Vote> getVotes(User user, Long lastVoteId, String ongoingOrDone);

    ScrollPage<Vote> getPolls(User user, Long lastPollId);

    ScrollPage<Vote> getPolls(User user, Long lastPollId, String ongoingOrDone);

    List<Vote> getVoteList(Long aptId, Long vkId, Pageable pageable);

    Vote getVote(Long voteId);

    VoteItem getVoteItem(Long voteItemId);

    List<VoteItem> getVoteItems(Long voteId);

    VoteType getVoteType(String main, String sub);

    Boolean isAlreadyVoted(User user, Long voteId);

    Boolean isAlreadyVotedHouse(User user, Long voteId);

    String getVoteStatus(Long voteId);

    Voter save(User user, Voter voter);

    List<Voter> getVoters(Long voteId);

    List<VoterOffline> getOfflineVoters(Long voteId);

    List<Voter> getVotersByItem(Long voteId, Long voteItemId);

    List<VoterSecurity> getVoterSecurityByItem(Long voteId, Long voteItemId);

    Long getOfflineVoteResultCount(Long voteId, Long voteItemId);

    VoteOfflineResult getOfflineVoteResult(Long voteId);

    // 등록된 선거구 조회
    List<VoteGroup> getVoteGroupList(Long aptId, List<Long> ids);

}
