package com.jaha.server.emaul.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.model.AptAp;
import com.jaha.server.emaul.model.AptApAccessLog;
import com.jaha.server.emaul.model.AptApBrokenLog;
import com.jaha.server.emaul.model.AptApMonitoring;
import com.jaha.server.emaul.model.AptApMonitoringAlive;
import com.jaha.server.emaul.model.AptApMonitoringNoti;
import com.jaha.server.emaul.model.SimpleUser;

/**
 * Created by shavrani on 16-06-23
 */
@Mapper
public interface AptApMapper {

    List<AptAp> selectAptApAccessList(Map<String, Object> params);

    AptAp selectAptApAccess(Map<String, Object> params);

    AptAp selectAptAp(Map<String, Object> params);

    List<AptAp> selectAptApList(Map<String, Object> params);

    List<Map<String, Object>> selectAptApAccessDeviceAuthList(Map<String, Object> params);

    int saveAptApExpIp(Map<String, Object> params);

    List<Map<String, Object>> selectAptApAccessDeviceList(Map<String, Object> params);

    AptApMonitoring selectAptApMonitoring(Map<String, Object> params);

    int insertAptApMonitoring(AptApMonitoring aptApMonitoring);

    int insertAptApMonitoringNoti(AptApMonitoringNoti aptApMonitoringNoti);

    public List<SimpleUser> selectAptApInspAccountList(String type);

    public List<Map<String, Object>> selectAptApInspDailyList(Map<String, Object> params);

    public List<Map<String, Object>> selectAptApInspDataLimitList(Map<String, Object> params);

    public List<Map<String, Object>> selectAptApInspWarningAptList(Map<String, Object> params);

    public Date selectDate();

    public List<AptAp> selectNoHistoryApList(Map<String, Object> params);

    public List<Map<String, Object>> selectNoHistoryApAptList(Map<String, Object> params);

    public int insertApMonitoringAlive(AptApMonitoringAlive aptApMonitoringAlive);

    public int deleteApMonitoringAlive(Map<String, Object> params);

    // / Edoor User Monitoring///
    public List<Map<String, Object>> selectAptApUserMonitoringListBatch(Map<String, Object> params);

    public List<Map<String, Object>> selectAptApUserMonitoringList(Map<String, Object> params);

    public int insertAptApUserMonitroing(Map<String, Object> params);

    public Integer selectAptApMonitoringTotalUser(Map<String, Object> params);

    // / Edoor User Monitoring END///

    public int insertAptApAccessLog(AptApAccessLog aptApAccessLog);

    public int insertApBrokenLog(AptApBrokenLog aptApBrokenLog);

    public Map<String, Object> selectExceptApt(Map<String, Object> param);


}
