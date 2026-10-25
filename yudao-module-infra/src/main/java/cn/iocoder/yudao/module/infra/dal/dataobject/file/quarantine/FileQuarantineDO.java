package cn.iocoder.yudao.module.infra.dal.dataobject.file.quarantine;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// 历史文件隔离清单 DO
@TableName("infra_file_quarantine")
@KeySequence("infra_file_quarantine_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class FileQuarantineDO extends BaseDO {

    @TableId
    private Long id;
    private Long configId;
    private String pathHash;
    private byte[] canonicalPath;
    private String contentHash;
    private String reason;
}
