package com.jaha.server.emaul.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jaha.server.emaul.model.*;
import com.jaha.server.emaul.repo.*;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SqlUtil;
import com.jaha.server.emaul.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by doring on 15. 2. 25..
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
@Transactional(readOnly = true)
public class VoteKeyServiceImpl implements VoteKeyService {


    @Autowired
    private JdbcTemplate mJdbc;

    @Autowired
    private VoteKeyRepository voteRepository;

    @Override
    public List<VoteKey> getVoteKeyList(Long aptId, Pageable pageable) {

        /*
        String query =
                " SELECT *,                                                 \n"+
                        "       case when isnull(start_dt) || isnull(end_dt) then 1 \n"+
                        "            when now() < date(start_dt)             then 2 \n"+
                        "            when date(now())>=date(start_dt) and           \n"+
                        "                 date(now())<=date(end_dt)          then 3 \n"+
                        "            when now() > date(end_dt)               then 4 \n"+
                        "            else 0                                         \n"+
                        "          end as key_status                                 \n"+
                        "   FROM vote_key                                           \n"+
                        "  WHERE apt_id = ?                                        \n" +
                        SqlUtil.getPageSql(pageable);
        */
        String query =
                " SELECT *,                                                 \n"+
                        "       case when isnull(start_dt) || isnull(end_dt) then 1 \n"+
                        "            when now() < start_dt                   then 2 \n"+
                        "            when now() >= start_dt and                     \n"+
                        "                 now() <= end_dt                    then 3 \n"+
                        "            when now() > end_dt                     then 4 \n"+
                        "            else 0                                         \n"+
                        "          end as key_status                                 \n"+
                        "   FROM vote_key                                           \n"+
                        "  WHERE apt_id = ?                                        \n" +
                        SqlUtil.getPageSql(pageable);
        // Logger.debug(query);
        return
                mJdbc.query(query, new Object[]{aptId}, new RowMapper<VoteKey>() {
                    @Override
                    public VoteKey mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VoteKey cp = new VoteKey();
                        cp.vkId = rs.getLong("vk_id");
                        cp.aptId = rs.getLong("apt_id");
                        cp.keyName = rs.getString("key_name");
                        cp.startDt = rs.getDate("start_dt");
                        cp.endDt = rs.getDate("end_dt");
                        cp.useYn = rs.getString("use_yn");
                        cp.adminName = rs.getString("admin_name");
                        cp.adminEmail = rs.getString("admin_email");
                        cp.keyVoteDec = rs.getString("key_vote_dec");
                        cp.keyVoteEnc = rs.getString("key_vote_enc");
                        cp.keyCheckDec = rs.getString("key_check_dec");
                        cp.keyCheckEnc = rs.getString("key_check_enc");

                        cp.keyBase1 = rs.getString("key_base1");
                        cp.keyBase2 = rs.getString("key_base2");
                        cp.keyBase3 = rs.getString("key_base3");

                        cp.keyBase1Uname = rs.getString("key_base1_uname");
                        cp.keyBase2Uname = rs.getString("key_base2_uname");
                        cp.keyBase3Uname = rs.getString("key_base3_uname");


                        cp.createSignFname = rs.getString("create_sign_fname");
                        cp.grantSignFname = rs.getString("grant_sign_fname");
                        cp.checkSignFname = rs.getString("check_sign_fname");

                        cp.keyGrantDec = rs.getString("key_grant_dec");
                        cp.keyGrantYn = rs.getString("key_grant_yn");

                        cp.regDt = rs.getString("reg_dt");
                        cp.regTm = rs.getString("reg_tm");

                        cp.uptDt = rs.getDate("upt_dt");
                        cp.keyLevel = rs.getString("key_level");
                        cp.keyStatus = rs.getInt("key_status");
                        return cp;
                    }
                });

    }

    @Override
    public List<VoteKey> getVoteKeyList(Long aptId, String adminName, String adminEmail, Pageable pageable) {

        String query =
                " SELECT *,                                                 \n"+
                        "       case when isnull(start_dt) || isnull(end_dt) then 1 \n"+
                        "            when now() < start_dt                   then 2 \n"+
                        "            when now() >= start_dt and                     \n"+
                        "                 now() <= end_dt                    then 3 \n"+
                        "            when now() > end_dt                     then 4 \n"+
                        "            else 0                                         \n"+
                        "          end as key_status                                 \n"+
                        "   FROM vote_key                                           \n"+
                        "  WHERE apt_id = ?                                        \n" +
                        "  AND admin_name = ?                                        \n" +
                        "  AND admin_email = ?                                        \n" +
                        SqlUtil.getPageSql(pageable);
        return
                mJdbc.query(query, new Object[]{aptId, adminName, adminEmail}, new RowMapper<VoteKey>() {
                    @Override
                    public VoteKey mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VoteKey cp = new VoteKey();
                        cp.vkId = rs.getLong("vk_id");
                        cp.aptId = rs.getLong("apt_id");
                        cp.keyName = rs.getString("key_name");
                        cp.startDt = rs.getDate("start_dt");
                        cp.endDt = rs.getDate("end_dt");
                        cp.useYn = rs.getString("use_yn");
                        cp.adminName = rs.getString("admin_name");
                        cp.adminEmail = rs.getString("admin_email");
                        cp.keyVoteDec = rs.getString("key_vote_dec");
                        cp.keyVoteEnc = rs.getString("key_vote_enc");
                        cp.keyCheckDec = rs.getString("key_check_dec");
                        cp.keyCheckEnc = rs.getString("key_check_enc");

                        cp.keyBase1 = rs.getString("key_base1");
                        cp.keyBase2 = rs.getString("key_base2");
                        cp.keyBase3 = rs.getString("key_base3");

                        cp.keyBase1Uname = rs.getString("key_base1_uname");
                        cp.keyBase2Uname = rs.getString("key_base2_uname");
                        cp.keyBase3Uname = rs.getString("key_base3_uname");


                        cp.createSignFname = rs.getString("create_sign_fname");
                        cp.grantSignFname = rs.getString("grant_sign_fname");
                        cp.checkSignFname = rs.getString("check_sign_fname");

                        cp.keyGrantDec = rs.getString("key_grant_dec");
                        cp.keyGrantYn = rs.getString("key_grant_yn");

                        cp.regDt = rs.getString("reg_dt");
                        cp.regTm = rs.getString("reg_tm");

                        cp.uptDt = rs.getDate("upt_dt");
                        cp.keyLevel = rs.getString("key_level");
                        cp.keyStatus = rs.getInt("key_status");
                        return cp;
                    }
                });

    }

    @Override
    public List<VoterSecurity> getVoterSecurityList(Long voteId, Pageable pageable) {

        String query =
                " SELECT *                                                 \n"+
                        "   FROM voter_security                             \n"+
                        "  WHERE vote_id = ?                                \n" +
                        SqlUtil.getPageSql(pageable);
        return
                mJdbc.query(query, new Object[]{voteId}, new RowMapper<VoterSecurity>() {
                    @Override
                    public VoterSecurity mapRow(ResultSet rs, int rowNum) throws SQLException {
                        VoterSecurity cp = new VoterSecurity();
                        cp.viId = rs.getString("vi_id");
                        cp.voteId = rs.getLong("vote_id");
                        cp.itemIdEnc = rs.getString("item_id_enc");
                        cp.itemId = rs.getLong("item_id");
                        cp.itemIdChkFname = rs.getString("item_id_chk_fname");
                        cp.regDt = rs.getString("reg_dt");
                        return cp;
                    }
                });
    }
}
