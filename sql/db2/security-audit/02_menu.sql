-- ============================================================
-- 审计日志 + 安全监控 菜单权限（DB2）
-- 前置条件：system_menu 表中 id=108「审计日志」和 id=1「系统管理」必须存在
-- ============================================================

BEGIN
    DECLARE v_audit_log_menu_id  BIGINT;
    DECLARE v_security_dir_id    BIGINT;
    DECLARE v_alert_menu_id      BIGINT;
    DECLARE v_blacklist_menu_id  BIGINT;

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('操作审计', '', 2, 3, 108, 'custom-audit-log', 'ep:notebook',
            'custom/auditLog/index', 'CustomAuditLog',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');
    SET v_audit_log_menu_id = IDENTITY_VAL_LOCAL();

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('操作审计查询', 'custom:audit-log:query', 3, 1, v_audit_log_menu_id, '', '', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('操作审计导出', 'custom:audit-log:export', 3, 2, v_audit_log_menu_id, '', '', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('安全监控', '', 1, 10, 1, 'security-monitor', 'ep:warning', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');
    SET v_security_dir_id = IDENTITY_VAL_LOCAL();

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('告警管理', '', 2, 1, v_security_dir_id, 'alert', 'ep:bell',
            'custom/security/alert/index', 'CustomSecurityAlert',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');
    SET v_alert_menu_id = IDENTITY_VAL_LOCAL();

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('告警查询', 'custom:security:alert:query', 3, 1, v_alert_menu_id, '', '', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('告警处理', 'custom:security:alert:handle', 3, 2, v_alert_menu_id, '', '', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('IP黑名单', '', 2, 2, v_security_dir_id, 'blacklist', 'ep:lock',
            'custom/security/blacklist/index', 'CustomSecurityBlacklist',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');
    SET v_blacklist_menu_id = IDENTITY_VAL_LOCAL();

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('黑名单查询', 'custom:security:blacklist:query', 3, 1, v_blacklist_menu_id, '', '', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('封禁IP', 'custom:security:blacklist:add', 3, 2, v_blacklist_menu_id, '', '', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');

    INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name,
                             status, visible, keep_alive, creator, create_time, updater, update_time, deleted)
    VALUES ('解封IP', 'custom:security:blacklist:remove', 3, 3, v_blacklist_menu_id, '', '', '', '',
            0, '1', '0', '1', CURRENT TIMESTAMP, '1', CURRENT TIMESTAMP, '0');
END
