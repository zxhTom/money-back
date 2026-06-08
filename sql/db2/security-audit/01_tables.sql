-- ============================================================
-- 审计日志 + 安全监控 建表 SQL（DB2）
-- ============================================================

CREATE TABLE custom_audit_log (
    id             BIGINT         GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) NOT NULL PRIMARY KEY,
    trace_id       VARCHAR(64),
    user_id        BIGINT,
    user_type      SMALLINT,
    username       VARCHAR(64),
    module         VARCHAR(64),
    operation_type VARCHAR(20)    NOT NULL,
    entity_type    VARCHAR(64),
    entity_id      BIGINT,
    operation      VARCHAR(500),
    before_data    CLOB(16M),
    after_data     CLOB(16M),
    request_url    VARCHAR(255),
    request_method VARCHAR(10),
    external_ip    VARCHAR(50),
    direct_ip      VARCHAR(50),
    all_ip_headers VARCHAR(1000),
    user_agent     VARCHAR(500),
    status         SMALLINT       NOT NULL DEFAULT 0,
    error_message  VARCHAR(500),
    cost_time      INTEGER,
    create_time    TIMESTAMP      NOT NULL DEFAULT CURRENT TIMESTAMP
);

CREATE INDEX idx_audit_user_id        ON custom_audit_log (user_id);
CREATE INDEX idx_audit_entity         ON custom_audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_operation_type ON custom_audit_log (operation_type);
CREATE INDEX idx_audit_create_time    ON custom_audit_log (create_time);

COMMENT ON TABLE  custom_audit_log                IS '用户行为审计日志';
COMMENT ON COLUMN custom_audit_log.operation_type IS '操作类型(CREATE/READ/UPDATE/DELETE/EXPORT/LOGIN)';
COMMENT ON COLUMN custom_audit_log.status         IS '操作结果(0=成功,1=失败)';

CREATE TABLE custom_security_alert (
    id                  BIGINT         GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) NOT NULL PRIMARY KEY,
    alert_type          VARCHAR(50)    NOT NULL,
    severity            SMALLINT       NOT NULL,
    source_ip           VARCHAR(50),
    user_id             BIGINT,
    request_url         VARCHAR(255),
    request_method      VARCHAR(10),
    suspicious_content  CLOB(4M),
    alert_message       VARCHAR(1000)  NOT NULL,
    detail              CLOB(4M),
    handled             SMALLINT       NOT NULL DEFAULT 0,
    handle_by           BIGINT,
    handle_time         TIMESTAMP,
    handle_remark       VARCHAR(500),
    create_time         TIMESTAMP      NOT NULL DEFAULT CURRENT TIMESTAMP
);

CREATE INDEX idx_alert_type        ON custom_security_alert (alert_type);
CREATE INDEX idx_alert_severity    ON custom_security_alert (severity);
CREATE INDEX idx_alert_source_ip   ON custom_security_alert (source_ip);
CREATE INDEX idx_alert_handled     ON custom_security_alert (handled);
CREATE INDEX idx_alert_create_time ON custom_security_alert (create_time);

COMMENT ON TABLE  custom_security_alert          IS '安全告警记录';
COMMENT ON COLUMN custom_security_alert.severity IS '严重级别(1=INFO,2=WARNING,3=CRITICAL)';
COMMENT ON COLUMN custom_security_alert.handled  IS '处理状态(0=未处理,1=已处理,2=误报)';

CREATE TABLE custom_ip_blacklist (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) NOT NULL PRIMARY KEY,
    ip          VARCHAR(50)  NOT NULL,
    reason      VARCHAR(200),
    auto_added  SMALLINT     NOT NULL DEFAULT 0,
    expire_time TIMESTAMP,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT TIMESTAMP,
    CONSTRAINT uk_ip UNIQUE (ip)
);

COMMENT ON TABLE  custom_ip_blacklist IS 'IP黑名单';
