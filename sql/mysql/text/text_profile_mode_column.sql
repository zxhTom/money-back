-- text_mode: 文案套类型（safe=安全模式基准 / offcial=正式模式基准），创建时固定，创建后不可变；
-- "生效"从此前的全表唯一改为按 text_mode 分别互斥：可以同时存在一条生效的 safe 套 + 一条生效的 offcial 套
ALTER TABLE `custom_text_profile`
    ADD COLUMN `text_mode` varchar(16) NOT NULL DEFAULT 'safe'
    COMMENT '文案套类型：safe=安全模式基准 offcial=正式模式基准，创建后不可变' AFTER `seed_from`;
-- 回填已有的 default-offcial 种子行（default-safe 靠列默认值 'safe' 自动生效，无需 UPDATE）
UPDATE `custom_text_profile` SET `text_mode` = 'offcial' WHERE `code` = 'default-offcial';
