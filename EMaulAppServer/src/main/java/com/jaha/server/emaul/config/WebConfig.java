package com.jaha.server.emaul.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.jaha.server.emaul.common.converter.GuConverter;
import com.jaha.server.emaul.common.converter.TodaySortConverter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Bean
    public LoginChecker LoginCheckInterceptor() {
        return new LoginChecker();
    }

    @Bean
    public ParcelAuthChecker getParcelAuthCheckInterceptor() {
        return new ParcelAuthChecker();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public LiteDeviceResolver getLiteDeviceResolver() {
        return new LiteDeviceResolver();
    }

    @Bean
    @ConditionalOnMissingBean(DeviceResolverHandlerInterceptor.class)
    public DeviceResolverHandlerInterceptor getDeviceResolverHandlerInterceptor() {
        return new DeviceResolverHandlerInterceptor(this.getLiteDeviceResolver());
    }

    @Bean
    public PagingArgumentResolver getPagingArgumentResolver() {
        return new PagingArgumentResolver();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(LoginCheckInterceptor());
        registry.addInterceptor(this.getParcelAuthCheckInterceptor());
        registry.addInterceptor(this.getDeviceResolverHandlerInterceptor());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // argumentResolvers.add(this.getDeviceHandlerMethodArgumentResolver());

        argumentResolvers.add(this.getPagingArgumentResolver());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new TodaySortConverter());
        registry.addConverter(new GuConverter());
    }

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout(3000); // 연결 요청 지연이 3초를 초과하면 Exception
        requestFactory.setConnectTimeout(3000); // 서버에 연결 지연이 3초를 초과하면 Exception
        requestFactory.setReadTimeout(3000); // 데이터 수신 지연이 3초를 초과하면 Exception

        return new RestTemplate(requestFactory);
    }

}
