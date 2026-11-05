-- 可选的定向公告接收人。为空时保持原有租户内群发行为。
ALTER TABLE `system_notice`
    ADD COLUMN `target_user_id` bigint NULL COMMENT '指定接收用户 ID；NULL 为租户群发';
