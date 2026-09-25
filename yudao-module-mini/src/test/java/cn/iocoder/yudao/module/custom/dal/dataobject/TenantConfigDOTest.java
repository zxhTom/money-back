package cn.iocoder.yudao.module.custom.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.changelog.VersionChangelogDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractModelDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.iconset.IconSetProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.miniconfig.MiniProgramConfigDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.skin.SkinProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.speedcontrol.SpeedControlConfigDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextItemDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.text.TextProfileDO;
import cn.iocoder.yudao.module.custom.dal.dataobject.timewindow.TimeWindowDO;
import cn.iocoder.yudao.module.fee.dal.dataobject.strategy.StrategyDO;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TenantConfigDOTest {

    @Test
    public void testTenConfigDOsExtendTenantBaseDO() {
        List<Class<?>> classes = Arrays.asList(
                SkinProfileDO.class,
                TextProfileDO.class,
                TextItemDO.class,
                IconSetProfileDO.class,
                MiniProgramConfigDO.class,
                VersionChangelogDO.class,
                SpeedControlConfigDO.class,
                StrategyDO.class,
                ContractModelDO.class,
                TimeWindowDO.class
        );

        for (Class<?> clazz : classes) {
            assertTrue(TenantBaseDO.class.isAssignableFrom(clazz),
                    clazz.getName() + " 应继承 TenantBaseDO");
            assertNotNull(clazz.getAnnotation(TableName.class),
                    clazz.getName() + " 应注解 @TableName");
        }
    }
}
