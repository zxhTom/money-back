-- ==============================================================================
-- 历史人脸存证隔离与分批登记模板 (安全运维只读与受控写入)
-- 禁止在生产环境直接运行未经验证的批量写操作或执行全局 DELETE
-- ==============================================================================

-- 步骤 1：只读盘点待隔离的历史对象（基于已确认的历史命名模式或文件列表）
-- 示例：查询可能属于历史认证材料的记录
-- SELECT id, config_id, path, url, type, size, create_time
-- FROM infra_file
-- WHERE (path LIKE '%.mp4' OR path LIKE '%.jpg')
--   AND path NOT LIKE 'private/%'
--   AND path NOT LIKE 'avatar/%'
-- ORDER BY id ASC;

-- 步骤 2：人工/离线校验确认后，将确认为历史存证的对象录入隔离清单表 infra_file_quarantine
-- 只有在人工复核 hash 及归属后，按具体精确 config_id 与 canonical_path 插入
-- 注意：path_hash 为 canonical_path 原始 UTF-8 字节的 SHA256，用于 uk_config_path_hash 唯一索引与快速查找
-- 示例模板：
-- INSERT INTO infra_file_quarantine (config_id, path_hash, canonical_path, content_hash, reason, creator, create_time, updater, update_time, deleted)
-- VALUES
--   (28, SHA2('20260729/sample_face_token.mp4', 256), '20260729/sample_face_token.mp4', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', '历史人脸存证隔离', '1', NOW(), '1', NOW(), 0)
-- ON DUPLICATE KEY UPDATE reason = VALUES(reason);

-- 步骤 3：对能够可靠绑定归属的历史对象（需同时具备经过数字签名比对确认的 tenant_id, user_id 及 SHA256）
-- 导入 private/face-evidence 并插入 custom_face_evidence 表（不使用真实数据直接导入）
-- INSERT INTO custom_face_evidence (tenant_id, user_id, session_hash, media_type, path, content, size, sha256, creator, create_time, updater, update_time, deleted)
-- SELECT 1, 1024, SHA2('historical_session_token', 256), 'video', 'private/face-evidence/historical-uuid.mp4', content, LENGTH(content), SHA2(content, 256), '1', NOW(), '1', NOW(), 0
-- FROM infra_file_content WHERE config_id = 28 AND path = '20260729/sample_face_token.mp4';

-- 步骤 4：对于无法可靠绑定归属的对象，严格保留在隔离清单中，禁止自动开放
