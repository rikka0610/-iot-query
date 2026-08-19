package com.baicai.demo.service;

import java.util.List;
import java.util.Map;

/**
 * IoT 查询服务接口。
 */
public interface IotQueryService {

    /**
     * 根据 ICCID 查询 SIM 卡详情（手机号、流量等）。
     *
     * @param iccid SIM 卡的 ICCID
     * @return 查询结果
     */
    Map<String, Object> queryPhoneAndTraffic(String iccid);

    /**
     * 批量查询多个 SIM 卡详情。
     *
     * @param iccids SIM 卡 ICCID 列表
     * @return 查询结果列表，每个元素为扁平结构的设备信息 Map（iccid + 所有业务字段）；
     *         单卡查询失败时该卡对应对象除 iccid 外其余字段为 null，不影响其他卡
     */
    List<Map<String, Object>> batchQuery(List<String> iccids);
}
