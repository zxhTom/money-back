# 阶段二：全局 API 频率限制拦截器开发指南

## 1. 目标
实现 `RiskRateLimitInterceptor`，用于全局拦截 HTTP 请求，比对 API 频控规则，当请求超频时自动锁定账号。

## 2. 核心逻辑

### 2.1 规则加载缓存
在 `RiskLockService` 中新增方法：
- `List<RiskRateLimitConfigDO> getAllEnableRateLimitRules()`
该方法从数据库加载所有 `status = 0` (启用) 的规则。为了性能，可以将其缓存到 JVM 内存或 Redis 中（或每次请求查库，若并发不高。推荐使用 `@Cacheable` 或手动 Redis 缓存，或者 `@PostConstruct` 加载到本地 Map 中并在后台更新时刷新）。
**注意**：规则带有 `tenant_id`，查询时自动会走租户隔离。如果租户开关关闭，查出来的也是正常配置。

### 2.2 拦截器 `RiskRateLimitInterceptor`
位置：`yudao-module-system/src/main/java/cn/iocoder/yudao/module/system/framework/web/core/RiskRateLimitInterceptor.java`
继承：`HandlerInterceptor`
逻辑：
1. 从 `SecurityFrameworkUtils.getLoginUserId()` 获取当前登录用户 ID。如果没有登录，则直接放行。
2. 获得当前请求的 URI `request.getRequestURI()`。
3. 遍历所有的启用的频控规则 `RiskRateLimitConfigDO`。
4. 使用 `AntPathMatcher` 匹配 `rule.apiPattern` 和当前 URI。如果匹配，则检查该规则是否适用于该用户（如果 `rule.roleId` 不为空，则校验用户是否拥有该角色；如果为空，则适用于所有人）。
5. 适用该规则时，使用 Redis 计数器校验频率：
   - Key: `risk:api_limit:{rule.id}:{userId}`
   - 使用 `opsForValue().increment(key)`
   - 如果为 1，使用 `expire(key, rule.timeWindow, TimeUnit.SECONDS)`
   - 如果 count > `rule.maxCount`，说明触发限流！
6. 触发限流的处理：
   - 抛出自定义异常拦截当前请求（如 "接口访问过于频繁，账号已被锁定"）。
   - 调用 `riskLockService.escalateLock(userId, "RATE_LIMIT", rule)` 进行阶梯锁定。

### 2.3 阶梯锁定方法 `escalateLock`
在 `RiskLockService` 增加方法 `void escalateLock(Long userId, String lockType, RiskRateLimitConfigDO rule)`:
1. 从 `system_risk_user_lock_log` 查询该用户最近一次被该类规则锁定的记录，获取其 `escalation_level`。如果没有记录或已是很久以前，则为 1。
2. 解析 `rule.lockDurationLadder` (如 "300,3600,-1")。根据 `escalation_level` 决定本次锁定的时间。
   - 层级 1 取第 1 个值，层级 2 取第 2 个值...如果越界则取最后一个值。
3. 使用阶段一写好的 `lockUser` 私有方法（需修改支持阶梯等级），向 DB 插入新的锁定日志，并更新 Redis 锁。如果取到 `-1`，代表永久锁定（Redis可以设一个极大的过期时间，DB 结束时间设为 NULL）。

### 2.4 注册拦截器
在 `cn.iocoder.yudao.module.system.framework.web.config.SystemWebConfiguration` 中配置注册 `RiskRateLimitInterceptor`。

## 3. 验收要求
- 新增的代码必须能够通过 `mvn compile` 编译。
- 使用 `TenantBaseDO` 等多租户安全规范。
