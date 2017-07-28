package com.jaha.server.emaul.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.Vote;
import com.jaha.server.emaul.model.VoteGroup;
import com.jaha.server.emaul.model.VoteItem;
import com.jaha.server.emaul.model.VoteOfflineResult;
import com.jaha.server.emaul.model.VoteType;
import com.jaha.server.emaul.model.Voter;
import com.jaha.server.emaul.model.VoterOffline;
import com.jaha.server.emaul.model.VoterSecurity;
import com.jaha.server.emaul.repo.VoteItemRepository;
import com.jaha.server.emaul.repo.VoteOfflineResultRepository;
import com.jaha.server.emaul.repo.VoteRepository;
import com.jaha.server.emaul.repo.VoteTypeRepository;
import com.jaha.server.emaul.repo.VoterOfflineRepository;
import com.jaha.server.emaul.repo.VoterRepository;
import com.jaha.server.emaul.repo.VoterSecurityRepository;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SqlUtil;

/**
 * Created by doring on 15. 2. 25..
 */
// @SuppressWarnings("SpringJavaAutowiringInspection")
@Service
@Transactional(readOnly = true)
public class VoteServiceImpl implements VoteService {

    private static final String FILTER_ONGOING = "ongoing";
    private static final String FILTER_DONE = "done";

    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private VoteItemRepository voteItemRepository;
    @Autowired
    private VoteTypeRepository voteTypeRepository;
    @Autowired
    private VoterRepository voterRepository;
    @Autowired
    private VoterOfflineRepository voterOfflineRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private VoteOfflineResultRepository voteOfflineResultRepository;
    @Autowired
    private VoterSecurityRepository voterSecurityRepository;

    @Override
    public ScrollPage<Vote> getVotes(User user, Long lastVoteId) {
        return getVoteListInner("vote", user, lastVoteId, null);
    }

    @Override
    public ScrollPage<Vote> getVotes(User user, Long lastVoteId, String ongoingOrDone) {
        return getVoteListInner("vote", user, lastVoteId, ongoingOrDone);
    }

    @Override
    public ScrollPage<Vote> getPolls(User user, Long lastPollId) {
        return getVoteListInner("poll", user, lastPollId, null);
    }

    @Override
    public ScrollPage<Vote> getPolls(User user, Long lastPollId, String ongoingOrDone) {
        return getVoteListInner("poll", user, lastPollId, ongoingOrDone);
    }

    private ScrollPage<Vote> getVoteListInner(String voteMainType, User user, Long lastId, String ongoingOrDone) {
        if (lastId == null || lastId == 0l) {
            lastId = Long.MAX_VALUE;
        }
        ScrollPage<Vote> ret = new ScrollPage<>();
        List<VoteType> typeList = voteTypeRepository.findByMain(voteMainType);

        List<Long> typeIds = Lists.transform(typeList, input -> input.id);

        StringBuilder sb = new StringBuilder();
        for (Long typeId : typeIds) {
            sb.append(typeId);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        String filterOngoingOrDone = "";
        if (ongoingOrDone != null) {
            if (FILTER_ONGOING.equalsIgnoreCase(ongoingOrDone)) {
                filterOngoingOrDone = " AND v.status IN ('ready', 'active') ";
            } else if (FILTER_DONE.equalsIgnoreCase(ongoingOrDone)) {
                filterOngoingOrDone = " AND v.status = 'done' ";
            }
        }

        String filterTargetHo = "";
        if (!"0".equals(user.house.dong) || !"0".equals(user.house.ho)) {
            filterTargetHo = " AND v.json_array_target_ho LIKE IF(v.json_array_target_ho='', '%', '%\"" + user.house.ho + "\"%') ";
        }

        // ios와 기존 버전은 보안투표 노출하지 않음
        String filterKind = "";
        // if(user.kind != null && (user.kind.equals("ios") || user.kind.equals("old version"))){
        if (user.kind != null && user.kind.equals("old version")) {
            filterKind = " AND v.enable_security = 'N' ";
        }

        RowMapper<Vote> rowMapper = new RowMapper<Vote>() {
            @Override
            public Vote mapRow(ResultSet rs, int rowNum) throws SQLException {
                Vote ret = new Vote();
                VoteType voteType = new VoteType();
                voteType.main = rs.getString("main");
                voteType.sub = rs.getString("sub");

                ret.id = rs.getLong("id");
                ret.type = voteType;
                ret.status = rs.getString("status");
                ret.title = rs.getString("title");
                ret.description = rs.getString("description");
                ret.question = rs.getString("question");
                ret.targetApt = rs.getLong("target_apt");
                ret.targetDong = rs.getString("target_dong");
                ret.jsonArrayTargetHo = rs.getString("json_array_target_ho");
                ret.rangeAll = rs.getBoolean("range_all");
                ret.rangeSido = rs.getString("range_sido");
                ret.rangeSigungu = rs.getString("range_sigungu");
                ret.numberEnabled = rs.getBoolean("number_enabled");
                ret.houseLimited = rs.getBoolean("house_limited");
                ret.visible = rs.getBoolean("visible");
                ret.votersCount = rs.getLong("voters_count");
                ret.jsonArrayTargetUserTypes = rs.getString("json_array_target_user_types");
                ret.multipleChoice = rs.getBoolean("multiple_choice");
                ret.voteResultAvailable = rs.getBoolean("vote_result_available");
                ret.regDate = new Date(rs.getTimestamp("reg_date").getTime());
                ret.startDate = new Date(rs.getTimestamp("start_date").getTime());
                ret.endDate = new Date(rs.getTimestamp("end_date").getTime());
                ret.imageCount = rs.getInt("image_count");
                ret.file1 = rs.getString("file1");
                ret.file2 = rs.getString("file2");
                ret.items = Lists.newArrayList();
                ret.enableSecurity = rs.getString("enable_security");
                ret.securityLevel = rs.getString("security_level");
                ret.keyVoteEnc = rs.getString("key_vote_enc");
                ret.keyCheckEnc = rs.getString("key_check_enc");
                // 파일 확인 필수 여부 (Y/N)
                ret.fileCheckYn = rs.getString("file_check_yn");
                // 선거구
                ret.jsonArrayTargetGroup = rs.getString("json_array_target_group");

                return ret;
            }
        };

        // use_yn 조회 : AS-IS visible로 노출여부 사용, TO-BE : use_yn로 삭제여부 사용. (TO-BE에서 visible은 항상 1이다.)
        String sql = "SELECT v.*, t.main, t.sub, k.key_vote_enc, k.key_check_enc FROM vote v left outer join vote_key k " + "on v.vk_id = k.vk_id, vote_type t WHERE " + "v.type_id in ("
                + sb.toString() + ") AND v.type_id=t.id AND v.visible=1 AND v.use_yn = 'Y' AND ((v.target_apt=? AND " + "((v.target_dong=? " + filterTargetHo + " ) OR v.target_dong='')) OR "
                + "(v.range_sido=IF(v.range_sigungu='', ?, null) OR v.range_sigungu=?) OR v.range_all=1) AND " + "v.id < ? " + filterOngoingOrDone + filterKind + " ORDER BY v.reg_date DESC LIMIT 10";


        Object[] args = new Object[] {user.house.apt.id, user.house.dong, user.house.apt.address.시도명, user.house.apt.address.시군구명, lastId};

        if (user.type.admin || user.type.jaha) {
            sql = "SELECT v.*, t.main, t.sub, k.key_vote_enc, k.key_check_enc FROM vote v left outer join vote_key k on v.vk_id = k.vk_id, vote_type t WHERE " + "v.type_id in (" + sb.toString()
                    + ") AND v.type_id=t.id AND v.visible=1 AND (v.target_apt=? OR " + "(v.range_sido=IF(v.range_sigungu='', ?, null) OR v.range_sigungu=?) OR v.range_all=1) AND " + "v.id < ? "
                    + filterOngoingOrDone + filterKind + " ORDER BY v.reg_date DESC LIMIT 10";

            args = new Object[] {user.house.apt.id, user.house.apt.address.시도명, user.house.apt.address.시군구명, lastId};
        }

        List<Vote> temp = jdbcTemplate.query(sql, args, rowMapper);

        List<Long> voteIds = Lists.transform(temp, new Function<Vote, Long>() {
            @Override
            public Long apply(Vote input) {
                return input.id;
            }
        });

        if (voteIds.isEmpty()) {
            ret.setContent(Lists.newArrayList());
            return ret;
        }

        sb = new StringBuilder();
        for (Long voteId : voteIds) {
            sb.append(voteId);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        List<VoteItem> voteItems = jdbcTemplate.query("SELECT * FROM vote_item WHERE " + "parent_id in (" + sb.toString() + ")", new RowMapper<VoteItem>() {
            @Override
            public VoteItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                VoteItem ret = new VoteItem();
                ret.id = rs.getLong("id");
                ret.imageCount = rs.getInt("image_count");
                ret.commitment = rs.getString("commitment");
                ret.profile = rs.getString("profile");
                ret.isSubjective = rs.getBoolean("is_subjective");
                ret.parentId = rs.getLong("parent_id");
                ret.title = rs.getString("title");
                return ret;
            }
        });

        for (VoteItem voteItem : voteItems) {
            for (Vote vote : temp) {
                if (voteItem.parentId.equals(vote.id)) {
                    vote.items.add(voteItem);
                }
            }
        }

        ret.setContent(temp);
        final int listSize = temp.size();
        if (listSize >= 10) {
            ret.setNextPageToken(String.valueOf(temp.get(listSize - 1).id));
        }
        return ret;
    }



    /**
     * 선거구 등록내역 조회
     *
     * @param aptId
     * @param voteId
     * @return
     */
    @Override
    public List<VoteGroup> getVoteGroupList(Long aptId, List<Long> ids) {

        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            sb.append(id);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        List<VoteGroup> voteItems = jdbcTemplate.query("SELECT * FROM vote_group WHERE " + "id in (" + sb.toString() + ")", new RowMapper<VoteGroup>() {
            @Override
            public VoteGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
                VoteGroup voteGroup = new VoteGroup();
                voteGroup.id = rs.getLong("id");
                voteGroup.targetApt = rs.getLong("target_apt");
                voteGroup.name = rs.getString("name");
                voteGroup.jsonArrayTarget = rs.getString("json_array_target");
                voteGroup.description = rs.getString("description");
                voteGroup.votersCount = rs.getLong("voters_count");
                voteGroup.useYn = rs.getString("use_yn");
                voteGroup.groupType = rs.getString("group_type");
                voteGroup.userId = rs.getLong("user_id");
                return voteGroup;
            }
        });
        return voteItems;
    }


    @Override
    public List<Vote> getVoteList(Long aptId, Long vkId, Pageable pageable) {
        String query = "SELECT v.*, t.main, t.sub, k.key_vote_enc, k.key_check_enc FROM vote v left outer join vote_key k on v.vk_id = k.vk_id, vote_type t WHERE "
                + " v.type_id=t.id AND v.target_apt=?  AND " + " v.vk_id = ? " + SqlUtil.getPageSql(pageable);
        return jdbcTemplate.query(query, new Object[] {aptId, vkId}, new RowMapper<Vote>() {
            @Override
            public Vote mapRow(ResultSet rs, int rowNum) throws SQLException {
                Vote ret = new Vote();
                VoteType voteType = new VoteType();
                voteType.main = rs.getString("main");
                voteType.sub = rs.getString("sub");

                ret.id = rs.getLong("id");
                ret.type = voteType;
                ret.status = rs.getString("status");
                ret.title = rs.getString("title");
                ret.description = rs.getString("description");
                ret.question = rs.getString("question");
                ret.targetApt = rs.getLong("target_apt");
                ret.targetDong = rs.getString("target_dong");
                ret.jsonArrayTargetHo = rs.getString("json_array_target_ho");
                ret.rangeAll = rs.getBoolean("range_all");
                ret.rangeSido = rs.getString("range_sido");
                ret.rangeSigungu = rs.getString("range_sigungu");
                ret.numberEnabled = rs.getBoolean("number_enabled");
                ret.houseLimited = rs.getBoolean("house_limited");
                ret.visible = rs.getBoolean("visible");
                ret.votersCount = rs.getLong("voters_count");
                ret.jsonArrayTargetUserTypes = rs.getString("json_array_target_user_types");
                ret.multipleChoice = rs.getBoolean("multiple_choice");
                ret.voteResultAvailable = rs.getBoolean("vote_result_available");
                ret.regDate = new Date(rs.getTimestamp("reg_date").getTime());
                ret.startDate = new Date(rs.getTimestamp("start_date").getTime());
                ret.endDate = new Date(rs.getTimestamp("end_date").getTime());
                ret.imageCount = rs.getInt("image_count");
                ret.file1 = rs.getString("file1");
                ret.file2 = rs.getString("file2");
                ret.items = Lists.newArrayList();
                ret.enableSecurity = rs.getString("enable_security");
                ret.keyVoteEnc = rs.getString("key_vote_enc");
                ret.keyCheckEnc = rs.getString("key_check_enc");
                // 파일 확인 필수 여부 (Y/N)
                ret.fileCheckYn = rs.getString("file_check_yn");
                // 선거구
                ret.jsonArrayTargetGroup = rs.getString("json_array_target_group");
                return ret;
            }
        });
    }

    @Override
    public Vote getVote(Long voteId) {
        // return voteRepository.findOne(voteId);

        String query = "SELECT v.*, t.main, t.sub, k.key_vote_enc, k.key_check_enc FROM vote v left outer join vote_key k on v.vk_id = k.vk_id, vote_type t WHERE " + " v.type_id=t.id AND v.id = ?";

        List<Vote> voteList = jdbcTemplate.query(query, new Object[] {voteId}, new RowMapper<Vote>() {
            @Override
            public Vote mapRow(ResultSet rs, int rowNum) throws SQLException {
                Vote ret = new Vote();
                VoteType voteType = new VoteType();
                voteType.main = rs.getString("main");
                voteType.sub = rs.getString("sub");

                ret.id = rs.getLong("id");
                ret.type = voteType;
                ret.status = rs.getString("status");
                ret.title = rs.getString("title");
                ret.description = rs.getString("description");
                ret.question = rs.getString("question");
                ret.targetApt = rs.getLong("target_apt");
                ret.targetDong = rs.getString("target_dong");
                ret.jsonArrayTargetHo = rs.getString("json_array_target_ho");
                ret.rangeAll = rs.getBoolean("range_all");
                ret.rangeSido = rs.getString("range_sido");
                ret.rangeSigungu = rs.getString("range_sigungu");
                ret.numberEnabled = rs.getBoolean("number_enabled");
                ret.houseLimited = rs.getBoolean("house_limited");
                ret.visible = rs.getBoolean("visible");
                ret.votersCount = rs.getLong("voters_count");
                ret.jsonArrayTargetUserTypes = rs.getString("json_array_target_user_types");
                ret.multipleChoice = rs.getBoolean("multiple_choice");
                ret.voteResultAvailable = rs.getBoolean("vote_result_available");
                ret.regDate = new Date(rs.getTimestamp("reg_date").getTime());
                ret.startDate = new Date(rs.getTimestamp("start_date").getTime());
                ret.endDate = new Date(rs.getTimestamp("end_date").getTime());
                ret.imageCount = rs.getInt("image_count");
                ret.file1 = rs.getString("file1");
                ret.file2 = rs.getString("file2");
                ret.items = Lists.newArrayList();
                ret.enableSecurity = rs.getString("enable_security");
                ret.securityLevel = rs.getString("security_level");
                ret.keyVoteEnc = rs.getString("key_vote_enc");
                ret.keyCheckEnc = rs.getString("key_check_enc");
                // 파일 확인 필수 여부 (Y/N)
                ret.fileCheckYn = rs.getString("file_check_yn");
                // 선거구
                ret.jsonArrayTargetGroup = rs.getString("json_array_target_group");
                return ret;
            }
        });

        if (voteList == null || voteList.isEmpty()) {
            return null;
        } else {
            Vote vote = voteList.get(0);

            List<VoteItem> voteItems = jdbcTemplate.query("SELECT * FROM vote_item WHERE " + "parent_id = (" + vote.id + ")", new RowMapper<VoteItem>() {
                @Override
                public VoteItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                    VoteItem ret = new VoteItem();
                    ret.id = rs.getLong("id");
                    ret.imageCount = rs.getInt("image_count");
                    ret.commitment = rs.getString("commitment");
                    ret.profile = rs.getString("profile");
                    ret.isSubjective = rs.getBoolean("is_subjective");
                    ret.parentId = rs.getLong("parent_id");
                    ret.title = rs.getString("title");
                    return ret;
                }
            });

            for (VoteItem voteItem : voteItems) {
                vote.items.add(voteItem);
            }

            return vote;
        }
    }

    @Override
    public VoteItem getVoteItem(Long voteItemId) {
        return voteItemRepository.findOne(voteItemId);
    }

    @Override
    public List<VoteItem> getVoteItems(Long voteId) {
        return voteItemRepository.findByParentId(voteId);
    }

    @Override
    public VoteType getVoteType(String main, String sub) {
        return voteTypeRepository.findOneByMainAndSub(main, sub);
    }

    @Override
    public Boolean isAlreadyVoted(User user, Long voteId) {
        /** 임시코드0629 */
        Long jahaAptId = 1L;

        if (user.house.apt.id.equals(jahaAptId)) {
            return false;
        }
        /** 임시코드0629 */

        try {
            // 이투표에서 투표 시 사용자 아이디가 관리자 아이디가 들어가므로 다중 로우가 리턴될 수 있다.
            Voter voter = voterRepository.findOneByUserIdAndVoteId(user.id, voteId);
            return voter != null;
        } catch (Exception e) {
            List<Voter> voterList = voterRepository.findByUserIdAndVoteId(user.id, voteId);
            return voterList.get(0) != null;
        }

    }

    @Override
    public Boolean isAlreadyVotedHouse(User user, Long voteId) {
        /** 임시코드0629 */
        Long jahaAptId = 1L;

        if (user.house.apt.id.equals(jahaAptId)) {
            return false;
        }
        /** 임시코드0629 */

        Vote vote = voteRepository.findOne(voteId);
        if (!vote.houseLimited) {
            return false;
        }

        List<Voter> voters = voterRepository.findByVoteIdAndAptIdAndDongAndHo(voteId, user.house.apt.id, user.house.dong, user.house.ho);

        VoterOffline existOfflineVoter = voterOfflineRepository.findOneByVoteIdAndAptIdAndDongAndHo(voteId, user.house.apt.id, user.house.dong, user.house.ho);

        return (voters != null && !voters.isEmpty()) || existOfflineVoter != null;
    }

    @Override
    public String getVoteStatus(Long voteId) {
        Vote vote = voteRepository.findOne(voteId);
        return vote.status;
    }

    @Override
    @Transactional
    public Voter save(User user, Voter voter) {
        if (isAlreadyVoted(user, voter.vote.id) || isAlreadyVotedHouse(user, voter.vote.id)) {
            return null;
        }
        return voterRepository.saveAndFlush(voter);
    }

    @Override
    public List<Voter> getVoters(Long voteId) {
        return voterRepository.findByVoteId(voteId, new Sort(Sort.Direction.DESC, "voteDate"));
    }

    @Override
    public List<VoterOffline> getOfflineVoters(Long voteId) {
        return voterOfflineRepository.findByVoteId(voteId);
    }

    @Override
    public List<Voter> getVotersByItem(Long voteId, Long voteItemId) {
        return voterRepository.findByVoteIdAndVoteItemId(voteId, voteItemId, new Sort(Sort.Direction.DESC, "voteDate"));
    }

    @Override
    public List<VoterSecurity> getVoterSecurityByItem(Long voteId, Long voteItemId) {
        return voterSecurityRepository.findByVoteIdAndItemId(voteId, voteItemId, new Sort(Sort.Direction.DESC, "regDt"));
    }

    @Override
    public Long getOfflineVoteResultCount(Long voteId, Long voteItemId) {
        VoteOfflineResult result = voteOfflineResultRepository.findOne(voteId);
        if (result == null) {
            return 0l;
        }
        return result.getVoterCount(voteItemId);
    }

    @Override
    public VoteOfflineResult getOfflineVoteResult(Long voteId) {
        return voteOfflineResultRepository.findOne(voteId);
    }

}
