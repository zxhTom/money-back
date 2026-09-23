package cn.iocoder.yudao.module.custom.controller.admin.pwd;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.custom.dal.dataobject.pwd.AutoResetPwdUserDO;
import cn.iocoder.yudao.module.custom.dal.mysql.pwd.AutoResetPwdUserMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AutoResetPwdUserControllerTest {

    @Mock
    private AutoResetPwdUserMapper autoResetPwdUserMapper;

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AutoResetPwdUserController controller;

    @Test
    public void testCase1_ListUsers_FiltersOtherTenantUsers() {
        AutoResetPwdUserDO itemA = new AutoResetPwdUserDO();
        itemA.setId(1L);
        itemA.setUserId(100L);

        AutoResetPwdUserDO itemB = new AutoResetPwdUserDO();
        itemB.setId(2L);
        itemB.setUserId(200L);

        when(autoResetPwdUserMapper.selectAll()).thenReturn(Arrays.asList(itemA, itemB));

        AdminUserDO userA = new AdminUserDO();
        userA.setId(100L);
        userA.setUsername("userA");

        when(adminUserService.getUser(100L)).thenReturn(userA);
        when(adminUserService.getUser(200L)).thenReturn(null);

        CommonResult<List<AutoResetPwdUserController.AutoResetPwdUserVO>> response = controller.listUsers();

        assertNotNull(response);
        assertEquals(0, response.getCode());
        List<AutoResetPwdUserController.AutoResetPwdUserVO> list = response.getData();
        assertEquals(1, list.size());
        assertEquals(100L, list.get(0).getUserId());
        assertEquals("userA", list.get(0).getUsername());
    }

    @Test
    public void testCase2_RemoveUser_UserNotFoundInTenant_ThrowsException() {
        Long userIdB = 200L;
        when(adminUserService.getUser(userIdB)).thenReturn(null);

        assertThrows(ServiceException.class, () -> controller.removeUser(userIdB));
        verify(autoResetPwdUserMapper, never()).deleteByUserId(any());
    }

    @Test
    public void testCase3_RemoveUser_UserExistsInTenant_DeletesSuccessfully() {
        Long userIdA = 100L;
        AdminUserDO userA = new AdminUserDO();
        userA.setId(userIdA);

        when(adminUserService.getUser(userIdA)).thenReturn(userA);

        CommonResult<Boolean> response = controller.removeUser(userIdA);

        assertNotNull(response);
        assertEquals(0, response.getCode());
        assertTrue(response.getData());
        verify(autoResetPwdUserMapper, times(1)).deleteByUserId(userIdA);
    }

}
