# 阶段二：后台解锁与风控异常报表开发指南

## 1. 目标
实现后台管理员对被锁用户的“手动解锁”接口，并新增定时任务（Job）来分析用户被锁数据，生成风控异常报告。

## 2. 核心逻辑

### 2.1 手动解锁接口 (Admin API)
**在 `RiskLockService` 中补充：**
- `void unlockUser(Long adminUserId, Long targetUserId)`
- 逻辑：
  1. 使用 MyBatisPlus 从 `system_risk_user_lock_log` 查询该 `targetUserId` 最后一条且尚未结束的锁定记录（或者将所有符合 `lock_end_time > now()` 或为 `null` 的记录查出）。
  2. 将其更新：`unlock_time = now()`, `unlocker_id = adminUserId`, 如果 `lock_end_time` 为空或在未来，则将其改为 `now()`（即立刻结束）。
  3. 删除 Redis 中的限制锁：`stringRedisTemplate.delete("risk:lock_status:" + targetUserId);`
  4. 清除密码错误次数计数器：`stringRedisTemplate.delete("risk:pwd_err:" + targetUserId);`
  5. （可选）重置其在最近 24 小时内的频控 Redis key（这步可忽略，因为解锁后只要重置锁状态就行，频率会自然到期）。

**新建 Controller：**
- 位置：`cn.iocoder.yudao.module.system.controller.admin.risk.RiskLockController`
- API 路径：`PUT /admin-api/system/risk-lock/unlock`
- 参数：`@RequestParam("userId") Long userId`
- 安全校验：可以加上 `@PreAuthorize("@ss.hasPermission('system:risk-lock:update')")`
- 调用：调用 `riskLockService.unlockUser(SecurityFrameworkUtils.getLoginUserId(), userId);`

### 2.2 异常报告生成任务 (Quartz Job)
**新建定时任务类：**
- 位置：`cn.iocoder.yudao.module.system.job.risk.RiskAbnormalReportJob`
- 实现接口：`cn.iocoder.yudao.framework.quartz.core.handler.JobHandler`
- 在 `execute` 方法上添加注解：`@TenantJob` （由于我们需要分别扫描不同租户的数据，这个注解会自动轮询所有租户并注入上下文）。
- **执行逻辑**：
  1. 当前线程已经处于某个租户上下文中。查询 `system_risk_user_lock_log`。
  2. 规则定义：找出那些 `escalation_level >= 3`（或者短时间内被锁超过 3 次）的 `user_id`。为简单起见，这里只需找出数据库中所有 `escalation_level >= 3` 的记录对应的用户。
  3. 对于找出的每一个 `user_id`，检查 `system_risk_abnormal_report` 表中是否已经存在 `status = 0` (待处理) 的报告（避免重复生成）。
  4. 若没有，则生成一条新的告警：
     - `userId = 该用户ID`
     - `abnormalType = "FREQUENT_LOCK"`
     - `description = "用户频繁被风控系统锁定（达到阶梯 3 及以上），存在刷单或爆破风险，请人工核查。"`
     - `status = 0` (0-待处理)。

## 3. 验收要求
- 代码能通过 `mvn compile` 编译。
- API 和 Service 注意异常处理。
- `RiskAbnormalReportJob` 必须带 `@TenantJob` 以保证能够正确处理所有租户的数据。
