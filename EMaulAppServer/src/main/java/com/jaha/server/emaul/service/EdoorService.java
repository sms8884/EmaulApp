package com.jaha.server.emaul.service;

import java.util.List;
import java.util.Map;

import com.jaha.server.emaul.model.AptAp;
import com.jaha.server.emaul.model.AptApAccessDevice;
import com.jaha.server.emaul.model.AptApAccessLog;
import com.jaha.server.emaul.model.AptApDaemonLog;
import com.jaha.server.emaul.model.AptApMonitoring;
import com.jaha.server.emaul.model.AptApMonitoringNoti;
import com.jaha.server.emaul.model.User;



/**
 * @author shavrani
 * @since 2016. 9. 2.
 * @version 1.0
 */
public interface EdoorService {

    List<AptAp> selectAptApAccessList(Map<String, Object> params);

    AptAp selectAptApAccess(Map<String, Object> params);

    AptAp selectAptAp(Map<String, Object> params);

    List<AptAp> selectAptApList(Map<String, Object> params);

    void saveAptApAccessLog(AptApAccessLog aptApAccessLog);

    List<AptApAccessLog> findAptApAccessRequestExist(Map<String, Object> params);

    List<Map<String, Object>> selectAptApAccessDeviceAuthList(Map<String, Object> params);

    AptApAccessDevice findByAccessKeyAndDeactiveDateIsNull(String accessKey);

    String saveApAccessLocationLogBeacon(Map<String, Object> params);

    String saveApAccessLocationLogApp(Map<String, Object> params);

    String aptApAccessRequest(User user, Map<String, Object> params);

    String aptApAccessRequestExist(Map<String, Object> params);

    String aptApAccessOpenSuccess(Map<String, Object> params);

    String aptApAccessDevice(Map<String, Object> params);

    String saveAptApExpIp(Map<String, Object> params);

    Map<String, Object> saveAptAp(Map<String, Object> params);

    AptApDaemonLog saveAptApDaemonLog(AptApDaemonLog aadl);

    List<Map<String, Object>> selectAptApAccessDeviceList(Map<String, Object> params);

    String apAccessBeaconMockupPush(Map<String, Object> params);

    AptApMonitoring selectAptApMonitoring(Map<String, Object> params);

    int insertAptApMonitoring(AptApMonitoring aptApMonitoring);

    int insertAptApMonitoringNoti(AptApMonitoringNoti aptApMonitoringNoti);

    int monitoringSendMailUserList();

    int monitoringNoHistoryApSendMail(Integer hour);

    /**
     * @author shavrani 2017-01-13
     * @desc Apt Ap Health Check Process
     */
    public Map<String, Object> aptApMonitoringHealthCheck(List<AptAp> apList, Integer storagePeriod);

    /**
     * E도어 사용자 모니터링 배치용
     * 
     * @param params
     * @return
     */
    public List<Map<String, Object>> selectAptApUserMonitoringBatch();

    /**
     * E도어 사용자 모니터링 통계데이터 Save
     * 
     * @param params
     * @return
     */
    public int saveAptApUserMonitroing(List<Map<String, Object>> params);



    /**
     * ##############################################################################################################################################################################
     * #################################################################### 2017-02-01 이후 e-door api 개발 버전 ####################################################################
     * ##############################################################################################################################################################################
     */

    /**
     * Created by shavrani on 17-02-02
     */
    public String apOpenRequest(User user, Map<String, Object> params);

    /**
     * Created by shavrani on 17-02-03
     */
    public String saveApOpenResult(User user, Map<String, Object> params);

    /**
     * Created by shavrani on 17-02-03
     */
    public String batchSaveApOpenResult(User user, Map<String, Object> params);

    /**
     * Created by shavrani on 17-02-07
     */
    public String saveApBrokenLog(User user, Map<String, Object> params);

}
