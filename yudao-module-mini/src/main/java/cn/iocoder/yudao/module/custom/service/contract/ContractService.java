package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 客户端 Service 接口
 *
 * @author 芋道源码
 */
public interface ContractService {

    /**
     * 创建客户端
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContract(@Valid ContractSaveReqVO createReqVO);

    /**
     * 更新客户端
     *
     * @param updateReqVO 更新信息
     */
    void updateContract(@Valid ContractSaveReqVO updateReqVO);

    /**
     * 删除客户端
     *
     * @param id 编号
     */
    void deleteContract(Long id);

    /**
    * 批量删除客户端
    *
    * @param ids 编号
    */
    void deleteContractListByIds(List<Long> ids);

    /**
     * 获得客户端
     *
     * @param id 编号
     * @return 客户端
     */
    ContractDO getContract(Long id);

    /**
     * 获得客户端分页
     *
     * @param pageReqVO 分页查询
     * @return 客户端分页
     */
    PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO);

}