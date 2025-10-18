package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.CONTRACT_NOT_EXISTS;

/**
 * 客户端 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ContractServiceImpl implements ContractService {

    @Resource
    private ContractMapper contractMapper;

    @Override
    public Long createContract(ContractSaveReqVO createReqVO) {
        // 插入
        ContractDO contract = BeanUtils.toBean(createReqVO, ContractDO.class);
        contractMapper.insert(contract);

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