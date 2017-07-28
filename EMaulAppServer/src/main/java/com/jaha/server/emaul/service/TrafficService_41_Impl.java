package com.jaha.server.emaul.service;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jaha.server.emaul.model.TrafficCache;
import com.jaha.server.emaul.model.json.Station;
import com.jaha.server.emaul.model.json.StationBus;
import com.jaha.server.emaul.repo.TrafficRepository;
import com.jaha.server.emaul.util.Locations;

/**
 * Created by basscraft on 15. 11. 20. 경기도 버스 API 호출 경기도 => 41
 */
@Service
public class TrafficService_41_Impl implements TrafficService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // private static final String URL_BASE = "http://ws.bus.go.kr/api/rest/stationinfo/";
    // private static final String SERVICE_KEY = "/Ag7ihGQJBm145b/xHO1XaM9tV79Edoq2OC8Dg3W4NWJYXvzsnvAj8+IHi+7lMVeekuZx9hi+EsOTbEwQCGALQ==";
    //
    // private static final String URL_STATION_INFO = URL_BASE + "getStationByPos";
    // private static final String URL_STATION_BUS_INFO = URL_BASE + "getStationByUid";

    private static final String URL_BASE = "http://www.gbis.go.kr/gbis2014/schBusAPI.action";
    private static final String URL_STATION_CMD = "searchAroundBusStationJson";
    private static final String URL_STATION_BUS_CMD = "searchBusStationJson";

    private RestTemplate rest = new RestTemplate();
    private Gson gson = new Gson();

    @Autowired
    private TrafficRepository trafficRepository;

    @Override
    public List<Station> getStations(double lat, double lng, int radius, int retryCount) {
        final String cacheKey = "getStations " + lat + " " + lng + " " + radius;

        TrafficCache cache = trafficRepository.findOne(cacheKey);
        if (cache != null) {
            // 저장된 데이터 전송
            return gson.fromJson(cache.json, new TypeToken<List<Station>>() {}.getType());
        }

        String json = null;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 경기도 버스 api는 EPSG3857 좌표계 사용
            Locations.LatLng latlan = Locations.getLocationWGS84GEOtoEPSG3857(Double.toString(lat), Double.toString(lng));

            HttpEntity<String> param = new HttpEntity<>(String.format("cmd=%s&lon=%s&lat=%s&radius=%s", URL_STATION_CMD, latlan.lng, latlan.lat, radius), headers);

            json = rest.postForObject(URL_BASE, param, String.class);

            // 기준 위치 추가
            JSONObject fiducialObj = new JSONObject();
            fiducialObj.put("lat", lat);
            fiducialObj.put("lng", lng);
            fiducialObj.put("radius", radius);

            JSONObject jsonObj = new JSONObject(json);
            jsonObj.put("fiducial", fiducialObj);

            json = jsonObj.toString();
        } catch (Exception e) {
            logger.error("", e);
            if (retryCount > 0) {
                return getStations(lat, lng, radius, --retryCount);
            } else {
                return Lists.newArrayList();
            }
        }

        if (json == null) {
            return Lists.newArrayList();
        }

        try {
            List<Station> ret = Station.create41(json);
            cache = new TrafficCache();
            cache.cacheKey = cacheKey;
            cache.json = gson.toJson(ret);
            cache.cacheDate = new Date();
            cache.expireMinutes = 60 * 24; // 1일
            trafficRepository.save(cache);
            return ret;
        } catch (Exception e) {
            logger.error("<<getStations>> 오류", e.getMessage());
        }

        return Lists.newArrayList();
    }

    @Override
    public List<StationBus> getStationBuses(String stationId, int retryCount) {
        final String cacheKey = "getStationBuses " + stationId;
        TrafficCache cache = trafficRepository.findOne(cacheKey);
        if (cache != null) {
            // 저장된 데이터 전송
            return gson.fromJson(cache.json, new TypeToken<List<StationBus>>() {}.getType());
        }

        String json = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> param = new HttpEntity<>(String.format("cmd=%s&stationId=%s", URL_STATION_BUS_CMD, stationId), headers);

            json = rest.postForObject(URL_BASE, param, String.class);
        } catch (Exception e) {
            if (retryCount > 0) {
                return getStationBuses(stationId, --retryCount);
            } else {
                return Lists.newArrayList();
            }
        }

        if (json == null) {
            return Lists.newArrayList();
        }

        try {
            List<StationBus> ret = StationBus.create41(json);
            cache = new TrafficCache();
            cache.cacheKey = cacheKey;
            cache.json = gson.toJson(ret);
            cache.cacheDate = new Date();
            cache.expireMinutes = 1; // 1분
            trafficRepository.save(cache);
            return ret;
        } catch (Exception e) {
            logger.error("<<getStationBuses>> 오류", e.getMessage());
        }

        return Lists.newArrayList();
    }
}
