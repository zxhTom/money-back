# 时间窗口管理功能 - 后端对接说明

## 1. 功能概述

本功能用于管理「时间窗口」记录，每条记录包含：

- **开始时间**、**结束时间**：时间区间
- **快捷选择**：1 / 3 / 7 表示以当前时间（或用户选择的开始时间）为起点，结束时间分别为 +1 小时、+3 小时、+7 小时（仅前端展示与填充，可选存储用于回显）
- **选定用户集合**：从用户列表中多选
- **排除用户集合**：从用户列表中多选；与选定用户重合时**以排除为准**（即同时出现在两边的用户视为被排除）
- **激活状态**：默认新增为激活；用于与「时间窗口重合校验」配合（仅校验激活状态的记录）

### 1.1 业务规则摘要

- **用户集合并集规则**：有效用户 = 选定用户 \ 排除用户（排除优先）。
- **重合校验**：新增/修改时，当前时间窗口与**所有已存在的、激活状态的**时间窗口做重合判断：
  - 重合时长 **> 配置阈值（默认 1 小时）**：不允许保存，前端提示错误。
  - 重合时长 **> 0 且 ≤ 阈值**：允许保存，但需**警告**提示，用户确认后继续。
  - 无重合：直接允许保存。
- **阈值可配置**：默认 1 小时，需支持后端配置（建议系统参数表 key 如 `time_window.overlap_threshold_hours`，或单独配置接口），前端通过「重合校验」接口响应或单独配置接口获取。

详细业务规则见：[business-rules.md](./business-rules.md)。

---

## 2. 后端需要提供的接口

以下为前端已按此约定实现的接口，后端需按此实现以保证联调通过。

### 2.1 列表分页

- **接口**：`GET /custom/time-window/page` 或项目约定的前缀（如 `/system/time-window/page`）
- **请求参数**：标准分页 `pageNo`、`pageSize`，以及可选筛选：`status`（激活状态）、时间范围等（见 [api.md](./api.md)）
- **响应**：`{ code, msg, data: { list: [], total } }`，单条结构见 [api.md](./api.md) 的 TimeWindowVO

### 2.2 详情

- **接口**：`GET /custom/time-window/get?id={id}`
- **响应**：单条 TimeWindowVO（含 `selectedUserIds`、`excludedUserIds` 数组）

### 2.3 新增

- **接口**：`POST /custom/time-window/create`
- **请求体**：TimeWindowCreateReqVO（开始/结束时间、选定用户 ID 列表、排除用户 ID 列表、激活状态等）
- **响应**：统一成功响应；**建议**在服务端同样做「时间窗口重合校验」，与前端双重保障

### 2.4 修改

- **接口**：`PUT /custom/time-window/update`
- **请求体**：TimeWindowUpdateReqVO（含 id、其余同创建）
- **响应**：统一成功响应；修改时校验重合需**排除当前记录自身**

### 2.5 删除

- **接口**：`DELETE /custom/time-window/delete?id={id}`
- **响应**：统一成功响应

### 2.6 重合校验（前端提交前必调）

- **接口**：`POST /custom/time-window/check-overlap`
- **请求体**：`{ startTime, endTime, excludeId? }`  
  - `excludeId`：修改时传当前记录 id，新增不传
- **响应**：  
  - `{ allowed: boolean, overlapHours?: number, message?: string, overlapThresholdHours?: number }`  
  - `allowed === false` 且 `overlapHours > overlapThresholdHours`：禁止保存，前端展示 `message`  
  - `allowed === true` 且 `overlapHours > 0`：允许但需警告，前端弹窗确认  
  - `overlapThresholdHours`：当前使用的阈值（小时），前端用于展示「允许的最大重合时长」

说明：若后端暂未实现该接口，前端可仅做客户端校验或暂时跳过服务端校验，由后端在 create/update 时强制校验并返回明确错误码与错误信息。

### 2.7 重合阈值配置（可选但建议）

- **方式 A**：在「重合校验」接口的响应中返回 `overlapThresholdHours`，前端每次校验时拿到当前阈值。
- **方式 B**：单独接口如 `GET /custom/time-window/config` 或系统参数接口，返回 `{ overlapThresholdHours: number }`（单位：小时）。

前端已预留从「校验接口响应」或「单独配置接口」读取阈值的逻辑，见 [api.md](./api.md)。

---

## 3. 建表与菜单

- **建表 SQL**：[feature-time-window/sql/create_table.sql](./sql/create_table.sql)  
  - 表名可按项目规范调整（如加前缀、分库）
  - 用户集合存储：当前为 `selected_user_ids`、`excluded_user_ids`（JSON 数组或逗号分隔），以与前端传的 ID 数组一致

- **菜单与权限**：见 [feature-time-window/sql/insert_menu_time_window.sql](./sql/insert_menu_time_window.sql)  
  - 菜单挂在「综合查询」下：综合查询 → 时间窗口（目录）→ 时间窗口列表（页面）。path 为 `time-window` / `list`，若综合查询 path 为 `custom` 则完整路由为 `/custom/time-window/list`，与前端 `remaining.ts` 一致。

---

## 4. 前端已实现能力

- 列表页：分页、筛选（如状态）、新增/编辑/删除。
- 表单：开始/结束时间、快捷选择 1h/3h/7h（以当前时间或已选开始时间为基准）、选定用户多选、排除用户多选、激活状态。
- 提交前调用「重合校验」接口；若禁止则提示并不提交；若允许但存在重合则警告并需用户确认后提交。
- 有效用户展示：列表/详情中「有效用户」= 选定用户 − 排除用户（仅展示用，持久化仍为两列表）。

---

## 5. 文档索引

| 文档 | 说明 |
|------|------|
| [README.md](./README.md) | 本文件，后端对接总览 |
| [api.md](./api.md) | 接口约定（请求/响应字段、枚举） |
| [business-rules.md](./business-rules.md) | 业务规则（用户集合、重合判断、阈值） |
| [sql/create_table.sql](./sql/create_table.sql) | 建表 SQL |
| [sql/insert_menu_time_window.sql](./sql/insert_menu_time_window.sql) | 菜单与权限 INSERT SQL |
