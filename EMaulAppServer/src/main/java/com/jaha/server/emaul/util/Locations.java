package com.jaha.server.emaul.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doring on 15. 5. 1..
 */
public class Locations {
    public static class LatLng {
        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double lat;
        public double lng;
    }

    public static LatLng getLocationFromAddress(String addressOld) {

        RestTemplate restTemplate = new RestTemplate();

        // address 인코딩 하면 안됨. rest template 이 함.
        String resp = restTemplate.getForObject("http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=" +
                addressOld, String.class);
        if (resp == null || resp.isEmpty()) {
            return null;
        }

        JSONObject jsonObject = new JSONObject(resp);

        if (jsonObject.optString("status") == null ||
                !"OK".equalsIgnoreCase(jsonObject.optString("status"))) {
            return null;
        }

        try {
            double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            return new LatLng(lat, lng);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static LatLng getLocationEPSG3857toWGS84GEO(double lat, double lon) {
        return getLocationEPSG3857toWGS84GEO(String.valueOf(lat), String.valueOf(lon));
    }

    /**
     * 경기도 버스 API용
     * SK T-Map OPEN API 를 이용하여 EPSG3857 좌표를 WGS84GEO 로 변환
     *
     * @param lat
     * @param lon
     * @return
     */
    public static LatLng getLocationEPSG3857toWGS84GEO(String lat, String lon) {
        return getLocationCoordConvert(lat, lon, "EPSG3857", "WGS84GEO");
    }

    /**
     * 경기도 버스 API용
     * SK T-Map OPEN API 를 이용하여 WGS84GEO 좌표를 EPSG3857  로 변환
     *
     * @param lat
     * @param lon
     * @return
     */
    public static LatLng getLocationWGS84GEOtoEPSG3857(String lat, String lon) {
        return getLocationCoordConvert(lat, lon, "WGS84GEO", "EPSG3857");
    }

    /**
     * 경기도 버스 API용
     * SK T-Map OPEN API 를 이용하여 EPSG3857 좌표를 WGS84GEO 로 변환
     * EPSG:3857 : Google Mercator 좌표계. EPSG:900913으로 사용되기도 합니다. 900913은 알파벳 GOOGLE과 비슷한 숫자의 조합으로 특별한 뜻을 가지고 있지는 않습니다.
     * EPSG:4326 : WGS84 좌표계. 구글 Earth가 사용하고 있는 좌표입니다.(주의 : Google Earth의 이미지 타일과 Google Maps의 이미지는 다른 좌표계입니다.
     *
     * @param lat
     * @param lon
     * @param fromCoord
     * @param toCoord
     * @return
     */
    public static LatLng getLocationCoordConvert(String lat, String lon, String fromCoord, String toCoord) {

        // javax.net.ssl.SSLProtocolException: handshake alert: unrecognized_name 발생 방지
        System.setProperty("jsse.enableSNIExtension", "false");

        String url = "https://apis.skplanetx.com/tmap/geo/coordconvert?version=1&lat={lat}&lon={lon}&fromCoord={fromCoord}&toCoord={toCoord}";
        String appKey = "5bb5ccfd-4c52-3b66-a03d-cc87170d7e8a";

        RestTemplate rest = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("appKey", appKey);
        HttpEntity entity = new HttpEntity(headers);

        Map<String, String> param = new HashMap<String, String>();
        param.put("lat", lat);
        param.put("lon", lon);
        param.put("fromCoord", fromCoord);
        param.put("toCoord", toCoord);

        HttpEntity<String> response = rest.exchange(url, HttpMethod.GET, entity, String.class, param);

        String resp = response.getBody();

        if (resp == null || resp.isEmpty()) {
            return null;
        }

        // resp sample
        // {
        //    "coordinate": {
        //        "lat": "37.219568",
        //        "lon": "127.051676"
        //    }
        // }

        JSONObject jsonObject = new JSONObject(resp).getJSONObject("coordinate");

        try {
            double _lng = jsonObject.getDouble("lon");
            double _lat = jsonObject.getDouble("lat");

            return new LatLng(_lat, _lng);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
