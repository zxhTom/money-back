package cn.iocoder.yudao.module.system.service.monitor;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.dal.dataobject.monitor.IpWhitelistDO;

/**
 * IP 白名单：命中的 IP 躲过所有安全封禁（黑名单）与多登录风控。
 */
public interface IpWhitelistService {

    /** 是否在白名单（走内存缓存，5分钟刷新） */
    boolean isWhitelisted(String ip);

    PageResult<IpWhitelistDO> getPage(PageParam pageParam);

    Long create(String ip, String remark);

    void update(Long id, String ip, String remark, Integer enabled);

    void delete(Long id);

    /** 手动刷新缓存 */
    void refreshCache();

    /** 是否放行内网 IP（开关） */
    boolean isInternalAllowed();

    /** 设置"放行内网 IP"开关 */
    void setInternalAllow(boolean enabled);

    /**
     * 按来源对账同步白名单：只增删该 source 的行，其它来源（如 MANUAL 手动添加）绝不触碰。
     * 库里属于该 source、但不在最新列表里的会被删除；最新列表里、库里没有的会新增。
     *
     * @param source 来源标识，如 "WECHAT"
     * @param ips    最新的完整 IP/网段列表（为空时直接跳过，避免误删）
     * @return 同步后的目标数量
     */
    int syncSourceIps(String source, java.util.List<String> ips);
}
