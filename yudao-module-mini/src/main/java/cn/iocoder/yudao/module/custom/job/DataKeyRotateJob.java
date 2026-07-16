package cn.iocoder.yudao.module.custom.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.custom.framework.dataencrypt.core.DataKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class DataKeyRotateJob implements JobHandler {

    @Resource
    private DataKeyService dataKeyService;

    @Override
    @TenantIgnore
    public String execute(String param) {
        long version = dataKeyService.rotate();
        log.info("[execute][定时轮换数据密钥，新版本 ({})]", version);
        return String.format("数据密钥已轮换至版本 %s", version);
    }

}
