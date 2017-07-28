package com.jaha.server.emaul.model.xml;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.ibatis.type.Alias;
import org.xml.sax.SAXException;

@Deprecated
@Alias("DeprecatedStation")
public class Station extends ItemListGenerator {
    public String arsId;
    public String dist;
    public String gpsX; // longitude
    public String gpsY; // latitude
    public String stationId;
    public String stationNm;
    public String stationTp;

    /*
     * <arsId>17247</arsId> <gpsX>126.8911892632711</gpsX> <gpsY>37.483863491116615</gpsY> <stationId>31137</stationId> <stationNm>구로남초등학교</stationNm> <stationTp>0</stationTp>
     */

    public static List<Station> create(String xmlData) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, InstantiationException, IllegalAccessException {

        return create(xmlData, Station.class);
    }
}
