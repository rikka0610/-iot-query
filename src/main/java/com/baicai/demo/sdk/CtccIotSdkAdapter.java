package com.baicai.demo.sdk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 中国电信 IoT 平台适配器。
 *
 * 当前仅返回模拟数据；接入中国电信真实 API 时，替换 queryDeviceInfo 方法即可。
 */
@Component
@ConditionalOnProperty(name = "my-iot.platform", havingValue = "ctcc")
public class CtccIotSdkAdapter implements IotSdkAdapter {

    @Override
    public IotDeviceInfo queryDeviceInfo(String iccid) {
        // 模拟数据：此处暂未调用中国电信真实 API。
        return IotDeviceInfo.simulated(
                iccid,
                "18900000000",
                "中国电信（模拟）",
                "中国电信测试套餐（模拟）",
                2.0 * 1024.0 * 1024.0,
                4096.0,
                "2026-12-31"
        );
    }
}
