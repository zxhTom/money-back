package cn.iocoder.yudao.module.custom.security;
import cn.iocoder.yudao.module.system.service.user.AdminUserServiceImpl;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ManualVerificationTest {
 @Mock AdminUserMapper userMapper;
 @Mock UserRoleMapper userRoleMapper;
 @Mock RoleService roleService;
 @InjectMocks AdminUserServiceImpl service;
 AdminUserDO profile(Integer state) { AdminUserDO u=new AdminUserDO();u.setId(42L);u.setVerified(state);u.setRealname("Example");u.setIdNo("encrypted-value");return u; }
 @Test void directServiceRejectsInvalidInputsBeforeWriting() {
  assertThrows(RuntimeException.class,()->service.updateUserVerified(42L,0,2,"reason"));
  assertThrows(RuntimeException.class,()->service.updateUserVerified(42L,0,1," "));
  verifyNoInteractions(userMapper);
 }
 @Test void expectedStateMismatchCannotOverwrite() {
  when(userMapper.selectById(42L)).thenReturn(profile(1));
  assertThrows(RuntimeException.class,()->service.updateUserVerified(42L,0,1,"review"));
  verify(userMapper,never()).update(any(AdminUserDO.class),any(Wrapper.class));
 }
 @Test void incompleteIdentityCannotBeApproved() {
  AdminUserDO u=profile(0);u.setIdNo(null);when(userMapper.selectById(42L)).thenReturn(u);
  assertThrows(RuntimeException.class,()->service.updateUserVerified(42L,0,1,"review"));
  verify(userMapper,never()).update(any(AdminUserDO.class),any(Wrapper.class));
 }
 @Test void concurrentChangeRequiresRefresh() {
  when(userMapper.selectById(42L)).thenReturn(profile(0));
  when(userMapper.update(any(AdminUserDO.class),any(Wrapper.class))).thenReturn(0);
  assertThrows(RuntimeException.class,()->service.updateUserVerified(42L,0,1,"review"));
 }
 @Test void legacyNullStateCanBeReviewed() {
  when(userMapper.selectById(42L)).thenReturn(profile(null));
  when(userMapper.update(any(AdminUserDO.class),any(Wrapper.class))).thenReturn(1);
  assertDoesNotThrow(()->service.updateUserVerified(42L,0,1,"review"));
  ArgumentCaptor<AdminUserDO> saved=ArgumentCaptor.forClass(AdminUserDO.class);
  verify(userMapper).update(saved.capture(),any(Wrapper.class));assertEquals(1,saved.getValue().getVerified());
 }
}
