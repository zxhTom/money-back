package cn.iocoder.yudao.module.custom.service.autotest;

public interface AutoTestService {
    boolean runFullTest() throws Exception;
    boolean hasAdminRole();
}
