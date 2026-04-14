-- 修复 JobPersistenceException: The job (DEFAULT.contractOverdueCheckJob) referenced by the trigger does not exist.
-- 原因：QRTZ_TRIGGERS 中存在指向 contractOverdueCheckJob 的触发器，但 QRTZ_JOB_DETAILS 中无对应作业，
--      导致保存/更新触发器时报错。本脚本删除该孤立触发器，之后在管理端执行一次「同步」即可重新创建作业与触发器。
--
-- 使用前请确认 SCHED_NAME：若未改过配置，为 'schedulerName'（见 application.yaml 中 spring.quartz.properties.org.quartz.scheduler.instanceName）。

-- 1. 删除与 contractOverdueCheckJob 关联的 CRON 触发器（先删子表）
DELETE ct FROM QRTZ_CRON_TRIGGERS ct
INNER JOIN QRTZ_TRIGGERS t
  ON ct.SCHED_NAME = t.SCHED_NAME AND ct.TRIGGER_NAME = t.TRIGGER_NAME AND ct.TRIGGER_GROUP = t.TRIGGER_GROUP
WHERE t.JOB_NAME = 'contractOverdueCheckJob';

-- 2. 删除指向 contractOverdueCheckJob 的触发器
DELETE FROM QRTZ_TRIGGERS WHERE JOB_NAME = 'contractOverdueCheckJob';

-- 执行完成后：到 基础设施 -> 定时任务 -> 点击「同步」，即可为 contractOverdueCheckJob 重新创建 Job 与 Trigger。
