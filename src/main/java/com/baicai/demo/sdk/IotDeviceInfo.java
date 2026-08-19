package com.baicai.demo.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 与具体运营商平台无关的统一设备信息模型。
 */
public class IotDeviceInfo {

    private static final Logger log = LoggerFactory.getLogger(IotDeviceInfo.class);

    private String iccid;
    private String msisdn;
    private String carrierType;
    private String lifeCycle;
    private String serviceEndTime;
    private String packageName;
    private Double packageCapacityKb;
    private Double usedKb;
    private Double remainingKb;
    private Double usageRate;
    private String cycleEndTime;

    public IotDeviceInfo() {
    }

    public IotDeviceInfo(String iccid, String msisdn, String carrierType, String lifeCycle,
                         String serviceEndTime, String packageName, Double packageCapacityKb,
                         Double usedKb, Double remainingKb, Double usageRate, String cycleEndTime) {
        this.iccid = iccid;
        this.msisdn = msisdn;
        this.carrierType = carrierType;
        this.lifeCycle = lifeCycle;
        this.serviceEndTime = serviceEndTime;
        this.packageName = packageName;
        this.packageCapacityKb = packageCapacityKb;
        this.usedKb = usedKb;
        this.remainingKb = remainingKb;
        this.usageRate = usageRate;
        this.cycleEndTime = cycleEndTime;
    }

    public static IotDeviceInfo simulated(String iccid, String msisdn, String carrierType,
                                           String packageName, Double capacityKb, Double usedKb,
                                           String cycleEndTime) {
        Double remainingKb = capacityKb != null && usedKb != null
                ? Math.max(0, capacityKb - usedKb) : null;
        Double usageRate = capacityKb != null && capacityKb > 0 && usedKb != null
                ? Math.round(usedKb / capacityKb * 10000.0) / 100.0 : null;
        return new IotDeviceInfo(iccid, msisdn, carrierType, "已激活", null, packageName,
                capacityKb, usedKb, remainingKb, usageRate, cycleEndTime);
    }

    public String getIccid() { return iccid; }
    public String getMsisdn() { return msisdn; }
    public String getCarrierType() { return carrierType; }
    public String getLifeCycle() { return lifeCycle; }
    public String getServiceEndTime() { return serviceEndTime; }
    public String getPackageName() { return packageName; }
    public Double getPackageCapacityKb() { return packageCapacityKb; }
    public Double getUsedKb() { return usedKb; }
    public Double getRemainingKb() { return remainingKb; }
    public Double getUsageRate() { return usageRate; }
    public String getCycleEndTime() { return cycleEndTime; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("iccid", iccid);
        map.put("msisdn", msisdn);
        map.put("carrierType", carrierType);
        map.put("lifeCycle", lifeCycle);
        map.put("serviceEndTime", serviceEndTime);
        map.put("packageName", packageName);
        map.put("packageCapacityKb", packageCapacityKb);
        map.put("usedKb", usedKb);
        map.put("remainingKb", remainingKb);
        map.put("usageRate", usageRate);
        map.put("cycleEndTime", cycleEndTime);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static IotDeviceInfo from(Map<String, Object> raw) {
        if (raw == null) {
            throw new RuntimeException("平台返回为空");
        }
        String code = stringValue(raw.get("code"));
        String message = stringValue(raw.get("message"));
        if (!"0".equals(code)) {
            throw new RuntimeException("平台返回错误: " + message + " (code=" + code + ")");
        }

        Map<String, Object> resultItem = null;
        Object result = raw.get("result");
        if (result instanceof List<?> list
                && !list.isEmpty() && list.get(0) instanceof Map<?, ?>) {
            resultItem = (Map<String, Object>) list.get(0);
        }
        if (resultItem == null) {
            return new IotDeviceInfo();
        }

        IotDeviceInfo info = new IotDeviceInfo();
        info.iccid = stringValue(resultItem.get("iccid"));
        info.msisdn = stringValue(resultItem.get("msisdn"));
        info.carrierType = stringValue(resultItem.get("carrier_type"));
        info.serviceEndTime = stringValue(resultItem.get("service_end_time"));
        info.lifeCycle = parseLifeCycle(resultItem.get("life_cycle"));

        Object products = resultItem.get("current_products");
        Map<String, Object> product = null;
        if (products instanceof List<?> list
                && !list.isEmpty() && list.get(0) instanceof Map<?, ?>) {
            product = (Map<String, Object>) list.get(0);
        }
        if (product != null) {
            info.packageName = stringValue(product.get("name"));
            info.cycleEndTime = stringValue(product.get("current_cycle_end_time"));
            info.packageCapacityKb = toKb(product.get("package_capacity"),
                    stringValue(product.get("capacity_unit")));
            info.usedKb = toKb(product.get("current_cycle_usage"), "KB");
            if (info.packageCapacityKb != null && info.usedKb != null) {
                info.remainingKb = Math.max(0, info.packageCapacityKb - info.usedKb);
                if (info.packageCapacityKb > 0) {
                    double rate = info.usedKb / info.packageCapacityKb * 100.0;
                    info.usageRate = Math.round(rate * 100.0) / 100.0;
                }
            }
        }
        log.debug("平台数据已统一转换 | iccid={}", info.iccid);
        return info;
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static Double toKb(Object value, String unit) {
        if (value == null) return null;
        double number;
        try {
            number = Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
        if (unit == null) return number;
        return switch (unit.toUpperCase()) {
            case "KB" -> number;
            case "MB" -> number * 1024.0;
            case "GB" -> number * 1024.0 * 1024.0;
            case "TB" -> number * 1024.0 * 1024.0 * 1024.0;
            default -> number;
        };
    }

    private static String parseLifeCycle(Object value) {
        if (value == null) return null;
        return switch (value.toString().trim()) {
            case "1" -> "未激活";
            case "2" -> "已激活";
            case "3" -> "已停用";
            default -> value.toString();
        };
    }
}
