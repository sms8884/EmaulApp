package com.jaha.server.emaul.service;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jaha.server.emaul.model.TrafficCache;
import com.jaha.server.emaul.model.json.Station;
import com.jaha.server.emaul.model.json.StationBus;
import com.jaha.server.emaul.repo.TrafficRepository;

/**
 * Created by doring on 15. 5. 1.. 인천버스 API 인천 => 28
 */
@Service
public class TrafficService_28_Impl implements TrafficService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SERVICE_KEY = "ServiceKey=VxP0yGoGlvWiQzPgFr5DCx0aGXL%2BAocEN5DYR99RXkU%2F8N%2BWXt6MlL%2FEtUbbwGMrd1CAS%2FWOxW302A5TtGOHfw%3D%3D";

    private RestTemplate template = new RestTemplate();
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
            StringBuilder urlBuilder = new StringBuilder("http://openapi.tago.go.kr/openapi/service/BusSttnInfoInqireService/getCrdntPrxmtSttnList"); /* URL */
            urlBuilder.append("?" + SERVICE_KEY); /* Service Key */
            urlBuilder.append("&" + URLEncoder.encode("gpsLati", "UTF-8") + "=" + URLEncoder.encode(Double.toString(lat), "UTF-8")); /* GPS X 좌표 */
            urlBuilder.append("&" + URLEncoder.encode("gpsLong", "UTF-8") + "=" + URLEncoder.encode(Double.toString(lng), "UTF-8")); /* GPS Y 좌표 */
            URI uri = new URI(urlBuilder.toString());
            String tmp = template.getForObject(uri, String.class);
            json = new String(tmp.getBytes("iso-8859-1"), "utf-8");


        } catch (Exception e) {

            if (retryCount > 0) {
                return getStations(lat, lng, radius, --retryCount);
            } else {
                return Lists.newArrayList();
            }
        }

        if (json == null || json.length() < 20) {
            return Lists.newArrayList();
        }
        try {
            List<Station> ret = Station.create28(json);
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
            StringBuilder urlBuilder = new StringBuilder("http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList"); /* URL */

            urlBuilder.append("?" + SERVICE_KEY); /* Service Key */
            urlBuilder.append("&" + URLEncoder.encode("cityCode", "UTF-8") + "=" + URLEncoder.encode("23", "UTF-8")); /* 도시코드 */
            urlBuilder.append("&" + URLEncoder.encode("nodeId", "UTF-8") + "=" + URLEncoder.encode(stationId, "UTF-8")); /* 정류소ID */
            URI uri = new URI(urlBuilder.toString());
            String tmp = template.getForObject(uri, String.class);
            json = new String(tmp.getBytes("iso-8859-1"), "utf-8");
            JSONObject obj = new JSONObject(json);
            if (!obj.getJSONObject("response").getJSONObject("header").getString("resultCode").equals("00")) {
                logger.info("resultCodeError" + tmp);
                tmp = template.getForObject(uri, String.class);
                json = new String(tmp.getBytes("iso-8859-1"), "utf-8");
            }


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
            List<StationBus> ret = StationBus.create28(json);
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
