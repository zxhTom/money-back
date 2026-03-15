# 时间窗口管理 - 接口约定

## 1. 数据模型

### 1.1 TimeWindowVO（列表/详情）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 主键 |
| startTime | string | 开始时间，建议 ISO8601 或 `yyyy-MM-dd HH:mm:ss` |
| endTime | string | 结束时间 |
| quickSelect | number \| null | 快捷选择：1 / 3 / 7，可选，用于回显 |
| selectedUserIds | number[] | 选定用户 ID 列表 |
| excludedUserIds | number[] | 排除用户 ID 列表 |
| status | number | 激活状态：0 未激活，1 激活（与项目 COMMON_STATUS 一致时可用 0/1） |
| createTime | string | 创建时间 |
| updateTime | string | 更新时间 |

列表可只返回部分字段；详情建议返回完整字段。若后端用「用户名称」等做展示，可增加 `selectedUserNames`、`excludedUserNames` 等，前端可选展示。

### 1.2 创建/更新请求体

- **创建** TimeWindowCreateReqVO：除 `id`、`createTime`、`updateTime` 外与 VO 一致；时间、用户列表、状态必填（按业务约定）。
- **更新** TimeWindowUpdateReqVO：在创建基础上增加 `id`（必填）。

建议字段：

- startTime, endTime（string）
- quickSelect（number | null，可选）
- selectedUserIds（number[]）
- excludedUserIds（number[]）
- status（number）

## 2. 接口列表

### 2.1 分页列表

- **GET** `/custom/time-window/page`
- **Query**：`pageNo`, `pageSize`, `status`（可选）, 其他筛选（如时间范围）按需
- **Response**：`{ code, msg, data: { list: TimeWindowVO[], total: number } }`

### 2.2 详情

- **GET** `/custom/time-window/get?id={id}`
- **Response**：`{ code, msg, data: TimeWindowVO }`

### 2.3 新增

- **POST** `/custom/time-window/create`
- **Body**：TimeWindowCreateReqVO
- **Response**：统一成功结构

### 2.4 更新

- **PUT** `/custom/time-window/update`
- **Body**：TimeWindowUpdateReqVO
- **Response**：统一成功结构

### 2.5 删除

- **DELETE** `/custom/time-window/delete?id={id}`
- **Response**：统一成功结构

### 2.6 重合校验

- **POST** `/custom/time-window/check-overlap`
- **Body**：
  - `startTime`（string）
  - `endTime`（string）
  - `excludeId`（number | null，修改时传当前记录 id）
- **Response** 建议结构：

```json
{
  "code": 0,
  "msg": "",
  "data": {
    "allowed": true,
    "overlapHours": 0.5,
    "message": "与已存在的激活时间窗口重合约 0.5 小时，是否仍要保存？",
    "overlapThresholdHours": 1
  }
}
```

- `allowed`：是否允许保存；当重合时长 > 阈值时为 `false`。
- `overlapHours`：当前请求的时间窗口与已有激活窗口的最大重合时长（小时）。
- `message`：前端用于提示/警告的文案。
- `overlapThresholdHours`：当前使用的阈值（小时），前端可用于展示。

### 2.7 重合阈值配置（可选）

- **GET** `/custom/time-window/config` 或系统参数中的 key
- **Response**：`{ overlapThresholdHours: number }`（单位：小时）

若未单独提供，前端可从「重合校验」接口的 `data.overlapThresholdHours` 获取。

## 3. 用户列表接口（复用现有）

- 前端「选定用户」「排除用户」多选使用现有用户列表接口，例如：
  - `GET /system/user/simple-list` 或
  - `GET /system/user/page`（分页）
- 返回至少包含：`id`、`username`（或 nickname）用于展示。

## 4. 统一响应格式

与项目现有约定一致，例如：

- 成功：`{ code: 0 | 200, msg: '', data: ... }`
- 失败：`{ code: 非0/非200, msg: '错误说明', data: null }`

前端已按 `code === 0 || code === 200` 判断成功。
