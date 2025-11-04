package cn.iocoder.yudao.module.custom.service.wechat.impl;

import cn.iocoder.yudao.module.custom.dal.dataobject.wechat.MiniUserDo;
import cn.iocoder.yudao.module.custom.dal.mysql.wechat.MiniUserMapper;
import cn.iocoder.yudao.module.custom.service.wechat.MiniUserService;
import cn.iocoder.yudao.module.custom.service.wechat.model.CombineUser;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.anji.captcha.util.MD5Util;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @author zxhtom
 * 10/9/25
 */
@Service
@Slf4j
public class MiniUserServiceImpl implements MiniUserService {
    @Value("${zxhtom.security.user.password:123456}")
    String password;
    @Autowired
    MiniUserMapper miniUserMapper;
    @Autowired
    private AdminUserService userService;
    @Override
    public MiniUserDo selectMiniUser(String appId, String openId) {
        QueryWrapper<MiniUserDo> miniUserDoQueryWrapper = new QueryWrapper<>();
        miniUserDoQueryWrapper.eq("app_id", appId);
        miniUserDoQueryWrapper.eq("open_id", openId);
        return miniUserMapper.selectOne(miniUserDoQueryWrapper);
    }

    @Override
    public CombineUser selectMiniUserOrInitUserWithPrefix(String appId, String openId, String prefix) {
        CombineUser combineUser = new CombineUser();
        MiniUserDo miniUser = selectMiniUser(appId, openId);
        if (null == miniUser) {
            AdminUserDO user = new AdminUserDO();
            user.setId(System.currentTimeMillis());
            user.setUsername(String.format("%s_%s_%s", prefix, appId, openId));
            user.setNickname(MD5Util.md5(user.getUsername()));
//            user.setUserCode(openId.hashCode());
            user.setPassword(password);
            userService.insertUserSimply(user);
            log.debug("mini user init successful");
            combineUser.setMaltcloud(user);
            miniUser = new MiniUserDo();
            miniUser.setId(System.currentTimeMillis());
            miniUser.setAppId(appId);
            miniUser.setOpenId(openId);
            miniUser.setUserId(user.getId());
            miniUserMapper.insert(miniUser);
            combineUser.setRegisted(false);
        } else {
            combineUser.setRegisted(true);
            AdminUserDO user = userService.getUserSimple(miniUser.getUserId());
            if (user == null) {
                user = new AdminUserDO();
                user.setId(System.currentTimeMillis());
                user.setUsername(String.format("%s_%s_%s", prefix, appId, openId));
                user.setNickname(MD5Util.md5(user.getUsername()));
//            user.setUserCode(openId.hashCode());
                user.setPassword(password);
                userService.insertUserSimply(user);
            }
            combineUser.setMaltcloud(user);
        }
        combineUser.setOutUser(miniUser);
        return combineUser;
    }

    @Override
    public CombineUser initMini2Maltcloud(String prefix) {
        return null;
    }

    @Override
    public Integer finishMiniUser(MiniUserDo miniUser) {
        return miniUserMapper.finishMiniUser(miniUser);
    }
}
