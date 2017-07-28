package com.jaha.server.emaul.service;

import com.jaha.server.emaul.constants.Gu;

import java.io.IOException;
import java.util.Map;

/**
 * 
 * <pre>
 * Class Name : LockScreenService.java
 * Description : Description
 *  
 * Modification Information
 * 
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 8. 22.     shavrani      Generation
 * </pre>
 *
 * @author shavrani
 * @since 2016. 8. 22.
 * @version 1.0
 */
public interface LockScreenService {


    public Map<String, Object> getLockScreenNews(Map<String, Object> params);

    public Map<String, Object> getMetroNews(Map<String, Object> params);

    Map<String, Object> getRiverLevelInformation(Gu gu);
}
