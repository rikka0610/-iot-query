package com.baicai.demo.sdk;

import java.util.Map;

/**
 * IoT 平台适配器统一接口。
 */
public interface IotSdkAdapter {

    /**
     * 查询并返回平台无关的设备信息。
     */
    IotDeviceInfo queryDeviceInfo(String iccid);

    /**
     * 兼容现有 Service 的扁平 Map 查询契约。
     */
    default Map<String, Object> queryPhoneAndTraffic(String iccid) {
        return queryDeviceInfo(iccid).toMap();
    }
}
