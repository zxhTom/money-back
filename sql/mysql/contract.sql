CREATE TABLE contract_pay_relation (
    id BIGINT PRIMARY KEY auto_increment COMMENT '主键',
  	contract_id BIGINT  COMMENT '合同ID',
  	pay_order_id BIGINT  COMMENT '支付ID',
    UNIQUE KEY uk_con_pay (contract_id, pay_order_id)
) COMMENT='合同支付关联信息';