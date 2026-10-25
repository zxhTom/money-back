# 平台与运维安全修复实施记录 (2026-10-24)

## 1. 概述与范围
本次修复严格遵循 `requirements/handoff_security_remaining.md` 及安全设计评审要求，仅实施以下三个后端模块的安全强化（不涉及客户端、人脸存证及 A 组合同业务逻辑）：
- **B (SEC-06/07)**：全局诊断定向 WebSocket 消息与租户/用户隔离缓存。
- **C (SEC-08/11)**：平台文件配置权限守卫（PlatformFileConfigGuard）与 Spring Boot Admin 监控路径平台管理员访问控制。
- **D (SEC-09 - 仅后端部分)**：`SocialClientServiceImpl.getAuthUser` 敏感业务日志剔除。

---

## 2. 实施细节与代码变更

### 2.1 B 组 (SEC-06/07)：诊断消息与缓存隔离
- **根因**：原 `GlobalDiagnoseAsyncTask` 中 WebSocket 推送采用全局管理员广播（`sendObject(ADMIN, ...)` 无目标用户 ID），容易泄露敏感系统指标；原 `GlobalDiagnoseServiceImpl` 缓存键与锁键为共享单租户格式（如 `diagnose:scan:result:%d`），且释放锁时无凭证比较直接删除。
- **修复方案**：
  1. 新增不可变任务上下文 `GlobalDiagnoseTaskContext` 与缓存信封 `GlobalDiagnoseCacheEnvelope`，包含 `tenantId`、`userId`、`taskType`、`days`、`lockToken`。
  2. 新增 `GlobalDiagnoseAccessGuard`：
     - 在入口强校验 `LoginUser.getUserType() == ADMIN`、`TenantContextHolder.getTenantId() == LoginUser.getTenantId()`、发起用户处于启用状态（`status == ENABLE`）、发起用户属于该租户，并持有诊断权限（`custom:security:global-diagnose` 或 `mini:admin:security:diagnose`）。
     - 提供 `canPushResult` 校验，在异步推送前再次复核用户状态和权限，一旦用户被禁用或撤权立即停止敏感结果推送。
  3. `GlobalDiagnoseServiceImpl`：
     - 使用隔离版本前缀键：`diagnose:v2:scan:result:{tenantId}:{userId}:{days}`、`diagnose:v2:behavior:result:{tenantId}:{userId}` 及对应的 `running` 锁键。
     - 读取缓存时解析 `GlobalDiagnoseCacheEnvelope`，核验所属 `tenantId`、`userId`、`taskType` 及 `days`，防止伪造、串读或天数错用。
     - 分布式锁在任务触发前生成随机 UUID，构造不可变 `GlobalDiagnoseTaskContext`。
  4. `GlobalDiagnoseAsyncTask`：
     - 显式通过 `TenantUtils.execute(tenantId, ...)` 执行，确保异步线程绑定正确的租户上下文并在结束时恢复。
     - 进度（`diagnose-progress`）、完成（`diagnose-done`）、错误（`diagnose-error`）统一通过 `webSocketSenderApi.sendObject(ADMIN, userId, event, msg)` **定向推送**给发起用户，且推送与鉴权均在发起租户上下文中执行并恢复。
     - 异常或任务完成时，通过 **Redis Lua 脚本**（`if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end`）进行原子比对并释放锁，防止误删新任务持有的锁。
     - 错误推送使用安全文案，避免原样输出底层异常堆栈。
- **修改文件**：
  - `yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/monitor/GlobalDiagnoseTaskContext.java` (新)
  - `yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/monitor/GlobalDiagnoseCacheEnvelope.java` (新)
  - `yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/monitor/GlobalDiagnoseAccessGuard.java` (新)
  - `yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/monitor/GlobalDiagnoseServiceImpl.java`
  - `yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/monitor/GlobalDiagnoseAsyncTask.java`

### 2.2 C 组 (SEC-08/11)：平台文件配置与监控路径权限
- **根因**：原 `FileConfigController` 允许普通租户管理员（`tenant_admin`、`contract-manager`）配置存储；原 `SecurityConfiguration` 将 Spring Boot Admin 监控路径 `adminSeverContextPath` 设置为 `permitAll()`，且若配置为空或异常字符会产生通配放行隐患。
- **修复方案**：
  1. 新增 `PlatformFileConfigGuard`：
     - 校验 `LoginUser.getUserType() == ADMIN`。
     - 校验 `tenantProperties.getDefaultTenantId()`（若无配置拒绝全部平台操作）。
     - 校验 `LoginUser.getTenantId() == defaultTenantId`（token tenant 缺失或不匹配一律拒绝，绝不无凭证兜底）。
     - 若请求头带有 `tenant-id`，必须与平台 `defaultTenantId` 一致；若无请求头，允许合法 token 平台租户。
     - 在 `TenantUtils.execute(defaultTenantId, ...)` 上下文中判断角色 `super_admin` 后恢复上下文。
  2. `FileConfigController`：
     - 所有管理端点（`create`、`update`、`update-master`、`delete`、`delete-list`、`get`、`page`、`test`）的方法级权限收紧为：
       `@PreAuthorize("@platformFileConfigGuard.isPlatformSuperAdmin() and @ss.hasPermission(...)")`。
     - 保留 `getMasterFileConfigId()` 供普通登录用户上传流程内部使用。
  3. `FileConfigServiceImpl`：
     - 在写操作（`create`、`update`、`updateMaster`、`delete`、`deleteList`）及 `testFileConfig` 强制无条件执行服务层调用守卫 `platformFileConfigGuard.requirePlatformSuperAdmin()`，防止 fail-open。
     - `getFileConfig`、`getMasterFileClient` 等供系统内部文件上传调用的读取接口不受限，确保普通用户文件上传不受影响。
  4. Spring Boot Admin 监控路径强化：
     - 新增 `PlatformAdminAuthorizationManager`，将 Spring Boot Admin 端点访问限定为平台超级管理员（通过 `PlatformFileConfigGuard.isPlatformSuperAdmin()` 鉴权）。
     - 在 `SecurityConfiguration` 中严格校验 `adminSeverContextPath`：必须为固定绝对路径（以 `/` 开头且非单个 `/`），严格排除包含 `*`、`?`、`{`、`}`、`..`、`//`、`\` 等异常或通配字符；仅合法固定绝对路径才注册平台管理员专属授权，绝不注册 `/**` 规则。
- **修改文件**：
  - `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/framework/security/core/PlatformFileConfigGuard.java` (新)
  - `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/framework/security/core/PlatformAdminAuthorizationManager.java` (新)
  - `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/controller/admin/file/FileConfigController.java`
  - `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/service/file/FileConfigServiceImpl.java`
  - `yudao-module-infra/src/main/java/cn/iocoder/yudao/module/infra/framework/security/config/SecurityConfiguration.java`
  - `yudao-module-mini/src/test/java/cn/iocoder/yudao/module/custom/security/FileManagementBoundaryTest.java` (适配平台管理员 SpEL 守卫断言)

### 2.3 D 组 (SEC-09)：业务敏感日志清理
- **根因**：`SocialClientServiceImpl.getAuthUser` 在处理第三方登录回调时，原样通过 `toJsonString(authCallback)` 和 `toJsonString(authResponse)` 打印日志，导致授权临时 `code`、`state`、`token` 以及用户个人资料（昵称、头像等）泄露到生产日志。
- **修复方案**：
  - 删除回调与响应的全量序列化日志。
  - 成功时仅输出 `[getAuthUser][请求社交平台成功 type({})]`。
  - 失败时仅输出 `[getAuthUser][请求社交平台失败 type({}) code({})]`。
- **修改文件**：
  - `yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/service/social/SocialClientServiceImpl.java`

---

## 3. 回归测试清单与覆盖行为
创建并补充了正反例行为测试，包含但不限于：
1. `GlobalDiagnoseSecurityTest` (`yudao-module-system`):
   - 双租户、同租户双用户隔离性测试：断言生成的 Redis 运行锁与结果缓存键相互隔离。
   - 缓存信封校验测试：跨租户、跨用户、不同 days 伪造或篡改缓存数据直接判定未命中并重新触发，合法信封直接命中。
   - 定向 WebSocket 推送测试：断言消息定向推给触发管理员，拒绝全局广播；管理员撤权（Permission `true -> false`）或禁用后终止推送。
   - 分布式锁原子释放（Compare-and-Delete）测试：断言实际引用生产 `GlobalDiagnoseAsyncTask.RELEASE_LOCK_SCRIPT` 执行原子比对，真实匹配/不匹配 token，旧任务绝不误删新任务锁。
2. `DiagnoseSqlDualTenantDbTest` & `DiagnoseMapperAnnotationVerificationTest` (`yudao-module-system`):
   - 生产 `LoginLogMapper`、`OperateLogMapper`、`DataAccessLogMapper` 的诊断 SQL 通过 MyBatis 真实解析与注册验证。
   - H2 测试从生产 `MappedStatement` 构建 `BoundSql` 并执行，仅把 `DATE_SUB` 改写为等价 `DATEADD`；覆盖登录、操作、数据访问日志查询及租户、用户类型、删除状态、未知用户隔离。
3. `PlatformGuardContextTest` & `PlatformFileConfigSecurityTest` (`yudao-module-infra`):
   - 最小 Spring 上下文运行（`FakeSecurityFrameworkService`）：验证租户禁用且 `defaultTenantId` 缺失时的 fail-closed 默认拒绝行为。
   - 验证配置有效默认租户时平台管理员（`super_admin`）访问成功、非平台身份/非管理员拒绝。
   - 租户访问态（`visitTenantId`）绕过防御与 SpEL 方法级权限拦截。
4. `BaiduFaceAuthResultTest` (`yudao-module-mini`):
   - 网络请求与 token 获取统一脱敏异常边界：断言 `getAccessToken`/`sendPost` 异常时返回通用安全文案，通过 Logback 日志捕获（`ListAppender`）断言 provider 原始报文、异常堆栈或敏感密钥不泄漏至日志。
   - 入口访问控制测试：未登录、普通会员、被禁用管理员、租户 ID 不匹配均被拒绝。
2. `PlatformFileConfigSecurityTest` (`yudao-module-infra`):
   - 平台超管（`ADMIN` 用户类型、默认平台租户、`super_admin` 角色）放行。
   - 租户管理员有细分权限但租户不是平台默认租户，拒绝。
   - 会员用户与管理员同 ID（如 ID=1），用户类型为 `MEMBER`，拒绝。
   - 平台超管角色但 token 租户为非平台租户，拒绝。
   - token tenant 缺失直接拒绝，不默认补齐。
   - 禁用角色或缺少角色，拒绝。
   - 伪造 header `tenant-id` 与平台租户不匹配，拒绝。
   - 无 header 但 token 租户合法，允许。
   - 服务层写与测试操作强制执行守卫测试。
   - 内部读取与 `getMasterFileConfigId` 在普通用户上传流程下可用性验证。
   - Controller 层 `@PreAuthorize` SpEL 表达式精确求值验证。
3. `AdminMonitorSecurityTest` (`yudao-module-infra`):
   - 匿名访问 Spring Boot Admin 监控路径拒绝。
   - 普通会员访问监控路径拒绝。
   - 普通租户管理员访问监控路径拒绝。
   - 平台超管访问监控路径放行。
   - 空、空白、`/`、`/**`、`//`、`..` 等异常字符绝不注册通配规则。
   - 严格固定绝对 path 正确注册 `path` 和 `path/**` 平台管理员 matcher。
4. `SocialAuthLogSecurityTest` (`yudao-module-system`):
   - 采用合成敏感标识（如 `SYNTHETIC_AUTH_CODE_XYZ`、用户隐私昵称等）注入。
   - 捕获真实 Logback 日志事件，断言在成功与失败路径下均无任何敏感凭据或个人资料输出。

---

## 4. 部署与环境注意事项
1. **旧诊断缓存键处置**：
   本次改造将诊断键升级为带有版本及多维标识的 `diagnose:v2:...`。旧有单租户共享键 `diagnose:scan:result:*` 等不再读取，生产环境 Redis 中已有旧键可等待其原有 TTL 自然过期，无需执行危险的批量全库扫描命令（`keys *`）。
2. **Spring Boot Admin 监控访问变更**：
   原监控端点（如 `/admin`）已关闭匿名访问。监控服务端或客户端如果通过 HTTP 探活或抓取指标，必须携带具备平台管理员身份的合法 Bearer 认证 Token。
3. **平台默认租户配置依赖**：
   `yudao.tenant.default-tenant-id` 是平台文件存储管理和监控访问的关键依托，必须在生产配置（如 `application-prod.yaml`）中正确配置；若缺失该配置，系统安全守卫将基于安全优先原则拒绝所有平台级操作。

## 5. Verification & Test Execution Results

Final verification ran on 2026-10-25 after the review fixes, including the synchronous global-diagnose service guard. Across infra, system, and mini, the recorded test totals were **170 tests, 0 failures, 0 errors, 6 skipped** (`BUILD SUCCESS`); all six skips are in `S3FileClientTest`. `GlobalDiagnoseSecurityTest` completed 16 cases, including rejected unauthenticated, missing/mismatched tenant, disabled-user, and no-permission calls, plus a valid tenant-scoped call.

```bash
mvn -o -pl yudao-module-mini -am -Dtest='*FaceEvidence*Test,*FileQuarantine*Test,S3FileClientTest,FaceAuthSessionGuardTest,BaiduFaceAuthResultTest,FaceAuthControllerTest,MiniCallbackControllerTest,MiniCallbackTemplateTest,PrivacyHardeningTest,LeakBoundaryRegressionTest,FileManagementBoundaryTest,ContractAttachmentReferenceGuardTest,RiskLockControllerTest,CreditQueryAccessCheckerTest,*ContractQueryScope*Test,*ContractTotalInfos*Test,*GlobalDiagnose*Test,*PlatformFileConfig*Test,*AdminMonitorSecurity*Test,*SocialAuthLog*Test,*PlatformGuardContext*Test,*DiagnoseMapper*Test,*DiagnoseSqlDualTenant*Test' -Dsurefire.failIfNoSpecifiedTests=false -Dflatten.skip=true test
```

`DiagnoseSqlDualTenantDbTest` now has three H2 tests. It executes production `LoginLogMapper`, `OperateLogMapper`, and `DataAccessLogMapper` `MappedStatement` SQL using their `BoundSql` and parameter mappings. The only test adaptation is the MySQL date expression converted to H2 `DATEADD`; no diagnostic query is copied into a test mapper. Coverage includes login IP associations, login and hourly counts, last and distinct IPs, operation-log IP associations, data-access IP associations and distinct IPs, with tenant 1/2, ADMIN/MEMBER, deleted rows/users, and unknown users.

The targeted final face/callback and mapper verification command completed with **28 tests, 0 failures, 0 errors**. This includes the malformed `idcard_confirm` sentinel test and the callback path that asserts the provider marker does not appear in response data or captured logs.

## 6. Deployment & Runtime Notes
1. **Tenant disabled mode compatibility**:
   - When multi-tenancy is disabled (`yudao.tenant.enable=false`), `TenantProperties` bean is absent.
   - `PlatformFileConfigGuard` gracefully resolves `defaultTenantId` via fallback property `@Value("${yudao.tenant.default-tenant-id:#{null}}")`. Platform admins are not locked out.
   - If neither bean nor property exists, the guard fails closed.
2. **Diagnosis Redis Cache Isolation**:
   - V2 cache and lock keys use `diagnose:v2:...` namespace with envelope validation preventing cross-tenant or mismatched parameter pollution.
   - Lock release uses Lua script compare-and-delete to avoid deleting newer task tokens.
3. **Sensitive Logs**:
   - `SocialClientServiceImpl` emits high-level audit summaries only, removing token and raw user JSON from logs.
