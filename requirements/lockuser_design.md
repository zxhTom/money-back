# 账号锁定与风控功能设计 (LockUser)

## 1. 核心需求
根据 `lockuser.md`，实现：
1. 角色级别的 API 访问频率限制，超出频率锁定用户。
2. 频率限制规则可配置（针对全部接口、指定接口）。
3. 阶梯式惩罚算法：多次触发锁定时，锁定时间递增，最终永久禁用。
4. 用户锁定期间禁止访问任何需要登录的接口。
5. 后台管理员可手动解锁、解禁。
6. 异常用户风控检测并生成异常报告（例如频繁短时间请求、频繁密码错误等）。
7. 登录密码错误次数超限锁定。

## 2. 数据库设计 (需要提供 DDL 脚本)
> **原则**：必须支持多租户（继承 `TenantBaseDO`），表必须包含 `tenant_id` 及其它基础字段，以兼容线上随意开启/关闭租户的回退能力。

### 2.1 频率规则配置表 (`risk_rate_limit_config`)
- `id` bigint
- `role_id` bigint (为空代表全局)
- `api_pattern` varchar (接口匹配模式，支持 Ant 风格或具体 URI)
- `time_window` int (时间窗口，秒)
- `max_count` int (窗口内最大请求次数)
- `lock_duration_ladder` varchar (阶梯锁定时间，如 "300,3600,86400,-1" 表示 5分钟、1小时、1天、永久)
- `status` tinyint (状态：启用/禁用)
- `tenant_id` bigint NOT NULL DEFAULT 1
- `creator`, `updater`, `create_time`, `update_time`, `deleted`

### 2.2 用户锁定记录表 (`risk_user_lock_log`)
- `id` bigint
- `user_id` bigint
- `lock_type` varchar (锁定类型：RATE_LIMIT, PASSWORD_ERR, MANUAL, RISK_CONTROL)
- `lock_start_time` datetime
- `lock_end_time` datetime (为空代表永久禁用)
- `escalation_level` int (阶梯层级，记录这是第几次惩罚)
- `unlock_time` datetime (手动解锁时间，为空代表未手动解锁)
- `unlocker_id` bigint (解锁人ID)
- `tenant_id` bigint NOT NULL DEFAULT 1
- `creator`, `updater`, `create_time`, `update_time`, `deleted`

### 2.3 异常报告表 (`risk_abnormal_report`)
- `id` bigint
- `user_id` bigint
- `abnormal_type` varchar (异常类型)
- `description` varchar (描述)
- `status` tinyint (处理状态：0-待处理，1-已处理)
- `tenant_id` bigint NOT NULL DEFAULT 1
- `creator`, `updater`, `create_time`, `update_time`, `deleted`

## 3. Redis 缓存设计
1. **密码错误计数**：`risk:pwd_err:{userId}` (Value为错误次数，TTL为24小时)
2. **API频率计数**：`risk:api_limit:{ruleId}:{userId}` (使用 Redis 滑动窗口或 INCR+EXPIRE)
3. **用户当前锁定状态**：`risk:lock_status:{userId}` (Value为锁定结束时间戳或-1，拦截器直接读缓存快速拦截)

## 4. 拦截器/过滤器设计
新增 `RiskUserLockInterceptor` 或在 `TokenAuthenticationFilter` 校验逻辑中：
在获取到登录用户后，检查 Redis 中的 `risk:lock_status:{userId}`。
- 如果被锁定且未到期，抛出自定义异常 (如 "您的账号已被锁定，请联系管理员")。
- 如果是永久禁用，提示 "您的账号已被永久禁用"。

## 5. 开发任务切分
为了保证代码质量，我们将分步骤由 Subagent (开发代理) 来开发：
- **阶段一 (Phase 1)**：完成数据库 DDL、实体类 (DO/Mapper/Service)、密码错误锁定的基本逻辑。
- **阶段二 (Phase 2)**：完成 API 频率限制拦截器、阶梯惩罚算法与 Redis 记录。
- **阶段三 (Phase 3)**：完成后台的手动解锁接口、风控异常报告生成的异步逻辑。

