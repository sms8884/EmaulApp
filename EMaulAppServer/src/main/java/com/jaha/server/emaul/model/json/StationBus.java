package com.jaha.server.emaul.model.json;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by doring on 15. 5. 21..
 */
// http://m.bus.go.kr/mBus/bus/getStationByUid.bms
// arsId
public class StationBus {
    // 노선 유형 변환 필요(서울 기준 1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
    private static final Map<String, String> routeTypeMap;
    static {
        routeTypeMap = Maps.newHashMap();

        routeTypeMap.put("공항버스", "1");
        routeTypeMap.put("마을버스", "2");
        routeTypeMap.put("간선버스", "3");
        routeTypeMap.put("지선버스", "4");
        routeTypeMap.put("순환버스", "5");
        routeTypeMap.put("광역버스", "6");
    }

    public String arsId;
    public String busRouteId;
    public String busType1;
    public String busType2;
    public String firstTm;
    public String gpsX;
    public String gpxY;
    public String isArrive1;
    public String isArrive2;
    public String isLast1;
    public String isLast2;
    public String lastTm;
    public String nextBus;
    public String plainNo1;
    public String plainNo2;
    public String repTm1;
    public String repTm2;
    public String routeType;
    public String rtNm;
    public String sectOrd1;
    public String sectOrd2;
    public String stId;
    public String stNm;
    public String staOrd;
    public String stationNm1;
    public String stationNm2;
    public String stationTp;
    public String term;
    public String traSpd1;
    public String traSpd2;
    public String traTime1;
    public String traTime2;
    public String vehId1;
    public String vehId2;

    public static List<StationBus> create(String json) {
        JSONObject obj = new JSONObject(json);
        JSONArray jsonArray = obj.optJSONArray("resultList");
        if (jsonArray != null) {
            return new Gson().fromJson(jsonArray.toString(), new TypeToken<List<StationBus>>() {}.getType());
        }

        return Lists.newArrayList();
    }

    /**
     * 경기도 버스 정류장 버스 정보
     *
     * @param json
     * @return
     */
    public static List<StationBus> create41(String json) {
        JSONObject obj = new JSONObject(json).getJSONObject("result");
        JSONArray busStationInfos = obj.getJSONArray("busStationInfo");
        JSONArray busArrivalInfos = obj.getJSONArray("busArrivalInfo");
        JSONArray jsonArray = new JSONArray();

        if (busStationInfos != null) {

            for (int index = 0; index < busStationInfos.length(); index++) {
                JSONObject busInfo = new JSONObject();
                busInfo.put("arsId", obj.getString("stationId"));
                busInfo.put("stId", obj.getString("stationId"));
                busInfo.put("stNm", obj.getString("stationNm"));

                JSONObject busStationInfo = busStationInfos.getJSONObject(index);
                String routeId = busStationInfo.getString("routeId"); // 노선 ID
                busInfo.put("busRouteId", routeId);
                busInfo.put("rtNm", busStationInfo.getString("routeName")); // 노선명
                busInfo.put("staOrd", busStationInfo.getString("staOrder")); // 요청 정류소 순번
                busInfo.put("routeType", busStationInfo.getString("routeTypeCd")); // 노선 유형 변환 필요(서울 기준 1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)

                for (int index2 = 0; index2 < busArrivalInfos.length(); index2++) {
                    if (routeId.equals(busArrivalInfos.getJSONObject(index2).getString("routeId"))) {
                        JSONObject busArrivealInfo = busArrivalInfos.getJSONObject(index2);
                        busInfo.put("arrmsg1", busArrivealInfo.getString("predictTime1") + "분후 [" + busArrivealInfo.getString("locationNo1") + "번째 전]");// ?
                        busInfo.put("arrmsg2", busArrivealInfo.getString("predictTime2") + "분후 [" + busArrivealInfo.getString("locationNo2") + "번째 전]");// ?
                        busInfo.put("vehId1", busArrivealInfo.getString("vehId1"));
                        busInfo.put("vehId2", busArrivealInfo.getString("vehId2"));
                        busInfo.put("adirection", busArrivealInfo.getString("routeDestName")); // ?
                        busInfo.put("plainNo1", busArrivealInfo.getString("plateNo1"));
                        busInfo.put("plainNo2", busArrivealInfo.getString("plateNo2"));
                        busInfo.put("stationNm1", busArrivealInfo.getString("stationNm1"));
                        busInfo.put("stationNm2", busArrivealInfo.getString("stationNm2"));
                        busInfo.put("busType1", "");
                        busInfo.put("busType2", "");
                        busInfo.put("firstTm", "");
                        busInfo.put("gpsX", "");
                        busInfo.put("gpxY", "");
                        busInfo.put("isLast1", "");
                        busInfo.put("isLast2", "");
                        busInfo.put("lastTm", "");
                        busInfo.put("nextBus", "");
                        busInfo.put("repTm1", "");
                        busInfo.put("repTm2", "");
                        busInfo.put("stationTp", "");
                        busInfo.put("term", "");
                        busInfo.put("traSpd1", "");
                        busInfo.put("traSpd2", "");

                        busInfo.put("isArrive1", "0"); // ? 첫번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착) 경기 버스 api 에는 값이 없음
                        busInfo.put("isArrive2", "0"); // ? 두번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착) 경기 버스 api 에는 값이 없음

                        /* 서울버스는 와 동일하게 보여지도록 'n번째 전' 값 가공 */
                        int locationNo1, locationNo2, staOrder;
                        try {
                            locationNo1 = Integer.parseInt(busArrivealInfo.getString("locationNo1"));
                        } catch (Exception e) {
                            locationNo1 = 0;
                        }
                        try {
                            locationNo2 = Integer.parseInt(busArrivealInfo.getString("locationNo2"));
                        } catch (Exception e) {
                            locationNo2 = 0;
                        }
                        try {
                            staOrder = Integer.valueOf(busArrivealInfo.getString("staOrder"));
                        } catch (Exception e) {
                            staOrder = 0;
                        }
                        busInfo.put("sectOrd1", Integer.toString(staOrder - locationNo1));
                        busInfo.put("sectOrd2", Integer.toString(staOrder - locationNo2));

                        /* 경기 버스는 분, 서울버스는 초단위 사용함 */
                        String predictTime1 = busArrivealInfo.getString("predictTime1");
                        try {
                            if (null == predictTime1 || predictTime1.isEmpty())
                                predictTime1 = "";
                            else
                                predictTime1 = Integer.toString((Integer.valueOf(predictTime1) * 60));
                            busInfo.put("traTime1", predictTime1);
                        } catch (Exception e) {
                            predictTime1 = "";
                        }
                        busInfo.put("traTime1", predictTime1);

                        String predictTime2 = busArrivealInfo.getString("predictTime2");
                        try {
                            if (null == predictTime2 || predictTime2.isEmpty())
                                predictTime2 = "";
                            else
                                predictTime2 = Integer.toString((Integer.valueOf(predictTime2) * 60));
                        } catch (Exception e) {
                            predictTime2 = "";
                        }
                        busInfo.put("traTime2", predictTime2);

                        jsonArray.put(busInfo); // 운행 정보가 있는 경우만 담는다.
                    }
                }
            }
            // jsonArray.put(busInfo);
        }
        if (jsonArray != null) {
            return new Gson().fromJson(jsonArray.toString(), new TypeToken<List<StationBus>>() {}.getType());
        }

        return Lists.newArrayList();
    }


    /**
     * 인천 버스 정류장 버스정보
     * 
     * @param json
     * @return
     */

    public static List<StationBus> create28(String json) {
        JSONObject obj = new JSONObject(json);
        JSONObject busInfoObject;
        JSONArray busInfoArray;
        JSONArray jsonArray = new JSONArray();


        if (!obj.getJSONObject("response").getJSONObject("body").get("items").toString().isEmpty()) {

            /* 도착예정버스가 한대만 있을경우 */
            if (obj.getJSONObject("response").getJSONObject("body").get("items").toString().length() < 200) {
                JSONObject busInfo = new JSONObject();
                busInfoObject = obj.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONObject("item");

                busInfo.put("arsId", busInfoObject.getString("nodeid"));
                busInfo.put("stId", busInfoObject.getString("nodeid"));
                busInfo.put("stNm", busInfoObject.getString("nodenm"));
                String routeId2 = busInfoObject.getString("routeid"); // 노선 ID busInfo.put("busRouteId", routeId2);
                if (!routeId2.isEmpty()) {
                    String routeType2 = busInfoObject.getString("routetp");
                    busInfo.put("rtNm", busInfoObject.get("routeno")); // 노선명
                    routeType2 = routeTypeMap.get(routeType2); // 노선 유형 변환 필요(서울 기준 1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
                    busInfo.put("busRouteId", routeId2);
                    busInfo.put("routeType", routeType2);
                    busInfo.put("traTime1", busInfoObject.getInt("arrtime"));
                    busInfo.put("vehId1", routeId2);
                    busInfo.put("sectOrd1", busInfoObject.getInt("arrprevstationcnt"));
                    /* 요청정류장순번 가공 */
                    int staOrd = busInfoObject.getInt("arrprevstationcnt") * 2;
                    busInfo.put("staOrd", staOrd);
                    busInfo.put("arrmsg1", busInfoObject.getInt("arrtime") + "분후 [" + busInfoObject.getInt("arrprevstationcnt") + "번째 전]");// 첫번째 도착예정 버스의
                    // 비어있는값들은 API 에 없는 값들입니다
                    busInfo.put("arrmsg2", "");
                    busInfo.put("vehId2", "");
                    busInfo.put("adirection", "");
                    busInfo.put("plainNo1", "");
                    busInfo.put("plainNo2", "");
                    busInfo.put("stationNm1", "");
                    busInfo.put("stationNm2", "");
                    busInfo.put("busType1", "");
                    busInfo.put("busType2", "");
                    busInfo.put("firstTm", "");
                    busInfo.put("gpsX", "");
                    busInfo.put("gpxY", "");
                    busInfo.put("isLast1", "");
                    busInfo.put("isLast2", "");
                    busInfo.put("lastTm", "");
                    busInfo.put("nextBus", "");
                    busInfo.put("repTm1", "");
                    busInfo.put("repTm2", "");
                    busInfo.put("stationTp", "");
                    busInfo.put("term", "");
                    busInfo.put("traSpd1", "");
                    busInfo.put("traSpd2", "");
                    busInfo.put("isArrive1", "0"); // ? 첫번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착) 인천 버스 api 에는 값이 없음
                    busInfo.put("isArrive2", "0"); // ? 두번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착) 인천 버스 api 에는 값이 없음
                    busInfo.put("sectOrd2", "");
                    busInfo.put("traTime2", "");
                    jsonArray.put(busInfo); // 운행 정보가 있는 경우만 담는다.

                }
            } else {

                /* 도착예정버스가 한대이상있을 경우 */
                busInfoArray = obj.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item");

                /* 첫번째 도착 예정버스 ID 를 계산한다 */

                int tmp = 0;
                for (int index = 0; index < busInfoArray.length(); index++) {
                    if (index < busInfoArray.length() - 1) {
                        String indedxStr = busInfoArray.getJSONObject(index).getString("routeid");
                        String indedxAfStr = busInfoArray.getJSONObject(index + 1).getString("routeid");
                        if (indedxStr.equals(indedxAfStr)) {
                            if (busInfoArray.getJSONObject(index).getInt("arrtime") > busInfoArray.getJSONObject(index + 1).getInt("arrtime")) {
                                continue;
                            } else {
                                tmp = index + 1;
                            }
                        }
                    }
                    JSONObject busInfo = new JSONObject();
                    if (index != 0 && index == tmp) {
                        continue;
                    }
                    String routeId = busInfoArray.getJSONObject(index).getString("routeid"); // 노선 ID
                    if (!routeId.isEmpty()) {
                        busInfo.put("arsId", busInfoArray.getJSONObject(index).getString("nodeid"));
                        busInfo.put("stId", busInfoArray.getJSONObject(index).getString("nodeid"));
                        busInfo.put("stNm", busInfoArray.getJSONObject(index).getString("nodenm"));
                        busInfo.put("busRouteId", routeId);
                        busInfo.put("rtNm", busInfoArray.getJSONObject(index).get("routeno")); // 노선명
                        String routeType = busInfoArray.getJSONObject(index).getString("routetp");
                        routeType = routeTypeMap.get(routeType); // 노선 유형 변환 필요(서울 기준 1:공항, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
                        busInfo.put("routeType", routeType);


                        busInfo.put("vehId1", busInfoArray.getJSONObject(index).getString("routeid"));
                        busInfo.put("traTime1", busInfoArray.getJSONObject(index).getInt("arrtime"));
                        busInfo.put("sectOrd1", busInfoArray.getJSONObject(index).getInt("arrprevstationcnt"));

                        /* 요청정류장순번 가공 */
                        int staOrd = busInfoArray.getJSONObject(index).getInt("arrprevstationcnt") * 2;

                        busInfo.put("staOrd", staOrd);// 요청 정류소 순번

                        busInfo.put("arrmsg1", busInfoArray.getJSONObject(index).getInt("arrtime") + "분후 [" + busInfoArray.getJSONObject(index).getInt("arrprevstationcnt") + "번째 전]");// 첫번째 도착예정 버스의
                        // 비어있는값들은 API 에 없는 값들입니다
                        busInfo.put("arrmsg2", "");
                        busInfo.put("vehId2", "");
                        busInfo.put("adirection", "");
                        busInfo.put("plainNo1", "");
                        busInfo.put("plainNo2", "");
                        busInfo.put("stationNm1", "");
                        busInfo.put("stationNm2", "");
                        busInfo.put("busType1", "");
                        busInfo.put("busType2", "");
                        busInfo.put("firstTm", "");
                        busInfo.put("gpsX", "");
                        busInfo.put("gpxY", "");
                        busInfo.put("isLast1", "");
                        busInfo.put("isLast2", "");
                        busInfo.put("lastTm", "");
                        busInfo.put("nextBus", "");
                        busInfo.put("repTm1", "");
                        busInfo.put("repTm2", "");
                        busInfo.put("stationTp", "");
                        busInfo.put("term", "");
                        busInfo.put("traSpd1", "");
                        busInfo.put("traSpd2", "");
                        busInfo.put("isArrive1", "0"); // ? 첫번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착) 인천 버스 api 에는 값이 없음
                        busInfo.put("isArrive2", "0"); // ? 두번째도착예정버스의 최종 정류소 도착출발여부 (0:운행중, 1:도착) 인천 버스 api 에는 값이 없음
                        busInfo.put("sectOrd2", "");
                        busInfo.put("traTime2", "");
                        jsonArray.put(busInfo); // 운행 정보가 있는 경우만 담는다.
                    }
                }
            }
        }
        // jsonArray.put(busInfo);
        if (jsonArray != null) {
            return new Gson().fromJson(jsonArray.toString(), new TypeToken<List<StationBus>>() {}.getType());
        }
        return Lists.newArrayList();
    }

}
