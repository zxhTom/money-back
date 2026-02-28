package cn.iocoder.yudao.module.custom.service.contract;

import java.io.IOException;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.*;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 合同 Service 接口
 *
 * @author 芋道源码
 */
public interface ContractService {

    /**
     * 创建合同
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createContract(@Valid ContractSaveReqVO createReqVO);

    /**
     * 更新合同
     *
     * @param updateReqVO 更新信息
     */
    void updateContract(@Valid ContractSaveReqVO updateReqVO);

    /**
     * 删除合同
     *
     * @param id 编号
     */
    void deleteContract(Long id);

    /**
    * 批量删除合同
    *
    * @param ids 编号
    */
    void deleteContractListByIds(List<Long> ids);

    /**
     * 获得合同
     *
     * @param id 编号
     * @return 合同
     */
    ContractDO getContract(Long id);

    /**
     * 获得合同分页
     *
     * @param pageReqVO 分页查询
     * @return 合同分页
     */
    PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO);

    /**
     * 获得当前登录用户创建的合同分页
     *
     * @param pageReqVO 分页查询
     * @return 合同分页
     */
    PageResult<ContractDO> getSelfContractPage(ContractPageReqVO pageReqVO);

    /**
     * 根据合同 ID 导出合同协议 PDF
     *
     * @param id       合同编号
     * @param response HTTP 响应
     */
    void exportContractProtocolPdf(Long id, javax.servlet.http.HttpServletResponse response) throws java.io.IOException;

    void exportContractProtocolContractPdf(ContractDO contractDO, HttpServletResponse response) throws IOException;
}