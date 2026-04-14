# script 目录说明

本目录存放与部署、本地联调、数据生成相关的脚本与配置模板。按子路径分节说明。

---

## `generate_mock_system_users.py`

用于生成 **`system_users`** 模拟用户的 **MySQL 插入 SQL** 与 **本地账本 JSON**（用于区分本脚本生成的账号，数据库中不留“模拟”标记）。

| 能力 | 说明 |
|------|------|
| 身份证 | GB 11643 校验位正确，真实县级区划码 + 合法公历生日 |
| 冲突处理 | `INSERT ... SELECT ... WHERE NOT EXISTS`：同租户未删除下，**username、nickname、realname、id_no** 任一同值即跳过；**mobile / email** 仅在非空时参与比较。`--minimal` 旧表无 `realname`/`id_no` 列时不校验这两项 |
| 邮箱 | 默认随机 **@qq.com**（9 位数字）或 **@163.com**（字母数字）；`--no-email` 时 `email` 为空串 |
| 显示名 | **username、nickname、realname** 三者相同（中文姓名；本批次内重名时自动在姓名后加数字，保证 username 唯一） |
| 账本 | 写入 `script/mock_users_ledger/`，含批次与用户明细（含敏感信息，勿提交仓库；根目录 `.gitignore` 已忽略） |
| 输出 SQL | 默认 `script/out/mock_system_users_<batch>.sql` |
| 创建时间 | **create_time** 与 **update_time** 相同；约 **50%** 落在最近 **3 天**、**30%** 在 **3～10 天前**、**20%** 在 **10～30 天前**（各段内均匀随机，行顺序打乱） |

**依赖**

```bash
pip install bcrypt
```

未安装 `bcrypt` 时，仅默认密码 `123456` 可用内置哈希。

**常用参数**

| 参数 | 说明 |
|------|------|
| `--count` | 生成用户数（默认 10） |
| `--tenant-id` | 租户 ID（默认 1） |
| `--dept-id` | 部门 ID，不设则为 SQL 中 `NULL` |
| `--mobile-prefix` | 手机号前 3 位（默认 `199`）；与 `--no-mobile` 互斥生效 |
| `--no-mobile` | 不生成手机号，`mobile` 为空串 |
| `--no-email` | 不生成邮箱，`email` 为空串 |
| `--password-plain` | 登录/支付密码明文（bcrypt 写入） |
| `--minimal` | 仅基础列（旧表无 `id_no`/`realname`）；NOT EXISTS 不校验这两项，其余字段仍校验 |
| `--out-sql` / `--ledger` | 自定义输出路径 |

**示例**

```bash
python3 script/generate_mock_system_users.py --count 20 --tenant-id 1 --dept-id 103
python3 script/generate_mock_system_users.py --count 10 --no-mobile
python3 script/generate_mock_system_users.py --count 10 --no-email
python3 script/generate_mock_system_users.py --count 5 --minimal
```

---

## `shell/deploy.sh`

服务器上 **Jar 热部署** 用 Bash 脚本：备份旧包 → 停止进程 → 拷贝新 `yudao-server.jar` → 后台启动 → **Actuator 健康检查**（或等待人工看日志）。

脚本内可改：

- `BASE_PATH`、`SOURCE_PATH`、`SERVER_NAME`、`PROFILES_ACTIVE`
- `HEALTH_CHECK_URL`（默认 `http://127.0.0.1:48080/actuator/health/`）
- `JAVA_OPS`、可选 SkyWalking `JAVA_AGENT`

典型配合 **Jenkins**：构建产物拷到 `SOURCE_PATH`，再在目标机执行本脚本。使用前请按实际路径与 Spring Profile 修改变量。

---

## `jenkins/Jenkinsfile`

**Jenkins Pipeline（Declarative）** 示例：检出代码 → `mvn clean package`（可选从 `~/resources` 覆盖 `yaml`）→ 拷贝 `deploy.sh` 与 jar 到部署目录 → 执行部署脚本。

内含 **DockerHub / GitHub / Kubeconfig** 等凭证 ID 与仓库地址占位，需按你们环境改成当前仓库与凭证；其中 Git 地址仍指向示例远程仓，**不可直接用于生产**，仅作流水线结构参考。

---

## `idea/http-client.env.json`

**IntelliJ HTTP Client** 环境变量文件，供 `.http` 请求引用 `{{baseUrl}}`、`{{token}}` 等。

| 环境名 | 用途 |
|--------|------|
| `local` | 直连本机后端：`127.0.0.1:48080` 的 `admin-api` / `app-api` |
| `gateway` | 经网关：`127.0.0.1:8888` |

`token`、`adminTenantId`、`appToken`、`appTenantId` 为示例占位，请按登录接口返回与租户实际值替换。

---

## `docker/`

本地或单机用 **Docker Compose** 拉起 **MySQL、Redis、后端、管理端前端** 的编排与说明。

| 文件 | 说明 |
|------|------|
| `docker-compose.yml` | 服务定义：`mysql`（初始化 SQL 挂载）、`redis`、`server`（`yudao-server` 镜像构建）、`admin`（`yudao-ui-admin` + Nginx） |
| `docker.env` | 可传给 `docker compose --env-file` 的环境变量：库名、密码、数据源 URL、Redis、前端构建参数等 |
| `Docker-HOWTO.md` | 构建 jar、Compose 启动、端口说明（8080 管理端、48080 API、3306/6379） |

**注意**：Compose 中 `build.context` 指向 `./yudao-server/`、`./yudao-ui-admin`，需与仓库实际前端/后端目录布局一致；若路径不同，请同步修改 `docker-compose.yml` 或调整目录结构。

更细步骤见同目录 **`Docker-HOWTO.md`**。

---

## 目录结构一览

```text
script/
├── README.md                        # 本说明
├── generate_mock_system_users.py    # 模拟 system_users + SQL/账本
├── shell/
│   └── deploy.sh                    # Jar 部署与健康检查
├── jenkins/
│   └── Jenkinsfile                  # CI 流水线示例
├── idea/
│   └── http-client.env.json         # IDEA HTTP Client 环境
└── docker/
    ├── docker-compose.yml
    ├── docker.env
    └── Docker-HOWTO.md
```
