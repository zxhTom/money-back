-- 用户属性：戏耍模式（开启后该用户查询到的合同相关数据全部为确定性伪造数据，本人信息不受影响）
ALTER TABLE `system_users`
    ADD COLUMN `tease_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否对该用户的合同类查询返回伪造数据(0-否 1-是)' AFTER `disable_pwd_change`;
