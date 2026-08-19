CREATE DATABASE IF NOT EXISTS iot_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE iot_db;

CREATE TABLE IF NOT EXISTS sim_card_info (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    iccid VARCHAR(32) NOT NULL COMMENT 'SIM 卡 ICCID',
    msisdn VARCHAR(32) NULL COMMENT '手机号',
    carrier_type VARCHAR(64) NULL COMMENT '运营商类型',
    life_cycle VARCHAR(32) NULL COMMENT '生命周期',
    service_end_time VARCHAR(64) NULL COMMENT '服务到期时间',
    package_name VARCHAR(255) NULL COMMENT '套餐名称',
    package_capacity_kb DOUBLE NULL COMMENT '套餐总流量（KB）',
    used_kb DOUBLE NULL COMMENT '已用流量（KB）',
    remaining_kb DOUBLE NULL COMMENT '剩余流量（KB）',
    usage_rate DOUBLE NULL COMMENT '使用率（百分比）',
    cycle_end_time VARCHAR(64) NULL COMMENT '当前周期结束时间',
    last_query_time DATETIME NOT NULL COMMENT '最后一次平台查询时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sim_card_info_iccid (iccid)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SIM 卡查询结果缓存';

CREATE TABLE IF NOT EXISTS favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    iccid VARCHAR(32) NOT NULL COMMENT 'SIM 卡 ICCID',
    remark VARCHAR(255) NULL COMMENT '收藏备注',
    created_at DATETIME NOT NULL COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_favorite_iccid (iccid)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SIM 卡收藏';

CREATE TABLE IF NOT EXISTS query_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    iccid VARCHAR(32) NOT NULL COMMENT 'SIM 卡 ICCID',
    query_time DATETIME NOT NULL COMMENT '平台查询时间',
    PRIMARY KEY (id),
    KEY idx_query_log_time (query_time),
    KEY idx_query_log_time_iccid (query_time, iccid)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SIM 卡平台查询日志';
