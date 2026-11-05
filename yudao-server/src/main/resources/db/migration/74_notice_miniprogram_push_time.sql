-- 手动推送后小程序才能看到公告；旧公告不回填，避免历史公告首次上线时集中弹出。
ALTER TABLE `system_notice`
    ADD COLUMN `push_time` datetime(3) NULL COMMENT '最近一次手动推送时间';
