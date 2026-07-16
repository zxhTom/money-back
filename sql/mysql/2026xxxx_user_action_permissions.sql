-- 用户列表「更多」操作独立授权资源：重置支付密码 / 禁止自主改密 / 戏耍模式
-- 原先复用 system:user:update / update-password，无法单独授权，现拆分为独立按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive)
VALUES
 (5183, '重置支付密码', 'system:user:reset-pay-password', 3, 8, 100, '', '', NULL, 0, b'1', b'1'),
 (5184, '禁止自主改密', 'system:user:disable-pwd',        3, 9, 100, '', '', NULL, 0, b'1', b'1'),
 (5185, '戏耍模式',     'system:user:tease',              3, 10, 100, '', '', NULL, 0, b'1', b'1');
