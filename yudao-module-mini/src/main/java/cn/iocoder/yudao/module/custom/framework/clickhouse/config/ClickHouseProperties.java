package cn.iocoder.yudao.module.custom.framework.clickhouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yudao.clickhouse")
@Data
public class ClickHouseProperties {
    private boolean enabled = false;
    private String url;        // jdbc:clickhouse://host:8123/db
    private String username;
    private String password;
    private String database = "default";
}
