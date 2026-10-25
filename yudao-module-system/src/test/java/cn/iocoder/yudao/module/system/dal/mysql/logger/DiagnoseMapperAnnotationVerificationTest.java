package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpUserRefDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.LoginUserStatsDO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.UserIpStatDO;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DiagnoseMapperAnnotationVerificationTest {

    @Test
    public void testMyBatisAnnotationParsingAndStatementRegistration() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperAnnotationBuilder loginParser = new MapperAnnotationBuilder(configuration, LoginLogMapper.class);
        loginParser.parse();

        // 验证 LoginLogMapper 的诊断 MappedStatement 被 MyBatis 真正解析并注册
        String loginIpRefId = LoginLogMapper.class.getName() + ".selectDiagnoseIpUserRefs";
        assertTrue(configuration.hasStatement(loginIpRefId), "selectDiagnoseIpUserRefs 必须成功注册进 MyBatis MappedStatement");
        MappedStatement msIpRef = configuration.getMappedStatement(loginIpRefId);
        assertNotNull(msIpRef);
        assertEquals(IpUserRefDO.class, msIpRef.getResultMaps().get(0).getType());

        String statsId = LoginLogMapper.class.getName() + ".selectDiagnoseUserLoginStats";
        assertTrue(configuration.hasStatement(statsId));
        MappedStatement msStats = configuration.getMappedStatement(statsId);
        assertEquals(LoginUserStatsDO.class, msStats.getResultMaps().get(0).getType());

        String countId = LoginLogMapper.class.getName() + ".selectDiagnoseUserLoginCountLastHour";
        assertTrue(configuration.hasStatement(countId));

        String lastIpId = LoginLogMapper.class.getName() + ".selectDiagnoseLastLoginIp";
        assertTrue(configuration.hasStatement(lastIpId));

        String distinctId = LoginLogMapper.class.getName() + ".selectDiagnoseDistinctIpsByUserId";
        assertTrue(configuration.hasStatement(distinctId));

        String originalDistinctId = LoginLogMapper.class.getName() + ".selectDistinctIpsByUserId";
        assertTrue(configuration.hasStatement(originalDistinctId));

        // 验证 OperateLogMapper 的诊断 MappedStatement
        MapperAnnotationBuilder opParser = new MapperAnnotationBuilder(configuration, OperateLogMapper.class);
        opParser.parse();

        String opIpRefId = OperateLogMapper.class.getName() + ".selectDiagnoseIpUserRefs";
        assertTrue(configuration.hasStatement(opIpRefId));
        MappedStatement msOpRef = configuration.getMappedStatement(opIpRefId);
        assertEquals(IpUserRefDO.class, msOpRef.getResultMaps().get(0).getType());
    }

    @Test
    public void testLoginLogMapperAnnotations() throws Exception {
        // 1. 验证 selectDistinctIpsByUserId 恢复拥有 @Select 注解且仅有一个
        Method distinctMethod = LoginLogMapper.class.getMethod("selectDistinctIpsByUserId", Long.class);
        Select distinctSelect = distinctMethod.getAnnotation(Select.class);
        assertNotNull(distinctSelect, "selectDistinctIpsByUserId 必须有 @Select 注解");
        assertEquals(1, distinctMethod.getAnnotationsByType(Select.class).length);
        assertTrue(distinctSelect.value()[0].contains("SELECT DISTINCT user_ip FROM system_login_log"));

        // 2. 验证 selectDiagnoseIpUserRefs 仅有唯一正确的 @Select 注解，且包含 INNER JOIN system_users 且不包含 l.tenant_id
        Method diagnoseIpMethod = LoginLogMapper.class.getMethod("selectDiagnoseIpUserRefs", Long.class, int.class);
        Select diagnoseIpSelect = diagnoseIpMethod.getAnnotation(Select.class);
        assertNotNull(diagnoseIpSelect, "selectDiagnoseIpUserRefs 必须有 @Select 注解");
        assertEquals(1, diagnoseIpMethod.getAnnotationsByType(Select.class).length, "selectDiagnoseIpUserRefs 只能有且仅有一个 @Select 注解");
        String sql = diagnoseIpSelect.value()[0];
        assertTrue(sql.contains("INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId}"));
        assertTrue(sql.contains("l.user_type = 2"));
        assertFalse(sql.contains("l.tenant_id = #{tenantId}"), "不应对 BaseDO 日志表强求 l.tenant_id");

        // 3. 验证 selectDiagnoseUserLoginStats 仅有唯一正确 @Select 注解
        Method statsMethod = LoginLogMapper.class.getMethod("selectDiagnoseUserLoginStats", Long.class);
        Select statsSelect = statsMethod.getAnnotation(Select.class);
        assertNotNull(statsSelect);
        assertEquals(1, statsMethod.getAnnotationsByType(Select.class).length);
        String statsSql = statsSelect.value()[0];
        assertTrue(statsSql.contains("INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId}"));
        assertTrue(statsSql.contains("l.user_type = 2"));
        assertFalse(statsSql.contains("l.tenant_id = #{tenantId}"));

        // 4. 验证 selectDiagnoseUserLoginCountLastHour 仅有唯一正确 @Select 注解
        Method hourMethod = LoginLogMapper.class.getMethod("selectDiagnoseUserLoginCountLastHour", Long.class);
        Select hourSelect = hourMethod.getAnnotation(Select.class);
        assertNotNull(hourSelect);
        assertEquals(1, hourMethod.getAnnotationsByType(Select.class).length);
        String hourSql = hourSelect.value()[0];
        assertTrue(hourSql.contains("INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId}"));
        assertTrue(hourSql.contains("l.user_type = 2"));
        assertFalse(hourSql.contains("l.tenant_id = #{tenantId}"));

        // 5. 验证 selectDiagnoseLastLoginIp & selectDiagnoseDistinctIpsByUserId
        Method lastIpMethod = LoginLogMapper.class.getMethod("selectDiagnoseLastLoginIp", Long.class, Long.class);
        Select lastIpSelect = lastIpMethod.getAnnotation(Select.class);
        assertNotNull(lastIpSelect);
        assertTrue(lastIpSelect.value()[0].contains("INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId}"));
        assertFalse(lastIpSelect.value()[0].contains("l.tenant_id = #{tenantId}"));

        Method distinctByTenantMethod = LoginLogMapper.class.getMethod("selectDiagnoseDistinctIpsByUserId", Long.class, Long.class);
        Select distinctByTenantSelect = distinctByTenantMethod.getAnnotation(Select.class);
        assertNotNull(distinctByTenantSelect);
        assertTrue(distinctByTenantSelect.value()[0].contains("INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId}"));
        assertFalse(distinctByTenantSelect.value()[0].contains("l.tenant_id = #{tenantId}"));
    }

    @Test
    public void testOperateLogMapperAnnotations() throws Exception {
        Method opMethod = OperateLogMapper.class.getMethod("selectDiagnoseIpUserRefs", Long.class, int.class);
        Select opSelect = opMethod.getAnnotation(Select.class);
        assertNotNull(opSelect);
        assertEquals(1, opMethod.getAnnotationsByType(Select.class).length);
        String sql = opSelect.value()[0];
        assertTrue(sql.contains("INNER JOIN system_users u ON u.id = l.user_id AND u.deleted = 0 AND u.tenant_id = #{tenantId}"));
        assertTrue(sql.contains("l.user_type = 2"));
        assertFalse(sql.contains("l.tenant_id = #{tenantId}"));
    }
}
