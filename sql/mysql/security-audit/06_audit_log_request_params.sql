-- 审计日志新增接口参数字段 + 密码修改记录索引
ALTER TABLE `custom_audit_log`
    ADD COLUMN `request_params` mediumtext DEFAULT NULL COMMENT '接口请求参数(JSON，密码字段已脱敏)' AFTER `after_data`;
