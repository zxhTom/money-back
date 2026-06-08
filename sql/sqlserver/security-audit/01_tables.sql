-- ============================================================
-- 审计日志 + 安全监控 建表 SQL（SQL Server）
-- ============================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'custom_audit_log')
CREATE TABLE custom_audit_log (
    id             BIGINT        IDENTITY(1,1) PRIMARY KEY,
    trace_id       NVARCHAR(64),
    user_id        BIGINT,
    user_type      TINYINT,
    username       NVARCHAR(64),
    module         NVARCHAR(64),
    operation_type NVARCHAR(20)  NOT NULL,
    entity_type    NVARCHAR(64),
    entity_id      BIGINT,
    operation      NVARCHAR(500),
    before_data    NVARCHAR(MAX),
    after_data     NVARCHAR(MAX),
    request_url    NVARCHAR(255),
    request_method NVARCHAR(10),
    external_ip    NVARCHAR(50),
    direct_ip      NVARCHAR(50),
    all_ip_headers NVARCHAR(1000),
    user_agent     NVARCHAR(500),
    status         TINYINT       NOT NULL DEFAULT 0,
    error_message  NVARCHAR(500),
    cost_time      INT,
    create_time    DATETIME2     NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX idx_audit_user_id        ON custom_audit_log (user_id);
CREATE INDEX idx_audit_entity         ON custom_audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_operation_type ON custom_audit_log (operation_type);
CREATE INDEX idx_audit_create_time    ON custom_audit_log (create_time);
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'custom_security_alert')
CREATE TABLE custom_security_alert (
    id                  BIGINT        IDENTITY(1,1) PRIMARY KEY,
    alert_type          NVARCHAR(50)  NOT NULL,
    severity            TINYINT       NOT NULL,
    source_ip           NVARCHAR(50),
    user_id             BIGINT,
    request_url         NVARCHAR(255),
    request_method      NVARCHAR(10),
    suspicious_content  NVARCHAR(MAX),
    alert_message       NVARCHAR(1000) NOT NULL,
    detail              NVARCHAR(MAX),
    handled             TINYINT       NOT NULL DEFAULT 0,
    handle_by           BIGINT,
    handle_time         DATETIME2,
    handle_remark       NVARCHAR(500),
    create_time         DATETIME2     NOT NULL DEFAULT GETDATE()
);
GO

CREATE INDEX idx_alert_type        ON custom_security_alert (alert_type);
CREATE INDEX idx_alert_severity    ON custom_security_alert (severity);
CREATE INDEX idx_alert_source_ip   ON custom_security_alert (source_ip);
CREATE INDEX idx_alert_handled     ON custom_security_alert (handled);
CREATE INDEX idx_alert_create_time ON custom_security_alert (create_time);
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'custom_ip_blacklist')
CREATE TABLE custom_ip_blacklist (
    id          BIGINT       IDENTITY(1,1) PRIMARY KEY,
    ip          NVARCHAR(50) NOT NULL,
    reason      NVARCHAR(200),
    auto_added  BIT          NOT NULL DEFAULT 0,
    expire_time DATETIME2,
    create_time DATETIME2    NOT NULL DEFAULT GETDATE(),
    CONSTRAINT uk_ip UNIQUE (ip)
);
GO
