package com.jaha.server.emaul.prop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by doring on 15. 4. 29..
 */
@Component
@ConfigurationProperties(locations = "classpath:/url.properties", ignoreUnknownFields = false, prefix = "url")
public class UrlProperties {

    @Value("${gcm.server.url}")
    private String gcmServerUrl;

    private String gcmServer;

    public String getGcmServer() {
        gcmServer = gcmServerUrl;
        return gcmServer;
    }

    public void setGcmServer(String gcmServer) {
        // this.gcmServer = gcmServer;
        this.gcmServer = gcmServerUrl;
    }

}
