# 历史明文身份证刷为密文

## 说明

身份证加密算法在代码中实现（`IdCardCipherUtil` + `yudao.id-card.cipher.secret`），**无法用一条纯 SQL** 完成与线上一致的加密，请使用应用内迁移任务。

## 步骤

1. **备份** `system_users`、`custom_contract` 表。
2. 在 `application.yaml`（或 `application-prod.yaml`）中配置与线上一致的密钥：
   ```yaml
   yudao:
     id-card:
       cipher:
         secret: "你的与线上一致的密钥至少8位建议32位以上"
       migration:
         enabled: true
   ```
3. **启动应用一次**，日志中会出现 `[IdNoCipherMigration][START]` / `[DONE]`。
4. **立即**将 `yudao.id-card.migration.enabled` 改回 `false`，避免重复执行。
5. 验证：用户资料、合同当事人证件号应为 Base64 形态密文；接口返回密文 + `*Display` 展示串。

## 若需离线脚本

可复制 `IdNoCipherMigrationRunner` 中逻辑，在本地用 Java 读取同一 `secret` 调用 `IdCardCipherUtil.encrypt(plain, secret)`，对导出 CSV 批量生成 UPDATE 语句（注意 SQL 转义与事务）。
