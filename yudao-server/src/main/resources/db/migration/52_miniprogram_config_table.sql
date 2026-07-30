-- ============================================================
-- 小程序静态信息配置（单例，全局固定只有 id=1 这一行）
-- 对应 Java: cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO
-- ============================================================

CREATE TABLE IF NOT EXISTS `custom_miniprogram_config` (
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键，固定只有一行，约定用 id=1',
    `app_name`         varchar(100) NOT NULL DEFAULT '' COMMENT '小程序名称',
    `slogan`           varchar(200) NOT NULL DEFAULT '' COMMENT '一句话简介',
    `app_description`  varchar(1000) NOT NULL DEFAULT '' COMMENT '详细描述',
    `company_name`     varchar(200) NOT NULL DEFAULT '' COMMENT '公司名称',
    `contact_email`    varchar(200) NOT NULL DEFAULT '' COMMENT '联系邮箱',
    `bound_user_id`    bigint       DEFAULT NULL COMMENT '绑定的代表用户ID，改app_name时联动改这个用户的姓名+其相关合同当事人姓名',
    `creator`          varchar(64)  DEFAULT '' COMMENT '创建者',
    `create_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`          varchar(64)  DEFAULT '' COMMENT '更新者',
    `update_time`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序静态信息配置（单例）';

INSERT INTO `custom_miniprogram_config` (id, app_name, slogan, app_description, company_name, contact_email, creator, updater)
SELECT 1, '合约管理', '让生活更美好', '我们致力于为用户提供优质的服务体验。通过创新的技术和用心的设计，让每一位用户都能感受到科技的温暖与便捷。', '飞速合约', '492728133@qq.com', 'system', 'system'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `custom_miniprogram_config` WHERE id = 1);
