package com.jaha.server.emaul.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.jaha.server.emaul.model.*;
import com.jaha.server.emaul.model.json.DongHo;
import com.jaha.server.emaul.repo.*;
import com.jaha.server.emaul.util.ScrollPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Created by Administrator on 2015-06-28.
 */

@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
@Transactional(readOnly = true)

public class JhElectLogServiceImpl implements JhElectLogService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Autowired
    private JhElectLogRepository jhElectLogRepository;

    @Autowired
    private VoteTypeRepository voteTypeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private VoterRepository voterRepository;


    @Override
    @Transactional
    public JhElectLog save(JhElectLog log) {
        return jhElectLogRepository.saveAndFlush(log);
    }

    private static final String FILTER_ONGOING = "ongoing";
    private static final String FILTER_DONE = "done";


    /**
     * 아파트 ID를 가지고 동,호를 가져옴.
     * @param aptId
     * @return
     */
    public List<DongHo> getDongHo(int aptId){
        String query=
                String.format(
                "SELECT dong,ho FROM  house \n" +
                " WHERE apt_id = %d          \n" +
                " ORDER BY dong asc, ho asc \n",aptId);

        List<DongHo> item = jdbcTemplate.query(query, new RowMapper<DongHo>() {

            @Override
            public DongHo mapRow(ResultSet rs, int rowNum) throws SQLException {
                DongHo ret = new DongHo();
                ret.dong = rs.getString("dong");
                ret.ho = rs.getString("ho");
                return ret;
            }
        });

        return item;
    }


    /**
     * VoteServiceImpl.getVoteListInner 과 로직은 같으나
     * 동,호 구분으로 선거 리스트를 가져온다.
     * @param voteMainType: 'vote': 선거, 'poll':설문
     * @param user
     * @param lastId
     * @param ongoingOrDone
     * @return
     */
    @Override
    public ScrollPage<Vote>
    getVoteListInner(String voteMainType, User user, Long lastId, String ongoingOrDone,String dong,String ho) {

        if ( !(lastId != null &&  lastId != 0l)) {
            lastId = Long.MAX_VALUE;
        }

        ScrollPage<Vote> ret = new ScrollPage<>();

        String filterOngoingOrDone = "";
        if (ongoingOrDone != null) {
            if (FILTER_ONGOING.equalsIgnoreCase(ongoingOrDone)) {
                filterOngoingOrDone = " AND v.status IN ('active') ";
            } else if (FILTER_DONE.equalsIgnoreCase(ongoingOrDone)) {
                filterOngoingOrDone = " AND v.status = 'done' ";
            }
        }

        /*
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
                ret.items = Lists.newArrayList();
                return ret;
            }
        };
        */
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
                ret.items = Lists.newArrayList();
                ret.enableSecurity = rs.getString("enable_security");
                ret.keyVoteEnc = rs.getString("key_vote_enc");
                ret.keyCheckEnc = rs.getString("key_check_enc");
                return ret;
            }
        };

        /*
        String sql =String.format(
        "SELECT v.*, t.main, t.sub                                                                                 \n"+
        "  FROM vote v, vote_type t                                                                                \n"+
        "  WHERE v.type_id in (select id from vote_type where main='vote')                                         \n"+
        "   AND v.type_id=t.id                                                                                     \n"+
        "   AND v.visible=1                                                                                        \n"+
        "   %s \n"+
        "   AND 1 = ifnull(                                                                                        \n"+
        "          (CASE WHEN range_all = 1 THEN 1                                                                 \n"+
        "                ELSE CASE WHEN v.range_sido='' OR v.range_sido = ?                                       \n"+
        "						  THEN CASE WHEN v.range_sigungu= ''  OR v.range_sigungu= ?                       \n"+
        "                                    THEN CASE WHEN v.target_apt= '' OR v.target_apt= ?                    \n"+
        "                                              THEN CASE WHEN v.target_dong= '' OR v.target_dong= ?       \n"+
        "                                                        THEN CASE WHEN v.json_array_target_ho = ''        \n"+
        "                                                                    OR v.json_array_target_ho like '%%%s%%'  \n"+
        "                                                                  THEN 1                                  \n"+
        "															  END                                          \n"+
        "													END                                                    \n"+
        "										END                                                                \n"+
        "							    END                                                                        \n"+
        "					   END                                                                                 \n"+
        "		   END),0)                                                                                         \n"+
        "     AND v.id < ? \n"+
        "ORDER BY  v.reg_date  ASC                                                                                 ",
                filterOngoingOrDone,ho
        );
        */
        String sql =String.format(
                " SELECT v.*, t.main, t.sub , k.key_vote_enc, k.key_check_enc                                                                           \n"+
                        "   FROM vote v                                                                                         \n"+
                        "  INNER JOIN vote_type t   \n"+
                        "     ON v.type_id=t.id                                                                                    \n"+
                        "    AND v.type_id in (select id from vote_type where main='vote')  \n "+
                        "   LEFT OUTER JOIN vote_key k                                                                             \n" +
                        "     ON v.vk_id = k.vk_id                                                                                 \n" +
                        "  WHERE                                                                                                    \n" +
                        "        v.visible=1                                                                                        \n"+
                        "   %s \n"+
                        "   AND 1 = ifnull(                                                                                        \n"+
                        "          (CASE WHEN range_all = 1 THEN 1                                                                 \n"+
                        "                ELSE CASE WHEN v.range_sido='' OR v.range_sido = ?                                       \n"+
                        "						  THEN CASE WHEN v.range_sigungu= ''  OR v.range_sigungu= ?                       \n"+
                        "                                    THEN CASE WHEN v.target_apt= '' OR v.target_apt= ?                    \n"+
                        "                                              THEN CASE WHEN v.target_dong= '' OR v.target_dong= ?       \n"+
                        "                                                        THEN CASE WHEN v.json_array_target_ho = ''        \n"+
                        "                                                                    OR v.json_array_target_ho like '%%%s%%'  \n"+
                        "                                                                  THEN 1                                  \n"+
                        "															  END                                          \n"+
                        "													END                                                    \n"+
                        "										END                                                                \n"+
                        "							    END                                                                        \n"+
                        "					   END                                                                                 \n"+
                        "		   END),0)                                                                                         \n"+
                        "     AND v.id < ? \n"+
                        "ORDER BY  v.reg_date  ASC                                                                                 ",
                filterOngoingOrDone,ho
        );

       // Debug.sql(sql);
        Object[] args = new Object[]{user.house.apt.address.시도명,user.house.apt.address.시군구명,user.house.apt.id, dong, lastId};
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

        StringBuilder sb = new StringBuilder();
        for (Long voteId : voteIds) {
            sb.append(voteId);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        List<VoteItem> voteItems = jdbcTemplate.query("SELECT * FROM vote_item WHERE " +
                "parent_id in (" + sb.toString() + ")", new RowMapper<VoteItem>() {
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
     * 투표 확인
     * @param user
     * @param voteId
     * @return
     */
    public boolean isAlreadyVotedHouse(User user,Long voteId) {
        String sql =
                "select count(*) as cnt from voter \n" +
                " where apt_id = ?        \n" +
                "   and vote_id = ?         \n" +
                "   and dong = ?          \n" +
                "   and ho = ?            \n";

        List<Integer> res1 = jdbcTemplate.query(sql,
                new Object[]{user.house.apt.id, voteId, user.house.dong, user.house.ho},
                new RowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("cnt");
                    }
                }
        );

        if(res1.get(0) >0){
            return true;
        }

         sql =
                "select count(*) as cnt from voter_offline \n" +
                " where apt_id = ?        \n" +
                "   and vote_id = ?         \n" +
                "   and dong = ?          \n" +
                "   and ho = ?            \n";

        List<Integer> res2 = jdbcTemplate.query(sql,
                new Object[]{user.house.apt.id, voteId, user.house.dong, user.house.ho},
                new RowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("cnt");
                    }
                });

        return res2.get(0) > 0? true:false;
    }


    /**
     * 투표 내용 저장.
     * @param user
     * @param voter
     * @return
     */
    @Override
    @Transactional
    public boolean save(User user, Voter voter) {
        if (isAlreadyVotedHouse(user, voter.vote.id)) {
            return false;
        }
        return (voterRepository.saveAndFlush(voter) !=null) ? true:false;
    }

    /**
     * 투표 확인
     * @param aptId
     * @param dong
     * @param ho
     * @param voteId
     * @return
     */
    public boolean isAlreadyVotedHouse(Long aptId, String dong, String ho, Long voteId) {
        String sql =
                "select count(*) as cnt from voter \n" +
                        " where apt_id = ?        \n" +
                        "   and vote_id = ?         \n" +
                        "   and dong = ?          \n" +
                        "   and ho = ?            \n";

        List<Integer> res1 = jdbcTemplate.query(sql,
                new Object[]{aptId, voteId, dong, ho},
                new RowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("cnt");
                    }
                }
        );

        if(res1.get(0) >0){
            return true;
        }

        sql =
                "select count(*) as cnt from voter_offline \n" +
                        " where apt_id = ?        \n" +
                        "   and vote_id = ?         \n" +
                        "   and dong = ?          \n" +
                        "   and ho = ?            \n";

        List<Integer> res2 = jdbcTemplate.query(sql,
                new Object[]{aptId, voteId, dong, ho},
                new RowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getInt("cnt");
                    }
                });

        return res2.get(0) > 0? true:false;
    }


    /**
     * 투표 내용 저장.
     * @param aptId
     * @param dong
     * @param ho
     * @param voter
     * @return
     */
    @Override
    @Transactional
    public boolean save(Long aptId, String dong, String ho, Voter voter) {
        if (isAlreadyVotedHouse(aptId, dong, ho, voter.vote.id)) {
            return false;
        }
        return (voterRepository.saveAndFlush(voter) !=null) ? true:false;
    }

    /**
     * 세대주 이름 가져옴.
     * @param aptId
     * @param dong
     * @param ho
     * @return
     */
    @Override
    public String getHouseHostFromDongHo(Long aptId, String dong, String ho) {

        String sql =
                " select full_name          \n"+
                "   from house a            \n"+
                "       ,user b             \n"+
                "       ,user_type c        \n"+
                "  where a.apt_id = ?       \n"+
                "    and a.dong=?           \n"+
                "    and a.ho = ?           \n"+
                "    and a.id = b.house_id  \n"+
                "    and b.id = c.user_id   \n"+
                "    and house_host = 1     \n";
        ;
        List<String> res = jdbcTemplate.query(sql,new Object[]{aptId, dong, ho},
                new RowMapper<String>() {
                    @Override
                    public String  mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString("full_name");
                    }
        });

        logger.debug("getHouseHostFromDongHo:" + res.size());
        return res.isEmpty() ? "":res.get(0);
    }
}
