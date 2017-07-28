package com.jaha.server.emaul.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.jaha.server.emaul.mapper.CommonMapper;
import com.jaha.server.emaul.model.BaseSecuModel;
import com.jaha.server.emaul.util.RandomKeys;

/**
 * Created by doring on 15. 4. 30..
 */
@Service
public class PhoneAuthServiceImpl implements PhoneAuthService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CommonMapper commonMapper;

    @Override
    public boolean sendMsgNow(String destNumber, String sendNumber, String msg, String code, String key) {
        String uniqueKey = System.currentTimeMillis() + RandomKeys.make(6);

        String sql = "INSERT INTO uds_msg \n" + "        (MSG_TYPE, CMID, REQUEST_TIME, SEND_TIME, DEST_PHONE, SEND_PHONE, MSG_BODY, ETC1, ETC2) \n" + "        VALUES \n"
                + "        (0, ?, SYSDATE(), SYSDATE(), ?, ?, ?, ?, ?)";
        int affectedNumber = jdbcTemplate.update(sql, uniqueKey, destNumber, sendNumber, msg, code, key);

        return affectedNumber > 0;
    }

    @Override
    public boolean sendMessageWithValidation(String destNumber, String sendNumber, String msg, String code, String key) {
        // 중복가능으로 변경 2016-10-20
        // if(!verifyPhoneNumber(destNumber))
        // throw new PhoneNumberDuplicatedException(destNumber);

        String uniqueKey = System.currentTimeMillis() + RandomKeys.make(6);
        String sql = "INSERT INTO uds_msg \n" + "        (MSG_TYPE, CMID, REQUEST_TIME, SEND_TIME, DEST_PHONE, SEND_PHONE, MSG_BODY, ETC1, ETC2) \n" + "        VALUES \n"
                + "        (0, ?, SYSDATE(), SYSDATE(), ?, ?, ?, ?, ?)";
        int affectedNumber = jdbcTemplate.update(sql, uniqueKey, destNumber, sendNumber, msg, code, key);

        return affectedNumber > 0;

    }

    private boolean verifyPhoneNumber(String phoneNumber) {
        BaseSecuModel baseSecuModel = new BaseSecuModel();

        String _sql = "select count(1) from user where phone = ? and deactive_date is NULL";

        int cnt = jdbcTemplate.queryForObject(_sql, new Object[] {baseSecuModel.encString(phoneNumber)}, Integer.class);

        return cnt == 0;
    }

    @Override
    public boolean checkAuth(String code, String key) {
        String sql = "SELECT CMID FROM uds_msg WHERE ETC1=? AND ETC2=?";

        List<String> ret = jdbcTemplate.query(sql, new Object[] {code, key}, (rs, rowNum) -> {
            return rs.getString("CMID");
        });

        return ret != null && !ret.isEmpty();
    }

    @Override
    public boolean checkAuth(String code, String key, String phone) {

        Map<String, Object> params = Maps.newHashMap();
        params.put("code", code);
        params.put("key", key);
        params.put("phone", phone);
        Map<String, Object> udsMsg = commonMapper.selectUdsMsg(params);

        boolean result = udsMsg == null ? false : true;
        logger.info("{}, {}, {}. 인증번호 확인 " + (result ? "성공" : "실패"), code, key, phone);

        return result;
    }

    @Override
    public String getPhoneNumber(String code, String key) {
        String sql = "SELECT DEST_PHONE FROM uds_msg WHERE ETC1=? AND ETC2=?";
        List<String> list = jdbcTemplate.query(sql, new Object[] {code, key}, (rs, rowNum) -> {
            return rs.getString("DEST_PHONE");
        });

        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }
}
