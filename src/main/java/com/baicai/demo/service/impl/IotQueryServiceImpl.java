package com.baicai.demo.service.impl;

import com.baicai.demo.entity.QueryLog;
import com.baicai.demo.entity.SimCardInfo;
import com.baicai.demo.repository.QueryLogRepository;
import com.baicai.demo.repository.SimCardInfoRepository;
import com.baicai.demo.sdk.IotSdkAdapter;
import com.baicai.demo.service.IotQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class IotQueryServiceImpl implements IotQueryService {

    private static final Logger log = LoggerFactory.getLogger(IotQueryServiceImpl.class);
    private static final int CACHE_MINUTES = 10;

    private final IotSdkAdapter iotSdkAdapter;
    private final SimCardInfoRepository simCardInfoRepository;
    private final QueryLogRepository queryLogRepository;

    public IotQueryServiceImpl(IotSdkAdapter iotSdkAdapter,
                               SimCardInfoRepository simCardInfoRepository,
                               QueryLogRepository queryLogRepository) {
        this.iotSdkAdapter = iotSdkAdapter;
        this.simCardInfoRepository = simCardInfoRepository;
        this.queryLogRepository = queryLogRepository;
    }

    @Override
    public Map<String, Object> queryPhoneAndTraffic(String iccid) {
        long startTime = System.currentTimeMillis();
        log.info("查询请求开始 | iccid={}", iccid);

        LocalDateTime now = LocalDateTime.now();
        SimCardInfo cached = simCardInfoRepository.findByIccid(iccid).orElse(null);
        if (isFresh(cached, now)) {
            log.info("命中数据库缓存 | iccid={} | lastQueryTime={}", iccid, cached.getLastQueryTime());
            return logAndReturn(iccid, cached.toMap(), startTime);
        }

        if (cached == null) {
            log.info("数据库未命中，调用平台 | iccid={}", iccid);
        } else {
            log.info("数据库缓存已过期，调用平台 | iccid={} | lastQueryTime={}",
                    iccid, cached.getLastQueryTime());
        }

        Map<String, Object> sdkResult = iotSdkAdapter.queryPhoneAndTraffic(iccid);
        SimCardInfo entity = cached != null ? cached : new SimCardInfo();
        updateFromMap(entity, iccid, sdkResult, now);
        SimCardInfo saved = simCardInfoRepository.save(entity);
        queryLogRepository.save(createQueryLog(iccid, now));
        log.info("平台结果已保存 | iccid={} | lastQueryTime={}", iccid, saved.getLastQueryTime());
        return logAndReturn(iccid, saved.toMap(), startTime);
    }

    private QueryLog createQueryLog(String iccid, LocalDateTime queryTime) {
        QueryLog queryLog = new QueryLog();
        queryLog.setIccid(iccid);
        queryLog.setQueryTime(queryTime);
        return queryLog;
    }

    private boolean isFresh(SimCardInfo info, LocalDateTime now) {
        return info != null
                && info.getLastQueryTime() != null
                && !info.getLastQueryTime().isBefore(now.minusMinutes(CACHE_MINUTES));
    }

    private void updateFromMap(SimCardInfo entity, String requestedIccid,
                               Map<String, Object> data, LocalDateTime lastQueryTime) {
        SimCardInfo mapped = SimCardInfo.fromMap(requestedIccid, data, lastQueryTime);
        entity.setIccid(mapped.getIccid());
        entity.setMsisdn(mapped.getMsisdn());
        entity.setCarrierType(mapped.getCarrierType());
        entity.setLifeCycle(mapped.getLifeCycle());
        entity.setServiceEndTime(mapped.getServiceEndTime());
        entity.setPackageName(mapped.getPackageName());
        entity.setPackageCapacityKb(mapped.getPackageCapacityKb());
        entity.setUsedKb(mapped.getUsedKb());
        entity.setRemainingKb(mapped.getRemainingKb());
        entity.setUsageRate(mapped.getUsageRate());
        entity.setCycleEndTime(mapped.getCycleEndTime());
        entity.setLastQueryTime(mapped.getLastQueryTime());
    }

    private Map<String, Object> logAndReturn(String iccid, Map<String, Object> result, long startTime) {
        log.info("查询请求结束 | iccid={} | 耗时={}ms | msisdn={}",
                iccid, System.currentTimeMillis() - startTime, result.get("msisdn"));
        return result;
    }

    /** 设备信息包含的所有字段名（与 IotDeviceInfo.toMap() 保持一致） */
    private static final List<String> DEVICE_FIELDS = List.of(
            "iccid", "msisdn", "carrierType", "lifeCycle", "serviceEndTime",
            "packageName", "packageCapacityKb", "usedKb", "remainingKb",
            "usageRate", "cycleEndTime"
    );

    @Override
    public List<Map<String, Object>> batchQuery(List<String> iccids) {
        log.info("批量查询开始 | 数量={} | iccids={}", iccids.size(), iccids);

        List<CompletableFuture<Map<String, Object>>> futures = iccids.stream()
                .map(iccid -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return queryPhoneAndTraffic(iccid);
                    } catch (Exception e) {
                        log.error("批量查询单个失败 | iccid={} | error={}", iccid, e.getMessage());
                        return buildEmptyResult(iccid);
                    }
                }))
                .toList();

        List<Map<String, Object>> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long successCount = results.stream().filter(r -> r.get("msisdn") != null).count();
        log.info("批量查询结束 | 总数={} | 成功={} | 失败={}",
                results.size(), successCount, results.size() - successCount);
        return results;
    }

    /**
     * 构建查询失败时的空结果：iccid 保留，其余字段全部置 null。
     */
    private Map<String, Object> buildEmptyResult(String iccid) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String field : DEVICE_FIELDS) {
            map.put(field, null);
        }
        map.put("iccid", iccid);
        return map;
    }
}
