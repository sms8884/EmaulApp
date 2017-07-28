package com.jaha.server.emaul.config;

import java.util.Collections;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by doring on 2014. 3. 14..
 */
@Configuration
public class TomcatConfig {

    @Value("${server.http.port}")
    private int serverHttpPort;

    @Bean
    public EmbeddedServletContainerFactory containerCustomizer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        TomcatConnectorCustomizer connectorCustomizer = connector -> {
            connector.setAttribute("maxThreads", 50000);
            connector.setAttribute("acceptCount", 16000);
            connector.setAttribute("maxConnections", 100000);
            connector.setAttribute("minSpareThreads", 256);
            connector.setAttribute("URIEncoding", "UTF-8");
            connector.setAttribute("connectionTimeout", 20000);
            connector.setAttribute("connectionUploadTimeout", 140000);
            connector.setAttribute("keepAliveTimeout", 140000);
        };

        factory.setTomcatConnectorCustomizers(Collections.singletonList(connectorCustomizer));

        Connector connector = new Connector(TomcatEmbeddedServletContainerFactory.DEFAULT_PROTOCOL);
        connector.setPort(serverHttpPort);

        // HTTP compression 설정 추가, 20161103, 전강욱
        connector.setAttribute("compression", "on");
        connector.setAttribute("compressableMimeType", "text/html,text/xml,text/plain,application/json,application/xml");
        connector.setAttribute("compressionMinSize", "2048"); // 2KB 이하는 압축안함

        factory.addAdditionalTomcatConnectors(connector);

        return factory;
    }

}
