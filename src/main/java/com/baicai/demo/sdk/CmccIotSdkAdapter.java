package com.baicai.demo.sdk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 中国移动 IoT 平台适配器。
 *
 * 当前仅返回模拟数据；接入中国移动真实 API 时，替换 queryDeviceInfo 方法即可。
 */
@Component
@ConditionalOnProperty(name = "my-iot.platform", havingValue = "cmcc")
public class CmccIotSdkAdapter implements IotSdkAdapter {

    @Override
    public IotDeviceInfo queryDeviceInfo(String iccid) {
        // 模拟数据：此处暂未调用中国移动真实 API。
        return IotDeviceInfo.simulated(
                iccid,
                "13900000000",
                "中国移动（模拟）",
                "中国移动测试套餐（模拟）",
                1024.0 * 1024.0,
                2048.0,
                "2026-12-31"
        );
    }
}
