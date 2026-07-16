-- 合同回收站 + 归档查询 页面菜单与权限
INSERT IGNORE INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive)
VALUES
 (5191, '合同回收站', 'custom:contract:recycle', 2, 80, 5083, 'contract-recycle', 'ep:delete', 'custom/contract/recycle/index', 'ContractRecycle', 0, b'1', b'1'),
 (5192, '归档查询',  'custom:security:archive', 2, 81, 5083, 'archive-query', 'ep:files', 'custom/security/archiveQuery/index', 'ArchiveQuery', 0, b'1', b'1');
