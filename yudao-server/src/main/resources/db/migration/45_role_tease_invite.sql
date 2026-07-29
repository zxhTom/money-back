-- 角色属性：戏耍模式 & 邀请功能
ALTER TABLE `system_role`
    ADD COLUMN `tease_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启戏耍模式(0-否 1-是)' AFTER `remark`;
ALTER TABLE `system_role`
    ADD COLUMN `invite_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许邀请(0-否 1-是)' AFTER `tease_enabled`;
