CREATE TABLE IF NOT EXISTS contract_recycle (
    id UInt64,
    indebted_name String,
    indebted_id String,
    creditor_name String,
    creditor_id String,
    description String,
    status Int32,
    start_date Nullable(DateTime),
    end_date Nullable(DateTime),
    return_type String,
    reason_type String,
    create_time Nullable(DateTime),
    update_time Nullable(DateTime),
    archive_time DateTime
) ENGINE = ReplacingMergeTree(archive_time)
ORDER BY id;
