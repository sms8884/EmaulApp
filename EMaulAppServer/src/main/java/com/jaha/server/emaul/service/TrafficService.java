package com.jaha.server.emaul.service;


import com.jaha.server.emaul.model.json.Station;
import com.jaha.server.emaul.model.json.StationBus;

import java.util.List;

/**
 * Created by doring on 15. 5. 1..
 */
public interface TrafficService {

    List<Station> getStations(double lat, double lng, int radius, int retryCount);

    List<StationBus> getStationBuses(String arsId, int retryCount);

}
