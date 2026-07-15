package cn.iocoder.yudao.module.custom.framework.clickhouse.config;

import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@Slf4j
public class ClickHouseDataSourceConfig {

    @Bean(name = "clickHouseDataSource")
    @ConditionalOnProperty(prefix = "yudao.clickhouse", name = "enabled", havingValue = "true")
    public DataSource clickHouseDataSource(ClickHouseProperties props) throws Exception {
        Properties p = new Properties();
        if (props.getUsername() != null) {
            p.setProperty("user", props.getUsername());
        }
        if (props.getPassword() != null) {
            p.setProperty("password", props.getPassword());
        }
        log.info("[ClickHouse] 数据源已启用 url={}", props.getUrl());
        return new ClickHouseDataSource(props.getUrl(), p);
    }
}
