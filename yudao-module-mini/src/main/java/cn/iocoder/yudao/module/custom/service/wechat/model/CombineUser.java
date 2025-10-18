package cn.iocoder.yudao.module.custom.service.wechat.model;

import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import lombok.Data;

@Data
public class CombineUser {
    private boolean registed;
    private AdminUserDO maltcloud;
    private MiniUserDo outUser;
}
