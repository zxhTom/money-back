# 人脸存证私有化与历史材料隔离处置操作指南

## 1. 概述与安全原则

历史人脸核身材料此前直接经由普通存储链处理，可能处于公开存储或缺乏细粒度隔离保护，且未查线上实际 ACL 配置。本次实施完成私有存证（`custom_face_evidence`）及历史文件存储隔离（`infra_file_quarantine`）。

核心安全约束：
- **禁止一刀切封禁**：不能按所有 jpg/mp4 一刀切删除或封禁，以免影响头像与合同附件。
- **精确隔离**：基于 `config_id + canonical_path`（以及可选的 `content_hash`）在应用层封堵下载、预签名、列表及登记。
- **对象存储联动**：应用层拦截生效后，生产对象存储原始地址必须配合修改 ACL 为私有，或将对象移入私有桶并执行 CDN 缓存失效；不能只依赖后端封堵撤销历史公网暴露。
- **严禁无主开放**：不能可靠确认归属的历史对象，必须保留在隔离状态中，禁止回滚为公开可读。

## 2. 处置与迁移流程

### 步骤 1：只读盘点待隔离材料
在离线或只读副本中执行查询，列出疑似存证对象，盘点大小、存储桶位置及哈希值：
```sql
SELECT id, config_id, path, url, type, size, create_time
FROM infra_file
WHERE (path LIKE '%.mp4' OR path LIKE '%.jpg')
  AND path NOT LIKE 'private/%'
  AND path NOT LIKE 'avatar/%';
```

### 步骤 2：录入应用隔离清单
通过工单审计确认历史存证路径列表，按批次导入 `infra_file_quarantine`。导入后通用文件接口立即对这些对象执行 Fail-Closed 拦截。模板详见 `sql/mysql/face_evidence_quarantine_template.sql`。

### 步骤 3：云存储 ACL 与 CDN 处置
1. 确认已录入清单的对象在 OSS/MinIO 中的 key。
2. 将对象 ACL 设置为 `private`，或拷贝移入独立私有存证 bucket 并从公开 bucket 移除。
3. 对 CDN 执行 URL 路径失效/刷新操作，清除边缘节点公网缓存。

### 步骤 4：可信归属存证导入
对于具备可靠归属关系（具备可信验证的 tenant_id 与 user_id）的历史对象，校验其大小与 SHA256 无误后，分批写入 `custom_face_evidence` 表，并在 `path` 中赋予 `private/face-evidence/<uuid>.<ext>`。

### 步骤 5：无法归属材料保留隔离
对缺少可靠验证会话、无可靠 user_id 绑定的历史材料，永久保留在 `infra_file_quarantine` 隔离表中，仅允许审计复核，绝不开放下载。

## 3. 回滚策略

如迁移或隔离配置出现意外阻断：
- 可针对具体误录入的 `canonical_path` 在 `infra_file_quarantine` 表中标记 `deleted = 1` 恢复通用文件服务。
- **严禁将桶或历史对象权限批量恢复为公开可读（public-read）**。
