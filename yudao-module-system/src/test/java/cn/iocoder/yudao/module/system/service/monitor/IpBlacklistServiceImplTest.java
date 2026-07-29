package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpBlacklistDO;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpBlacklistLogMapper;
import cn.iocoder.yudao.module.system.dal.mysql.monitor.IpBlacklistMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IpBlacklistServiceImplTest {

    @Mock
    private IpBlacklistMapper ipBlacklistMapper;
    @Mock
    private IpBlacklistLogMapper ipBlacklistLogMapper;
    @Mock
    private IpWhitelistService ipWhitelistService;

    @InjectMocks
    private IpBlacklistServiceImpl service;

    @Test
    public void testGetActiveEntry_found_returnsEntry() {
        IpBlacklistDO entry = new IpBlacklistDO();
        entry.setIp("1.2.3.4");
        entry.setReason("自动封禁：暴力破解检测");
        when(ipBlacklistMapper.selectOne(any())).thenReturn(entry);

        assertSame(entry, service.getActiveEntry("1.2.3.4"));
    }

    @Test
    public void testGetActiveEntry_notFound_returnsNull() {
        when(ipBlacklistMapper.selectOne(any())).thenReturn(null);

        assertNull(service.getActiveEntry("5.6.7.8"));
    }
}
