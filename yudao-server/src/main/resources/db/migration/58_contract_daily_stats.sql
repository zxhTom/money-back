-- ============================================================
-- 合同金额走势"当天归档、归档后不再变化"
--
-- 问题：大盘走势此前是每次从 live 表实时重算
--   SELECT ... FROM custom_contract WHERE deleted = 0 AND create_time >= 29天前
-- 历史数字没有被存下来，所以任何让合同从 live 表消失的动作，都会让"那一天"的金额缩水：
--   1. 软删（deleted=1）—— 尤其 delete24HourContract 是按身份证批量软删；
--   2. ContractRecycleJob 6 个月后物理 DELETE 已软删合同；
--   3. ContractArchiveJob 12 个月后把合同搬去 archive 库（从主表删除）；
--   4. 多租户开启后查询自动追加 tenant_id 条件。
--
-- 解法：每天把前一天的数据落成快照，写入后永不覆盖。
-- 按 (租户, 日期, 状态) 三元组存，好处是以后要调整"哪些状态不计入"时，
-- 历史数据仍然算得出来，不需要重新归档（也归档不回来了）。
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_contract_daily_stats` (
    `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`      bigint         NOT NULL DEFAULT 1 COMMENT '租户编号',
    `stat_date`      date           NOT NULL COMMENT '统计日期（按合同 create_time 归属）',
    `status`         int            NOT NULL DEFAULT 0 COMMENT '快照时刻的合同状态：1待确认 2待收款 3已还款 4已逾期 5已失效 6拒签 7撤销',
    `contract_count` bigint         NOT NULL DEFAULT 0 COMMENT '合同笔数',
    `total_amount`   decimal(20,2)  NOT NULL DEFAULT 0.00 COMMENT '合同金额合计',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照写入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_date_status` (`tenant_id`, `stat_date`, `status`),
    KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同每日金额快照（写入后不再变更）';

-- 回填历史：从当前 live 表把已有数据补进来。
-- 注意不加 deleted 过滤——按需求"合同被删除了也应该算"。
-- 已经被物理删除/归档走的合同补不回来了，这部分历史仍然偏低，属于不可逆的既有损失。
-- INSERT IGNORE 保证幂等且不覆盖已有快照。
INSERT IGNORE INTO `custom_contract_daily_stats`
    (`tenant_id`, `stat_date`, `status`, `contract_count`, `total_amount`)
SELECT
    COALESCE(`tenant_id`, 1),
    DATE(`create_time`),
    COALESCE(`status`, 0),
    COUNT(*),
    COALESCE(SUM(`salary`), 0)
FROM `custom_contract`
WHERE `create_time` < CURDATE()
GROUP BY COALESCE(`tenant_id`, 1), DATE(`create_time`), COALESCE(`status`, 0);
