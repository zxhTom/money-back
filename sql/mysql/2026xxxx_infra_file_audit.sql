-- 图片内容安全审核字段（微信 mediaCheckAsync）
-- audit_status: 0=审核中, 1=通过/无需审核, 2=违规(已删除)
ALTER TABLE infra_file
    ADD COLUMN audit_status TINYINT NOT NULL DEFAULT 1 COMMENT '内容审核状态：0审核中 1通过 2违规',
    ADD COLUMN audit_trace_id VARCHAR(128) NULL COMMENT '微信内容检测 trace_id';
CREATE INDEX idx_infra_file_trace ON infra_file (audit_trace_id);
