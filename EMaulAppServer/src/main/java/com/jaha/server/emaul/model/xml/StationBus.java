package com.jaha.server.emaul.model.xml;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.ibatis.type.Alias;
import org.xml.sax.SAXException;

@Deprecated
@Alias("DeprecatedStationBus")
public class StationBus extends ItemListGenerator {
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

    /*
     * <arsId>17247</arsId> <busRouteId>4561500</busRouteId> <busType1>0</busType1> <busType2>0</busType2> <firstTm>04:10</firstTm> <gpsX>126.8911892632711</gpsX> <gpsY>37.483863491116615</gpsY>
     * <isArrive1>0</isArrive1> <isArrive2>0</isArrive2> <isLast1>0</isLast1> <isLast2>0</isLast2> <lastTm>23:00</lastTm> <nextBus></nextBus> <plainNo1>서울70사8348</plainNo1>
     * <plainNo2>서울74사9684</plainNo2> <repTm1>2015-05-15 11:38:07.0</repTm1> <repTm2>2015-05-15 11:37:46.0</repTm2> <routeType>4</routeType> <rtNm>5615</rtNm> <sectOrd1>14</sectOrd1>
     * <sectOrd2>9</sectOrd2> <stId>31137</stId> <stNm>구로남초등학교</stNm> <staOrd>16</staOrd> <stationNm1>디지털단지오거리</stationNm1> <stationNm2>난곡입구</stationNm2> <stationTp>0</stationTp> <term>4</term>
     * <traSpd1>18</traSpd1> <traSpd2>16</traSpd2> <traTime1>132</traTime1> <traTime2>683</traTime2> <vehId1>492</vehId1> <vehId2>9869</vehId2>
     */

    public static List<StationBus> create(String xmlData) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, InstantiationException, IllegalAccessException {

        return create(xmlData, StationBus.class);
    }
}
