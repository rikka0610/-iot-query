package com.baicai.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "my-iot")
public class IotProperties {

    /** 当前平台：njcl、cmcc 或 ctcc */
    private String platform;
    /** 平台 API ID */
    private String apiId;
    /** 平台 API Secret */
    private String apiSecret;
    /** 平台 API 地址 */
    private String apiUrl;
    /** 连接超时（毫秒） */
    private Integer connectTimeout;
    /** 读取超时（毫秒） */
    private Integer readTimeout;
}
