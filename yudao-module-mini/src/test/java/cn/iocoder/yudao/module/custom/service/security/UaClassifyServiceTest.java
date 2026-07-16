package cn.iocoder.yudao.module.custom.service.security;

import cn.iocoder.yudao.module.custom.dal.dataobject.security.UaWhitelistDO;
import cn.iocoder.yudao.module.custom.dal.mysql.security.UaWhitelistMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class UaClassifyServiceTest {

    @Mock
    private UaWhitelistMapper uaWhitelistMapper;

    @InjectMocks
    private UaClassifyService service;

    private UaWhitelistDO kw(String k) {
        UaWhitelistDO d = new UaWhitelistDO();
        d.setKeyword(k);
        d.setEnabled(1);
        return d;
    }

    @Test
    public void testHardBot() {
        assertTrue(service.isHardBot("python-requests/2.31.0"));
        assertTrue(service.isHardBot("Python-urllib/3.13"));
        assertTrue(service.isHardBot("curl/8.5.0"));
        assertTrue(service.isHardBot("python-httpx/0.28.1"));
        assertTrue(service.isHardBot("Apifox/1.0.0"));
        assertTrue(service.isHardBot("${jndi:ldap://evil}"));
        assertTrue(service.isHardBot(""));      // 空 UA 视为脚本
        assertTrue(service.isHardBot(null));
        // 真实浏览器不是硬 bot
        assertFalse(service.isHardBot("Mozilla/5.0 (iPhone) MicroMessenger/8.0"));
        assertFalse(service.isHardBot("Mozilla/5.0 (Windows NT 10.0) Chrome/120"));
    }

    @Test
    public void testWhitelistedBrowser() {
        lenient().when(uaWhitelistMapper.selectEnabled())
                .thenReturn(Arrays.asList(kw("Mozilla"), kw("MicroMessenger"), kw("Chrome")));
        assertTrue(service.isWhitelistedBrowser("Mozilla/5.0 (iPhone) MicroMessenger/8.0.49"));
        assertTrue(service.isWhitelistedBrowser("Mozilla/5.0 Chrome/120"));
        assertFalse(service.isWhitelistedBrowser("python-requests/2.31.0"));
        assertFalse(service.isWhitelistedBrowser(""));
    }
}
