package com.jaha.server.emaul.service;

import com.jaha.server.emaul.model.*;
import com.jaha.server.emaul.util.ScrollPage;
import org.springframework.data.domain.Pageable;
import com.jaha.server.emaul.model.VoterSecurity;

import java.util.List;

/**
 * Created by doring on 15. 2. 25..
 */
public interface VoteKeyService {
    List<VoteKey> getVoteKeyList(Long aptId, Pageable pageable);
    List<VoteKey> getVoteKeyList(Long aptId, String adminName, String adminEmail, Pageable pageable);
    List<VoterSecurity> getVoterSecurityList(Long voteId, Pageable pageable);
}
