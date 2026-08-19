package com.baicai.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchQueryRequest {

    /** SIM 卡 ICCID 列表 */
    private List<String> iccids;
}
