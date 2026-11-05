-- 系统升级横幅展示时段和计划停机时段分开维护。旧公告保持 NULL，由接口按推送后 24 小时兼容。
ALTER TABLE `system_notice`
    ADD COLUMN `display_start_time` datetime(3) NULL COMMENT '横幅展示开始时间',
    ADD COLUMN `display_end_time` datetime(3) NULL COMMENT '横幅展示结束时间',
    ADD COLUMN `maintenance_start_time` datetime(3) NULL COMMENT '计划停机开始时间',
    ADD COLUMN `maintenance_end_time` datetime(3) NULL COMMENT '计划停机结束时间';
