package cn.iocoder.yudao.framework.tenant.core.db;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TenantDatabaseInterceptorTest {

    @TableName("t_tenant")
    public static class TenantDO extends TenantBaseDO {
    }

    @TableName("t_plain")
    public static class PlainDO extends BaseDO {
    }

    @TableName("t_ignore")
    @TenantIgnore
    public static class IgnoreDO extends BaseDO {
    }

    @TableName("t_tenant_ignore")
    @TenantIgnore
    public static class TenantIgnoreDO extends TenantBaseDO {
    }

    private TenantDatabaseInterceptor interceptor;
    private TenantProperties properties;

    @BeforeEach
    public void setUp() {
        TenantContextHolder.clear();
        properties = new TenantProperties();

        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(configuration, "");

        TableInfoHelper.initTableInfo(builderAssistant, TenantDO.class);
        TableInfoHelper.initTableInfo(builderAssistant, PlainDO.class);
        TableInfoHelper.initTableInfo(builderAssistant, IgnoreDO.class);
        TableInfoHelper.initTableInfo(builderAssistant, TenantIgnoreDO.class);

        interceptor = new TenantDatabaseInterceptor(properties);
    }

    @AfterEach
    public void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    public void testCase1_TenantDO_NotIgnored() {
        assertFalse(interceptor.ignoreTable("t_tenant"));
    }

    @Test
    public void testCase2_PlainDO_Ignored() {
        assertTrue(interceptor.ignoreTable("t_plain"));
    }

    @Test
    public void testCase3_IgnoreDO_Ignored() {
        assertTrue(interceptor.ignoreTable("t_ignore"));
    }

    @Test
    public void testCase4_TenantIgnoreDO_Ignored() {
        assertTrue(interceptor.ignoreTable("t_tenant_ignore"));
    }

    @Test
    public void testCase5_NotRegisteredTable_Ignored() {
        assertTrue(interceptor.ignoreTable("not_registered_table"));
    }

    @Test
    public void testCase6_TenantPropertiesIgnoreTables_Ignored() {
        TenantProperties props = new TenantProperties();
        props.setIgnoreTables(new java.util.HashSet<>(java.util.Arrays.asList("t_tenant")));
        TenantDatabaseInterceptor interceptorWithIgnore = new TenantDatabaseInterceptor(props);
        assertTrue(interceptorWithIgnore.ignoreTable("t_tenant"));
    }

    @Test
    public void testCase7_TenantContextHolderIgnore_Ignored() {
        TenantContextHolder.setIgnore(true);
        assertTrue(interceptor.ignoreTable("t_tenant"));
    }

    @Test
    public void testCase8_BackticksAndUpperCase_Ignored() {
        assertTrue(interceptor.ignoreTable("`t_plain`"));
        assertTrue(interceptor.ignoreTable("T_PLAIN"));
    }

    @Test
    public void testCase9_TenantTablesContainsPlain_NotIgnored() {
        TenantProperties props = new TenantProperties();
        props.setTenantTables(new java.util.HashSet<>(java.util.Collections.singletonList("t_plain")));
        TenantDatabaseInterceptor interceptorWithTenantTables = new TenantDatabaseInterceptor(props);
        assertFalse(interceptorWithTenantTables.ignoreTable("t_plain"));
    }

    @Test
    public void testCase10_TenantTablesContainsPlain_UpperCase_NotIgnored() {
        TenantProperties props = new TenantProperties();
        props.setTenantTables(new java.util.HashSet<>(java.util.Collections.singletonList("t_plain")));
        TenantDatabaseInterceptor interceptorWithTenantTables = new TenantDatabaseInterceptor(props);
        assertFalse(interceptorWithTenantTables.ignoreTable("T_PLAIN"));
    }

    @Test
    public void testCase11_TenantTablesContainsIgnore_ExplicitConfigPriority_NotIgnored() {
        TenantProperties props = new TenantProperties();
        props.setTenantTables(new java.util.HashSet<>(java.util.Collections.singletonList("t_ignore")));
        TenantDatabaseInterceptor interceptorWithTenantTables = new TenantDatabaseInterceptor(props);
        assertFalse(interceptorWithTenantTables.ignoreTable("t_ignore"));
    }

    @Test
    public void testCase12_TenantTablesEmpty_Plain_Ignored() {
        TenantProperties props = new TenantProperties();
        TenantDatabaseInterceptor interceptorEmptyTenantTables = new TenantDatabaseInterceptor(props);
        assertTrue(interceptorEmptyTenantTables.ignoreTable("t_plain"));
    }

}
