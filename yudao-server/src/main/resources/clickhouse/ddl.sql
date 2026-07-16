-- ClickHouse 增量建表脚本。应用启动时由 ClickHouseMigrationRunner 自动检测并执行（仅当 yudao.clickhouse.enabled=true）。
-- 语句务必幂等（CREATE TABLE IF NOT EXISTS / ALTER TABLE ... ADD COLUMN IF NOT EXISTS），重复启动会自动跳过已存在对象。
-- 注：日志归档表 arc_* 结构统一 (id, log_time, data)，由 ArchiveTableRegistry 自动生成，无需在此声明；本文件只放 contract_recycle 及后续增量。

CREATE TABLE IF NOT EXISTS contract_recycle (
    id UInt64,
    indebted_name Nullable(String),
    indebted_id Nullable(String),
    creditor_name Nullable(String),
    creditor_id Nullable(String),
    description Nullable(String),
    status Nullable(Int32),
    start_date Nullable(DateTime),
    end_date Nullable(DateTime),
    return_type Nullable(String),
    reason_type Nullable(String),
    detail_reason Nullable(String),
    salary Nullable(Int64),
    tariff Nullable(Int64),
    file Nullable(String),
    interest Nullable(Float64),
    refund Nullable(Float64),
    dept_id Nullable(Int64),
    create_time Nullable(DateTime),
    update_time Nullable(DateTime),
    creator Nullable(String),
    updater Nullable(String),
    deleted Nullable(UInt8),
    archive_time DateTime
) ENGINE = ReplacingMergeTree(archive_time)
ORDER BY id;
