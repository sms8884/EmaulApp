package com.jaha.server.emaul.service;

import com.jaha.server.emaul.model.JhElectLog;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.Vote;
import com.jaha.server.emaul.model.Voter;
import com.jaha.server.emaul.model.json.DongHo;
import com.jaha.server.emaul.util.ScrollPage;

import java.util.List;

/**
 * Created by Administrator on 2015-06-28.
 */
public interface JhElectLogService {
    public JhElectLog save(JhElectLog log);
    public List<DongHo> getDongHo(int aptId);
    public ScrollPage<Vote> getVoteListInner(String voteMainType, User user, Long lastId, String ongoingOrDone,
                                             String dong, String ho);

    public boolean isAlreadyVotedHouse(User user, Long voteID);
    public boolean save(User user, Voter voter);

    public boolean isAlreadyVotedHouse(Long aptId, String dong, String ho, Long voteID);
    public boolean save(Long aptId, String dong, String ho, Voter voter);

    public String getHouseHostFromDongHo(Long aptId, String Dong, String ho);
}
