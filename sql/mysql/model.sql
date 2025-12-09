CREATE TABLE contract_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model VARCHAR(100) NOT NULL COMMENT '模式'
);
insert into contract_model (model) values ('safe');