package cn.iocoder.yudao.module.custom.service.wechat;


import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.service.wechat.model.CombineUser;

/**
 *
 * @author zxhtom
 * 10/9/25
 */
public interface MiniUserService {

    public MiniUserDo selectMiniUser(String appId, String openId);

    public CombineUser selectMiniUserOrInitUserWithPrefix(String appId, String openId,String unionId,String prefix);

    public CombineUser initMini2Maltcloud(String prefix);

    Integer finishMiniUser(MiniUserDo miniUser);

    MiniUserDo bindMinUser(String openid, String unionid);
}
