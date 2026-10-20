package cn.iocoder.yudao.module.custom.controller.admin.wechat;

import org.junit.jupiter.api.Test;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.junit.jupiter.api.Assertions.*;

class MiniCallbackTemplateTest {
    @Test
    void miniProgramReceivesTheSameTrustedStatusAsThePage() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        for (String status : new String[]{"failed", "pending", "success"}) {
            Context context = new Context();
            context.setVariable("status", status);
            context.setVariable("idNoCipher", "test-cipher");
            context.setVariable("idNoDisplay", "****");
            String html = engine.process("mini-callback", context);
            assertTrue(html.contains("const status = \"" + status + "\";"), status);
            assertFalse(html.contains("/*" + status + "*/"));
            assertTrue(html.contains("const idNoCipher = \"test-cipher\";"));
            assertTrue(html.contains("const verifyToken = null;"));
        }
    }
}
