package cn.iocoder.yudao.module.custom.migration;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.idcard.IdCardCipherUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.framework.idcard.IdCardCipherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 一次性将历史明文身份证刷为密文。执行后请立即关闭 yudao.id-card.migration.enabled=true。
 * <p>
 * <b>注意：</b>若当前版本约定库中存<strong>明文</strong>身份证，请勿启用本任务，否则会与业务冲突。
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "yudao.id-card.migration", name = "enabled", havingValue = "true")
public class IdNoCipherMigrationRunner implements ApplicationRunner {

    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private IdCardCipherService idCardCipherService;

    @Override
    public void run(ApplicationArguments args) {
        log.warn("[IdNoCipherMigration][START] 正在将 system_users.id_no / custom_contract 的明文身份证刷为密文，请勿重复执行");
        String secret = idCardCipherService.getSecret();
        int u = 0;
        List<AdminUserDO> users = adminUserMapper.selectList(new LambdaQueryWrapperX<AdminUserDO>()
                .isNotNull(AdminUserDO::getIdNo));
        for (AdminUserDO user : users) {
            if (StrUtil.isBlank(user.getIdNo())) {
                continue;
            }
            if (IdCardCipherUtil.looksLikePlainIdCard(user.getIdNo())) {
                String c = IdCardCipherUtil.encrypt(user.getIdNo().trim(), secret);
                adminUserMapper.updateById(new AdminUserDO().setId(user.getId()).setIdNo(c));
                u++;
            }
        }
        int c = 0;
        List<ContractDO> contracts = contractMapper.selectList();
        for (ContractDO contract : contracts) {
            boolean changed = false;
            ContractDO upd = new ContractDO().setId(contract.getId());
            if (StrUtil.isNotBlank(contract.getIndebtedId()) && IdCardCipherUtil.looksLikePlainIdCard(contract.getIndebtedId())) {
                upd.setIndebtedId(IdCardCipherUtil.encrypt(contract.getIndebtedId().trim(), secret));
                changed = true;
            }
            if (StrUtil.isNotBlank(contract.getCreditorId()) && IdCardCipherUtil.looksLikePlainIdCard(contract.getCreditorId())) {
                upd.setCreditorId(IdCardCipherUtil.encrypt(contract.getCreditorId().trim(), secret));
                changed = true;
            }
            if (changed) {
                contractMapper.updateById(upd);
                c++;
            }
        }
        log.warn("[IdNoCipherMigration][DONE] 更新 users={} contracts={}，请将 yudao.id-card.migration.enabled 改回 false", u, c);
    }
}
