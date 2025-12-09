CREATE TABLE fee_strategy (
    id BIGINT PRIMARY KEY auto_increment COMMENT '主键',
    min_amount DECIMAL(15,2) NOT NULL COMMENT '最小金额',
    max_amount DECIMAL(15,2) NOT NULL COMMENT '最大金额',
    fee DECIMAL(10,2) NOT NULL COMMENT '收费金额',
    strategy_order INT NOT NULL COMMENT '策略顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0禁用',
    UNIQUE KEY uk_min_max (min_amount, max_amount)
) COMMENT='费用策略';

-- 初始化数据
INSERT INTO fee_strategy (min_amount, max_amount, fee, strategy_order) VALUES
(0, 5000, 0.01, 1),
(5000, 10000, 9.8, 2),
(10000, 20000, 19.8, 3),
(20000, 99999999, 29.8, 4);