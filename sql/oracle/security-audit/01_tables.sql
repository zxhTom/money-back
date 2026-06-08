-- ============================================================
-- 审计日志 + 安全监控 建表 SQL（Oracle 12c+）
-- ============================================================

CREATE TABLE custom_audit_log (
    id             NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trace_id       VARCHAR2(64),
    user_id        NUMBER(19),
    user_type      NUMBER(3),
    username       VARCHAR2(64),
    module         VARCHAR2(64),
    operation_type VARCHAR2(20)  NOT NULL,
    entity_type    VARCHAR2(64),
    entity_id      NUMBER(19),
    operation      VARCHAR2(500),
    before_data    CLOB,
    after_data     CLOB,
    request_url    VARCHAR2(255),
    request_method VARCHAR2(10),
    external_ip    VARCHAR2(50),
    direct_ip      VARCHAR2(50),
    all_ip_headers VARCHAR2(1000),
    user_agent     VARCHAR2(500),
    status         NUMBER(3)     DEFAULT 0 NOT NULL,
    error_message  VARCHAR2(500),
    cost_time      NUMBER(10),
    create_time    TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL
);

COMMENT ON TABLE  custom_audit_log                IS '用户行为审计日志';
COMMENT ON COLUMN custom_audit_log.operation_type IS '操作类型(CREATE/READ/UPDATE/DELETE/EXPORT/LOGIN)';
COMMENT ON COLUMN custom_audit_log.user_type      IS '用户类型(1=管理员,2=小程序用户)';
COMMENT ON COLUMN custom_audit_log.status         IS '操作结果(0=成功,1=失败)';

CREATE INDEX idx_audit_user_id        ON custom_audit_log (user_id);
CREATE INDEX idx_audit_entity         ON custom_audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_operation_type ON custom_audit_log (operation_type);
CREATE INDEX idx_audit_create_time    ON custom_audit_log (create_time);

CREATE TABLE custom_security_alert (
    id                  NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alert_type          VARCHAR2(50)  NOT NULL,
    severity            NUMBER(3)     NOT NULL,
    source_ip           VARCHAR2(50),
    user_id             NUMBER(19),
    request_url         VARCHAR2(255),
    request_method      VARCHAR2(10),
    suspicious_content  CLOB,
    alert_message       VARCHAR2(1000) NOT NULL,
    detail              CLOB,
    handled             NUMBER(3)     DEFAULT 0 NOT NULL,
    handle_by           NUMBER(19),
    handle_time         TIMESTAMP,
    handle_remark       VARCHAR2(500),
    create_time         TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL
);

COMMENT ON TABLE  custom_security_alert          IS '安全告警记录';
COMMENT ON COLUMN custom_security_alert.severity IS '严重级别(1=INFO,2=WARNING,3=CRITICAL)';
COMMENT ON COLUMN custom_security_alert.handled  IS '处理状态(0=未处理,1=已处理,2=误报)';

CREATE INDEX idx_alert_type        ON custom_security_alert (alert_type);
CREATE INDEX idx_alert_severity    ON custom_security_alert (severity);
CREATE INDEX idx_alert_source_ip   ON custom_security_alert (source_ip);
CREATE INDEX idx_alert_handled     ON custom_security_alert (handled);
CREATE INDEX idx_alert_create_time ON custom_security_alert (create_time);

CREATE TABLE custom_ip_blacklist (
    id          NUMBER(19)   GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ip          VARCHAR2(50) NOT NULL,
    reason      VARCHAR2(200),
    auto_added  NUMBER(1)    DEFAULT 0 NOT NULL,
    expire_time TIMESTAMP,
    create_time TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT uk_ip UNIQUE (ip)
);

COMMENT ON TABLE  custom_ip_blacklist            IS 'IP黑名单';
COMMENT ON COLUMN custom_ip_blacklist.auto_added IS '是否自动封禁(0=否,1=是)';
COMMENT ON COLUMN custom_ip_blacklist.expire_time IS '过期时间(NULL=永久)';
