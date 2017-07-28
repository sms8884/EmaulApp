package com.jaha.server.emaul.model.json;

import com.jaha.server.emaul.model.Vote;

import java.util.Map;

/**
 * Created by doring on 15. 5. 26..
 */
public class VoteResult {
    public Vote vote;

    // voteItemId, count
    public Map<Long, Integer> onlineResultMap;
    public Map<Long, Integer> offlineResultMap;

    public String resultText;
}
