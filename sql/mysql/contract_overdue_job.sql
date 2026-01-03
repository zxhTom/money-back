-- 合同逾期检查定时任务
-- 功能：定时检查 custom_contract 表中 end_date < 当前时间的合同，将状态更新为已逾期（状态值：4）

-- 注意：
-- 1. handler_name 必须是 JobHandler 的 Spring Bean 名称（类名首字母小写）
-- 2. status: 1=正常(NORMAL), 2=暂停(STOP), 0=初始化中(INIT)
-- 3. cron_expression: CRON 表达式，示例：
--    - '0 0 0 * * ?' : 每天凌晨执行
--    - '0 0/30 * * * ?' : 每30分钟执行
--    - '0 * * * * ?' : 每分钟执行（测试用）

INSERT INTO `infra_job` (`name`, `status`, `handler_name`, `handler_param`, `cron_expression`, `retry_count`, `retry_interval`, `monitor_timeout`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES ('合同逾期检查 Job', 1, 'contractOverdueCheckJob', '', '0 0 0 * * ?', 3, 0, 0, '1', NOW(), '1', NOW(), b'0');

