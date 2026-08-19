package com.baicai.demo.controller;

import com.baicai.demo.common.Result;
import com.baicai.demo.dto.BatchQueryRequest;
import com.baicai.demo.service.IotQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iot")
public class IotQueryController {

    private final IotQueryService iotQueryService;

    public IotQueryController(IotQueryService iotQueryService) {
        this.iotQueryService = iotQueryService;
    }

    /** 单个查询（GET，参数放 URL） */
    @GetMapping("/query")
    public Result<Map<String, Object>> query(@RequestParam(required = false) String iccid) {
        if (iccid == null || iccid.isBlank()) {
            return Result.error(400, "iccid 参数不能为空");
        }
        Map<String, Object> data = iotQueryService.queryPhoneAndTraffic(iccid);
        return Result.success(data);
    }

    /** 批量查询（POST，参数放请求体） */
    @PostMapping("/batch-query")
    public Result<List<Map<String, Object>>> batchQuery(@RequestBody BatchQueryRequest request) {
        List<String> iccids = request.getIccids();
        if (iccids == null || iccids.isEmpty()) {
            return Result.error(400, "iccids 不能为空");
        }
        List<Map<String, Object>> results = iotQueryService.batchQuery(iccids);
        return Result.success(results);
    }
}
