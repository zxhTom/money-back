package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.dto.TemplateMessageDTO;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.CONTRACT_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_PASSWORD_FAILED;

/**
 * 合同 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ContractServiceImpl implements ContractService {

    @Resource
    private WechatService wechatService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ContractMapper contractMapper;
    @Autowired
    private AdminUserMapper adminUserMapper;

    @Override
    public Long createContract(ContractSaveReqVO createReqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        AdminUserDO user = adminUserMapper.selectById(loginUserId);
        boolean matches = passwordEncoder.matches(createReqVO.getPassword(), user.getPassword());
        if (!matches) {
            throw exception(USER_PASSWORD_FAILED);
        }
        // 插入
        ContractDO contract = BeanUtils.toBean(createReqVO, ContractDO.class);
        contractMapper.insert(contract);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String formattedDate = now.format(formatter);
        TemplateVO templateVO = new TemplateVO();
        templateVO.setIdNo(contract.getCreditorId());
        templateVO.setTemplateId("zQEe3cyJ4Vru_n7SGzSFg4I_2dCs6y8Fp-Xj5blh6oM");
        templateVO.setDatas(new HashMap<String, Object>() {
            {
                put("phrase3", user.getRealname());
                put("character_string9", contract.getId());
                put("time2", contract.getCreateTime().format(formatter));
                put("amount6", contract.getSalary());
            }
        });
        try {
            wechatService.send(templateVO);
            templateVO.setIdNo(contract.getIndebtedId());
            wechatService.send(templateVO);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        // 返回
        return contract.getId();
    }

    @Override
    public void updateContract(ContractSaveReqVO updateReqVO) {
        // 校验存在
        validateContractExists(updateReqVO.getId());
        // 更新
        ContractDO updateObj = BeanUtils.toBean(updateReqVO, ContractDO.class);
        contractMapper.updateById(updateObj);
    }

    @Override
    public void deleteContract(Long id) {
        // 校验存在
        validateContractExists(id);
        // 删除
        contractMapper.deleteById(id);
    }

    @Override
        public void deleteContractListByIds(List<Long> ids) {
        // 删除
        contractMapper.deleteByIds(ids);
        }


    private void validateContractExists(Long id) {
        if (contractMapper.selectById(id) == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
    }

    @Override
    public ContractDO getContract(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    public PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO) {
        return contractMapper.selectPage(pageReqVO);
    }

}