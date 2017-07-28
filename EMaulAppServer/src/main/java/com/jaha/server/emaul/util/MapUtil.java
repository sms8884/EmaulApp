package com.jaha.server.emaul.util;

/**
 * Created by basscraft on 2015-11-25.
 */
public class MapUtil {
    /**
     * 두 지점(경도,위도 ~ 경도,위도)간의 거리계산
     * http://egloos.zum.com/metashower/v/313035
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return 두 지점 간의 거리 (m)
     */
    public static double calDistance(double lat1, double lon1, double lat2, double lon2) {

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344; // 단위 mile 에서 km 변환.
        dist = dist * 1000.0; // 단위 km 에서 m 로 변환

        //System.out.println(lat1 + " " + lon1 + " to " + lat2 + " " + lon2 + " dist : " + dist);

        return dist;
    }

    /**
     * 주어진 도(degree) 값을 라디언으로 변환
     *
     * @param deg
     * @return
     */
    private static double deg2rad(double deg) {
        return (double) (deg * Math.PI / (double) 180d);
    }

    /**
     * 주어진 라디언(radian) 값을 도(degree) 값으로 변환
     *
     * @param rad
     * @return
     */
    private static double rad2deg(double rad) {
        return (double) (rad * (double) 180d / Math.PI);
    }

/*
    public static void main(String args[]) {
        double x1, x2, y1, y2, dx, dy, s, d;

        y1 = 127.08191341933116; x1 = 37.265219886776165; // 황골주공.벽산아파트
        y2 = 126.88091454546266; x2 = 37.46139621023124; // 하안사거리

        dx = x2 - x1;
        dy = y2 - y1;

        d = MapUtil.calDistance(x1, y1, x2, y2);
        System.out.println(d);
    }
*/
}
