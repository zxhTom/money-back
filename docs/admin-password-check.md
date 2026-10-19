# 管理员密码核验

只读接口，不调用登录、发放令牌、改密或支付服务，不修改被核验用户的登录失败计数。

前缀 `/admin-api/custom/admin/password-check`：

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | `/capability` | 已登录用户是否为超级管理员或租户管理员，用于小程序入口展示 |
| GET | `/users?keyword=...` | 在当前租户按用户名/昵称搜索，最多 20 条，只返回 username、nickname |
| POST | `/verify` | JSON `{username,password,kind}`；kind 为 login 或 pay；CommonResult.data 返回布尔值 |

搜索和核验在接口及服务层均限制 `super_admin` / `tenant_admin`。查询显式限定当前租户，排除逻辑删除用户；不接受请求体中的租户编号。用户名精确匹配，密码不去除空格。用户不存在、同租户用户名不唯一或密码未设置时返回 false。

核验接口每位管理员每分钟最多 10 次，访问日志不记录请求体及响应值。密码字段只允许反序列化，DTO 不生成包含字段的 toString；JDBC 读取密码列，避免 MyBatis StdOutImpl 将哈希写入结果日志。最终复用现有 AdminUserService.isPasswordMatch 比对。

配套小程序代码位于 `zxhTom/contract-miniprogram` 的 `feature/admin-password-check` 分支，入口为“我的 → 密码核验”。提交仅包含本功能，未合并其他本地改动。本次后端仅部署预环境，未执行正式发布或蓝绿切换。

验证：针对本功能的 Java 8 目标编译及 7 项 JUnit 检查通过；预环境验证管理员可访问、普通用户调用返回 403、匿名请求返回 401，以及原节点看板接口可用。正确密码比对使用合成 BCrypt 测试数据，不尝试核验真实用户的密码。未运行 Maven 全量构建。
