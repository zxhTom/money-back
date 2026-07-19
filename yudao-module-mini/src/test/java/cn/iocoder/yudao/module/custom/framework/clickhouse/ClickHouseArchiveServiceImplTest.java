package cn.iocoder.yudao.module.custom.framework.clickhouse;

import cn.iocoder.yudao.module.custom.framework.clickhouse.config.ClickHouseProperties;
import cn.iocoder.yudao.module.custom.framework.clickhouse.core.ClickHouseArchiveServiceImpl;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

class ClickHouseArchiveServiceImplTest {
    @Test
    void disabled_query_returnsEmpty_and_insert_throws() {
        // enabled=false 默认；不调用 init()，dataSource 保持 null 表示未启用
        ClickHouseArchiveServiceImpl svc = new ClickHouseArchiveServiceImpl(new ClickHouseProperties());
        assertFalse(svc.isEnabled());
        assertTrue(svc.query("SELECT 1").isEmpty());
        assertThrows(IllegalStateException.class,
                () -> svc.insertRows("t", Collections.singletonList("c"), Collections.emptyList()));
    }
}
