package com.jaha.server.emaul.model.json;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaha.server.emaul.util.Locations;
import com.jaha.server.emaul.util.MapUtil;

/**
 * Created by doring on 15. 5. 21..
 */

// http://m.bus.go.kr/mBus/bus/getStationByPos.bms
// tmX, tmY, radius
public class Station {
    public String arsId;
    public String dist;
    public String gpsX; // longitude
    public String gpsY; // latitude
    public String stationId;
    public String stationNm;
    public String stationTp;

    /**
     * 서울 버스 API : 정류장 정보
     *
     * @param json
     * @return
     */
    public static List<Station> create(String json) {
        JSONObject obj = new JSONObject(json);
        JSONArray jsonArray = obj.optJSONArray("resultList");
        if (jsonArray != null) {
            return new Gson().fromJson(jsonArray.toString(), new TypeToken<List<Station>>() {}.getType());
        }

        return Lists.newArrayList();
    }

    /**
     * 경기버스 API : 정류장 정보
     *
     * @param json
     * @return
     */
    public static List<Station> create41(String json) {
        JSONObject obj = new JSONObject(json);

        JSONArray jsonArray = obj.getJSONObject("result").getJSONObject("resultMap").getJSONArray("list");
        Locations.LatLng fiducial = new Locations.LatLng(obj.getJSONObject("fiducial").getDouble("lat"), obj.getJSONObject("fiducial").getDouble("lng"));
        int radius = obj.getJSONObject("fiducial").getInt("radius");

        if (jsonArray != null) {
            JSONArray stations = new JSONArray();
            for (int index = 0; index < jsonArray.length(); index++) {
                JSONObject station = new JSONObject();
                station.put("arsId", jsonArray.getJSONObject(index).getString("stationId"));
                // station.put("arsId", jsonArray.getJSONObject(index).getString("staNo"));
                station.put("stationId", jsonArray.getJSONObject(index).getString("stationId"));
                station.put("stationNm", jsonArray.getJSONObject(index).getString("stationNm"));
                Locations.LatLng latlan = Locations.getLocationEPSG3857toWGS84GEO(jsonArray.getJSONObject(index).getString("lat"), jsonArray.getJSONObject(index).getString("lon"));

                station.put("gpsX", latlan.lng);
                station.put("gpsY", latlan.lat);
                station.put("dist", "");
                station.put("stationTp", "");

                if (MapUtil.calDistance(fiducial.lng, fiducial.lat, latlan.lng, latlan.lat) <= radius)
                    stations.put(station);
            }
            return new Gson().fromJson(stations.toString(), new TypeToken<List<Station>>() {}.getType());
        }

        return Lists.newArrayList();
    }



    /**
     * 인천버스 API
     * 
     * @param json
     * @return
     */
    public static List<Station> create28(String json) {
        JSONObject obj = new JSONObject(json);
        JSONArray jsonArray = obj.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item");
        if (jsonArray != null) {
            JSONArray stations = new JSONArray();
            for (int index = 0; index < jsonArray.length(); index++) {
                JSONObject station = new JSONObject();
                station.put("arsId", jsonArray.getJSONObject(index).getString("nodeid"));
                station.put("stationId", jsonArray.getJSONObject(index).getString("nodeid"));
                station.put("stationNm", jsonArray.getJSONObject(index).getString("nodenm"));

                station.put("gpsX", jsonArray.getJSONObject(index).getDouble("gpslong"));
                station.put("gpsY", jsonArray.getJSONObject(index).getDouble("gpslati"));
                station.put("dist", "");
                station.put("stationTp", "");
                stations.put(station);

            }

            return new Gson().fromJson(stations.toString(), new TypeToken<List<Station>>() {}.getType());
        }
        return Lists.newArrayList();
    }

}
