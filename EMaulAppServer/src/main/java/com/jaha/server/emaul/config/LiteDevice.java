/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 8. 29.
 */
package com.jaha.server.emaul.config;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.mobile.device.DeviceType;

/**
 * <pre>
 * Class Name : LiteDevice.java
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
@Deprecated
public class LiteDevice implements Device {

    public static final LiteDevice NORMAL_INSTANCE = new LiteDevice(DeviceType.NORMAL, DevicePlatform.UNKNOWN);

    public static final LiteDevice MOBILE_INSTANCE = new LiteDevice(DeviceType.MOBILE, DevicePlatform.UNKNOWN);

    public static final LiteDevice TABLET_INSTANCE = new LiteDevice(DeviceType.TABLET, DevicePlatform.UNKNOWN);

    @Override
    public boolean isNormal() {
        return this.deviceType == DeviceType.NORMAL;
    }

    @Override
    public boolean isMobile() {
        return this.deviceType == DeviceType.MOBILE;
    }

    @Override
    public boolean isTablet() {
        return this.deviceType == DeviceType.TABLET;
    }

    @Override
    public DevicePlatform getDevicePlatform() {
        return this.devicePlatform;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public static Device from(DeviceType deviceType, DevicePlatform devicePlatform) {
        return new LiteDevice(deviceType, devicePlatform);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[LiteDevice ");
        builder.append("type").append("=").append(this.deviceType);
        builder.append("]");
        return builder.toString();
    }

    private final DeviceType deviceType;

    private final DevicePlatform devicePlatform;

    /**
     * Creates a LiteDevice with DevicePlatform.
     */
    private LiteDevice(DeviceType deviceType, DevicePlatform devicePlatform) {
        this.deviceType = deviceType;
        this.devicePlatform = devicePlatform;
    }

}
