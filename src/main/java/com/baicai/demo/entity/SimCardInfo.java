package com.baicai.demo.entity;

import com.baicai.demo.sdk.IotDeviceInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "sim_card_info")
public class SimCardInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String iccid;

    @Column(length = 32)
    private String msisdn;

    @Column(name = "carrier_type", length = 64)
    private String carrierType;

    @Column(name = "life_cycle", length = 32)
    private String lifeCycle;

    @Column(name = "service_end_time", length = 64)
    private String serviceEndTime;

    @Column(name = "package_name", length = 255)
    private String packageName;

    @Column(name = "package_capacity_kb")
    private Double packageCapacityKb;

    @Column(name = "used_kb")
    private Double usedKb;

    @Column(name = "remaining_kb")
    private Double remainingKb;

    @Column(name = "usage_rate")
    private Double usageRate;

    @Column(name = "cycle_end_time", length = 64)
    private String cycleEndTime;

    @Column(name = "last_query_time", nullable = false)
    private LocalDateTime lastQueryTime;

    public SimCardInfo() {
    }

    public static SimCardInfo fromIotDeviceInfo(IotDeviceInfo info, LocalDateTime lastQueryTime) {
        SimCardInfo entity = new SimCardInfo();
        entity.setIccid(info.getIccid());
        entity.setMsisdn(info.getMsisdn());
        entity.setCarrierType(info.getCarrierType());
        entity.setLifeCycle(info.getLifeCycle());
        entity.setServiceEndTime(info.getServiceEndTime());
        entity.setPackageName(info.getPackageName());
        entity.setPackageCapacityKb(info.getPackageCapacityKb());
        entity.setUsedKb(info.getUsedKb());
        entity.setRemainingKb(info.getRemainingKb());
        entity.setUsageRate(info.getUsageRate());
        entity.setCycleEndTime(info.getCycleEndTime());
        entity.setLastQueryTime(lastQueryTime);
        return entity;
    }

    public static SimCardInfo fromMap(String requestedIccid, Map<String, Object> data,
                                      LocalDateTime lastQueryTime) {
        SimCardInfo entity = new SimCardInfo();
        entity.setIccid(requestedIccid);
        entity.setMsisdn(stringValue(data, "msisdn"));
        entity.setCarrierType(stringValue(data, "carrierType"));
        entity.setLifeCycle(stringValue(data, "lifeCycle"));
        entity.setServiceEndTime(stringValue(data, "serviceEndTime"));
        entity.setPackageName(stringValue(data, "packageName"));
        entity.setPackageCapacityKb(doubleValue(data, "packageCapacityKb"));
        entity.setUsedKb(doubleValue(data, "usedKb"));
        entity.setRemainingKb(doubleValue(data, "remainingKb"));
        entity.setUsageRate(doubleValue(data, "usageRate"));
        entity.setCycleEndTime(stringValue(data, "cycleEndTime"));
        entity.setLastQueryTime(lastQueryTime);
        return entity;
    }

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

    private static String stringValue(Map<String, Object> data, String key) {
        Object value = data == null ? null : data.get(key);
        return value == null ? null : value.toString();
    }

    private static Double doubleValue(Map<String, Object> data, String key) {
        Object value = data == null ? null : data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
