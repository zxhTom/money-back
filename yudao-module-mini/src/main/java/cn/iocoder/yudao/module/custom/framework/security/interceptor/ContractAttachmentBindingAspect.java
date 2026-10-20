package cn.iocoder.yudao.module.custom.framework.security.interceptor;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Objects;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception0;

/** A party must not attach another uploader's private file to a contract it controls. */
@Aspect
@Component
public class ContractAttachmentBindingAspect {
    @Resource private ContractMapper contractMapper;
    @Resource private FileMapper fileMapper;
    @Before("execution(public * cn.iocoder.yudao.module.custom.service..*(..))")
    public void validatePrivateReferences(JoinPoint point) {
        for (Object arg : point.getArgs()) if (arg instanceof ContractSaveReqVO) validate((ContractSaveReqVO) arg);
    }
    void validate(ContractSaveReqVO input) {
        if (input.getFile() == null || input.getFile().isEmpty()) return;
        try {
            String rawPath = new URI(input.getFile()).getRawPath();
            String path = URLDecoder.decode(rawPath.replace("+", "%2B"), "UTF-8");
            if (path.contains("/../") || path.contains("/./") || path.contains("//")) throw exception0(403, "附件路径无效");
            if (!path.contains("/get/private/contracts/")) return;
            ContractDO previous = input.getId() == null ? null : contractMapper.selectById(input.getId());
            if (previous != null && previous.getFile() != null && Objects.equals(new URI(previous.getFile()).getPath(), path)) return;
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            String storagePath = path.substring(path.indexOf("/get/") + 5);
            if (userId == null || !storagePath.startsWith("private/contracts/" + userId + "/")) throw exception0(403, "不能引用他人的私有附件");
            FileDO file = fileMapper.selectOne(FileDO::getPath, storagePath);
            if (file == null || !String.valueOf(userId).equals(file.getCreator())) throw exception0(403, "附件不存在或不属于当前用户");
        } catch (cn.iocoder.yudao.framework.common.exception.ServiceException ex) { throw ex; }
        catch (Exception ex) { throw exception0(403, "附件引用无效"); }
    }
}
