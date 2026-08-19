package com.baicai.demo.sdk;

import com.baicai.demo.config.IotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 南京诚联 IoT 平台 SDK 适配器实现。
 */
@Component
@ConditionalOnProperty(name = "my-iot.platform", havingValue = "njcl")
public class NclIotSdkAdapter implements IotSdkAdapter {

    private static final Logger log = LoggerFactory.getLogger(NclIotSdkAdapter.class);

    private final IotProperties iotProperties;
    private final RestTemplate restTemplate;

    public NclIotSdkAdapter(IotProperties iotProperties, RestTemplate restTemplate) {
        this.iotProperties = iotProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public IotDeviceInfo queryDeviceInfo(String iccid) {
        Map<String, Object> rawData = fetchFromPlatform(iccid);
        return IotDeviceInfo.from(rawData);
    }

    @Override
    public Map<String, Object> queryPhoneAndTraffic(String iccid) {
        return queryDeviceInfo(iccid).toMap();
    }

    /**
     * 向南京诚联平台发起 HTTP 请求，返回原始 JSON 解析后的 Map。
     */
    @SuppressWarnings("unchecked")
    @Retryable(value = RestClientException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Map<String, Object> fetchFromPlatform(String iccid) {
        if (iccid == null || iccid.isBlank()) {
            throw new IllegalArgumentException("iccid 不能为空");
        }

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("api_id", iotProperties.getApiId());
        params.put("timestamp", timestamp);
        params.put("iccids", iccid);

        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder signBuilder = new StringBuilder();
        for (String key : keys) {
            if (signBuilder.length() > 0) {
                signBuilder.append("&");
            }
            signBuilder.append(key).append("=").append(params.get(key));
        }
        signBuilder.append(iotProperties.getApiSecret());

        String signStr = sha256(base64Encode(signBuilder.toString().getBytes(StandardCharsets.UTF_8)));
        log.debug("timestamp={}, sign={}", timestamp, signStr);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        for (String key : keys) {
            formData.add(key, params.get(key));
        }
        formData.add("sign", signStr);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        log.info("调用南京诚联平台API | iccid={}", iccid);
        long apiStart = System.currentTimeMillis();
        ResponseEntity<Map> response = restTemplate.postForEntity(
                iotProperties.getApiUrl(), requestEntity, Map.class);
        Map<String, Object> rawData = (Map<String, Object>) response.getBody();
        log.info("平台返回 | 耗时={}ms | 状态码={}",
                System.currentTimeMillis() - apiStart,
                rawData != null ? rawData.get("code") : "null");

        String code = rawData != null ? String.valueOf(rawData.get("code")) : null;
        if (!"0".equals(code)) {
            log.error("平台返回错误 | iccid={} | code={} | message={}",
                    iccid, code, rawData != null ? rawData.get("message") : null);
        }
        return rawData;
    }

    private String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
