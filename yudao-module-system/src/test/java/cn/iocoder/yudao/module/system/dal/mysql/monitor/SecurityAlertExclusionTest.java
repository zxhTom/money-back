package cn.iocoder.yudao.module.system.dal.mysql.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.monitor.vo.SecurityAlertPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.SecurityAlertDO;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SecurityAlertExclusionTest {
    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void exclusionsAreAppliedBeforePagingAndKeepUnknownSource() throws Exception {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), SecurityAlertDO.class);
        SecurityAlertMapper mapper = mock(SecurityAlertMapper.class, CALLS_REAL_METHODS);
        ArgumentCaptor<Wrapper> capture = ArgumentCaptor.forClass(Wrapper.class);
        doReturn(new PageResult<>(Collections.emptyList(), 0L)).when(mapper).selectPage(any(PageParam.class), capture.capture());
        SecurityAlertPageReqVO req = new SecurityAlertPageReqVO();
        req.setExcludedIps(Arrays.asList("192.0.2.1", "192.0.2.2"));
        req.setHandled(0);
        mapper.selectPage(req);
        Wrapper wrapper = capture.getValue();
        String sql = wrapper.getSqlSegment();
        Map<String, Object> values = ((com.baomidou.mybatisplus.core.conditions.AbstractWrapper) wrapper).getParamNameValuePairs();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String literal = entry.getValue() instanceof Number ? entry.getValue().toString() : "'" + entry.getValue() + "'";
            sql = sql.replace("#{ew.paramNameValuePairs." + entry.getKey() + "}", literal);
        }
        try (Connection c = DriverManager.getConnection("jdbc:h2:mem:alert_exclusions;MODE=MYSQL", "sa", ""); Statement st = c.createStatement()) {
            st.execute("create table alerts(id bigint, source_ip varchar(45), handled int)");
            st.execute("insert into alerts values(1,'192.0.2.1',0),(2,'192.0.2.2',0),(3,'192.0.2.3',0),(4,null,0),(5,null,1)");
            try (ResultSet rows = st.executeQuery("select id from alerts where " + sql)) {
                List<Long> ids = new ArrayList<>();
                while (rows.next()) ids.add(rows.getLong(1));
                assertEquals(Arrays.asList(4L, 3L), ids);
            }
        }
        req.setExcludedIps(Collections.emptyList());
        mapper.selectPage(req);
        assertFalse(capture.getValue().getSqlSegment().contains("NOT IN"));
    }
}
