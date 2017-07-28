package com.jaha.server.emaul.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Map;

/**
 * Created by doring on 15. 5. 26..
 */
@Entity
public class VoteOfflineResult {
    @Id
    public Long voteId;

    @Column(nullable = false, length = 200)
    public String resultText;

    @Column(nullable = false, length = 1000, columnDefinition = "VARCHAR(1000) NOT NULL DEFAULT ''")
    public String jsonMapVoteItemResult;

    public Long aptId;
    public Long regUserId;
    public Date regDate;

    @Transient
    public Long getVoterCount(Long voteItemId) {
        if (jsonMapVoteItemResult == null || jsonMapVoteItemResult.isEmpty()) {
            return 0l;
        }
        Map<Long, Long> map = new Gson().fromJson(jsonMapVoteItemResult, new TypeToken<Map<Long, Long>>() {
        }.getType());
        return map.get(voteItemId);
    }
}
