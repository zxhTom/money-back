package cn.iocoder.yudao.module.custom.service.tenantconfig;

import java.util.Map;

/**
 * 租户配置种子数据初始化 Service
 */
public interface TenantConfigSeedService {

    /**
     * 将源租户的配置复制一份给目标租户
     *
     * @param targetTenantId 目标租户编号
     * @return 每张表复制的行数（跳过的记为 -1）
     */
    Map<String, Integer> seedTenantConfig(Long targetTenantId);

}
