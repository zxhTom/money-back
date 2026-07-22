-- ============================================================================
-- 小程序默认审核模式 系统配置（infra_config，幂等）。默认 safe。
--   /custom/contract/dashboard/model 接口在 contract_model 表没有匹配到具体版本记录时，
--   原先硬编码返回 safe，现改为读取此系统参数，管理端"基础设施 -> 参数管理"页可见可编辑。
--   safe    = 审核期保守文案（默认，最保守）
--   offcial = 已过审真实文案
-- ============================================================================

INSERT INTO `infra_config`
    (`category`, `type`, `name`, `config_key`, `value`, `visible`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 'biz', 2, '小程序默认审核模式', 'custom.contract.default-model', 'safe', b'1',
       'safe或offcial：当/custom/contract/dashboard/model接口没有匹配到具体版本记录时使用的默认模式，safe=审核期保守文案，offcial=已过审真实文案。请谨慎修改，改错可能导致审核期展示未审核文案的风险。',
       '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `infra_config` WHERE `config_key` = 'custom.contract.default-model' AND `deleted` = b'0'
);
