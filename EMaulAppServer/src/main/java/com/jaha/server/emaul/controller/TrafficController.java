package com.jaha.server.emaul.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.json.Station;
import com.jaha.server.emaul.model.json.StationBus;
import com.jaha.server.emaul.service.TrafficService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by doring on 15. 4. 30..
 */
@Controller
public class TrafficController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrafficController.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    public UserService userService;

    public TrafficService trafficService;

    @RequestMapping(value = "/api/traffic/stations", method = RequestMethod.GET)
    public @ResponseBody List<Station> getNearStations(HttpServletRequest req) {
        Long userId = SessionAttrs.getUserId(req.getSession());
        // Long userId = 2411L;
        User user = userService.getUser(userId);

        try {
            /**
             * 11 서울특별시 26 부산광역시 27 대구광역시 28 인천광역시 29 광주광역시 30 대전광역시 31 울산광역시 36 세종특별자치시 41 경기도 42 강원도 43 충청북도 44 충청남도 45 전라북도 46 전라남도 47 경상북도 48 경상남도 50 제주특별자치도
             */
            String areaCode = user.house.apt.address.건물관리번호.substring(0, 2);

            int radius = 300;

            // 일산동구 위시티블루밍5차 아파트의 경우 버스정류소 범위 확장
            if (user.house.apt.id == 255) {
                radius = 500;
                // 성북구 종암2차 아이파크 아파트의 경우 버스 정류소가 너무 많아서 범위 축소 (속도 개선)
            } else if (user.house.apt.id == 230) {
                radius = 200;
            }

            if ("11".equals(areaCode) || "41".equals(areaCode) || "28".equals(areaCode)) {
                trafficService = (TrafficService) context.getBean(Class.forName("com.jaha.server.emaul.service.TrafficService_" + areaCode + "_Impl"));
                return trafficService.getStations(user.house.apt.latitude, user.house.apt.longitude, radius, 2);
            }

        } catch (Exception e) {
            // do nothing
            LOGGER.error("<</api/traffic/stations 조회 중 오류>>", e.getMessage());
        }

        return Lists.newArrayList();
    }

    @RequestMapping(value = "/api/traffic/buses/{arsId}", method = RequestMethod.GET)
    public @ResponseBody List<StationBus> getBusInfo(HttpServletRequest req, @PathVariable(value = "arsId") String arsId) {

        Long userId = SessionAttrs.getUserId(req.getSession());
        // Long userId = 2411L;
        User user = userService.getUser(userId);

        try {
            /**
             * 11 서울특별시 26 부산광역시 27 대구광역시 28 인천광역시 29 광주광역시 30 대전광역시 31 울산광역시 36 세종특별자치시 41 경기도 42 강원도 43 충청북도 44 충청남도 45 전라북도 46 전라남도 47 경상북도 48 경상남도 50 제주특별자치도
             */
            String areaCode = user.house.apt.address.건물관리번호.substring(0, 2);
            if ("11".equals(areaCode) || "41".equals(areaCode) || "28".equals(areaCode)) {
                trafficService = (TrafficService) context.getBean(Class.forName("com.jaha.server.emaul.service.TrafficService_" + areaCode + "_Impl"));

                List<StationBus> stationList = trafficService.getStationBuses(arsId, 2);

                // traTime1 & traTime2 없는경우 2017-01-19 버전의 앱에서 오류발생 ( 둘중하나라도 없으면 목록에서 제거 ) 2017-01-19 버스관련 API 앱오류 대응코드
                if (stationList != null && !stationList.isEmpty()) {
                    int size = stationList.size();
                    for (int i = (size - 1); i >= 0; i--) {
                        StationBus stationBus = stationList.get(i);
                        if (StringUtil.isBlank(stationBus.traTime1) || StringUtil.isBlank(stationBus.traTime2)) {
                            stationList.remove(i);
                        }
                    }
                }

                return stationList;
            }

        } catch (Exception e) {
            // do nothing
            LOGGER.error("<</api/traffic/buses/" + arsId + " 조회 중 오류>>", e.getMessage());
        }

        return Lists.newArrayList();
    }
}
