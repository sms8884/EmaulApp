package com.jaha.server.emaul.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.UserLoginLog;
import com.jaha.server.emaul.v2.model.user.UserUpdateHistoryVo;

/**
 * Created by shavrani on 16-10-21
 */
@Mapper
public interface UserMapper {

    List<SimpleUser> selectUser(Map<String, Object> params);

    List<SimpleUser> selectUserList(Map<String, Object> params);

    /**
     * 로그인 히스토레이서 해당 사용자의 접속 기기 이력을 조회한다. <br/>
     * 로그아웃 푸시발송 용 GCM_ID
     *
     * @param map
     * @return
     */
    List<String> selectUserGcmHistory(Map<String, Object> map);

    /**
     * 사용자 설정변경 이력
     *
     * @param history
     * @return
     */
    int insertUserUpdateHistory(UserUpdateHistoryVo history);

    /**
     * 사용자가 직접 로그인 로그아웃한 기록저장
     */
    int insertUserLoginLog(UserLoginLog userLoginLog);

}
