package cn.iocoder.yudao.module.custom.service.miniconfig;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigRespVO;
import cn.iocoder.yudao.module.custom.controller.admin.miniconfig.vo.MiniProgramConfigSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dal.mysql.miniconfig.MiniProgramConfigMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class MiniProgramConfigServiceImpl implements MiniProgramConfigService {

    @Resource
    private MiniProgramConfigMapper miniProgramConfigMapper;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private ContractMapper contractMapper;

    @Override
    public MiniProgramConfigRespVO getPublic() {
        MiniProgramConfigDO config = miniProgramConfigMapper.selectTheOne();
        return BeanUtils.toBean(config, MiniProgramConfigRespVO.class);
    }

    @Override
    public MiniProgramConfigDO getAdmin() {
        return miniProgramConfigMapper.selectTheOne();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MiniProgramConfigSaveReqVO reqVO) {
        MiniProgramConfigDO existing = miniProgramConfigMapper.selectTheOne();
        boolean appNameChanged = existing != null && !StrUtil.equals(existing.getAppName(), reqVO.getAppName());

        MiniProgramConfigDO update = new MiniProgramConfigDO();
        update.setId(1L);
        update.setAppName(reqVO.getAppName());
        update.setSlogan(reqVO.getSlogan());
        update.setAppDescription(reqVO.getAppDescription());
        update.setCompanyName(reqVO.getCompanyName());
        update.setContactEmail(reqVO.getContactEmail());
        update.setBoundUserId(reqVO.getBoundUserId());
        miniProgramConfigMapper.updateById(update);

        if (appNameChanged && reqVO.getBoundUserId() != null) {
            cascadeNameChange(reqVO.getBoundUserId(), reqVO.getAppName());
        }
    }

    private void cascadeNameChange(Long boundUserId, String newName) {
        AdminUserDO user = adminUserMapper.selectById(boundUserId);
        if (user == null || StrUtil.isBlank(user.getIdNo())) {
            log.warn("[MiniProgramConfig] 绑定用户 {} 不存在或没有身份证号，跳过姓名联动", boundUserId);
            return;
        }

        AdminUserDO userUpdate = new AdminUserDO();
        userUpdate.setId(boundUserId);
        userUpdate.setRealname(newName);
        adminUserMapper.updateById(userUpdate);

        contractMapper.updateIndebtedNameByIdCard(user.getIdNo(), newName);
        contractMapper.updateCreditorNameByIdCard(user.getIdNo(), newName);
    }

    @Override
    public int previewNameChangeImpact() {
        MiniProgramConfigDO existing = miniProgramConfigMapper.selectTheOne();
        if (existing == null || existing.getBoundUserId() == null) {
            return 0;
        }
        AdminUserDO user = adminUserMapper.selectById(existing.getBoundUserId());
        if (user == null || StrUtil.isBlank(user.getIdNo())) {
            return 0;
        }
        return contractMapper.countByPartyIdCard(user.getIdNo());
    }

}
