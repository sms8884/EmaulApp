/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 8. 29.
 */
package com.jaha.server.emaul.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceResolver;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * <pre>
 * Class Name : DeviceResolverHandlerInterceptor.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 8. 29.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 8. 29.
 * @version 1.0
 */
public class DeviceResolverHandlerInterceptor extends HandlerInterceptorAdapter {

    private final DeviceResolver deviceResolver;

    /**
     * Create a device resolving {@link HandlerInterceptor} that defaults to a {@link LiteDeviceResolver} implementation.
     */
    public DeviceResolverHandlerInterceptor() {
        this(new LiteDeviceResolver());
    }

    /**
     * Create a device resolving {@link HandlerInterceptor}.
     *
     * @param deviceResolver the device resolver to delegate to in {@link #preHandle(HttpServletRequest, HttpServletResponse, Object)}.
     */
    public DeviceResolverHandlerInterceptor(DeviceResolver deviceResolver) {
        this.deviceResolver = deviceResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Device device = deviceResolver.resolveDevice(request);
        request.setAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE, device);
        return true;
    }

}
