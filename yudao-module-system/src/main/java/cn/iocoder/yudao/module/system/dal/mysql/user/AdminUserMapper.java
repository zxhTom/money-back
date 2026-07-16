package cn.iocoder.yudao.module.system.dal.mysql.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserPageReqVO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapperX<AdminUserDO> {

    /** 安全互查：按 用户名/昵称/真实姓名/身份证号 模糊匹配用户ID */
    @TenantIgnore
    @Select("<script>SELECT id FROM system_users WHERE deleted = 0 AND (" +
            "username LIKE CONCAT('%', #{kw}, '%') OR nickname LIKE CONCAT('%', #{kw}, '%') " +
            "OR realname LIKE CONCAT('%', #{kw}, '%') OR id_no = #{kw}) LIMIT 100</script>")
    List<Long> selectIdsByKeyword(@Param("kw") String kw);

    default AdminUserDO selectByUsername(String username) {
        List<AdminUserDO> list = selectListByUsername(username);
        return list.isEmpty() ? null : list.get(0);
    }

    default List<AdminUserDO> selectListByUsername(String username) {
        return selectList(AdminUserDO::getUsername, username);
    }

    default AdminUserDO selectByEmail(String email) {
        return selectOne(AdminUserDO::getEmail, email);
    }

    default AdminUserDO selectByMobile(String mobile) {
        return selectOne(AdminUserDO::getMobile, mobile);
    }

    /**
     * 根据身份证号精确查询（仅未删除的用户）
     */
    default AdminUserDO selectByIdNo(String idNo) {
        return selectOne(AdminUserDO::getIdNo, idNo);
    }

    /**
     * 根据真实姓名精确查询（仅未删除的用户）
     */
    default AdminUserDO selectByRealnameEqual(String realname) {
        return selectOne(AdminUserDO::getRealname, realname);
    }

    default PageResult<AdminUserDO> selectPage(UserPageReqVO reqVO, Collection<Long> deptIds, Collection<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AdminUserDO>()
                .likeIfPresent(AdminUserDO::getUsername, reqVO.getUsername())
                .likeIfPresent(AdminUserDO::getMobile, reqVO.getMobile())
                .eqIfPresent(AdminUserDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AdminUserDO::getIdNo, reqVO.getIdNo())
                .eqIfPresent(AdminUserDO::getVerified, reqVO.getVerified())
                .eqIfPresent(AdminUserDO::getPasswordStrength, reqVO.getPasswordStrength())
                .betweenIfPresent(AdminUserDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(AdminUserDO::getLoginDate, reqVO.getLoginDate())
                .inIfPresent(AdminUserDO::getDeptId, deptIds)
                .inIfPresent(AdminUserDO::getId, userIds)
                .orderByDesc(AdminUserDO::getId));
    }

    default List<AdminUserDO> selectListByNickname(String nickname) {
        return selectList(new LambdaQueryWrapperX<AdminUserDO>().like(AdminUserDO::getNickname, nickname));
    }

    default List<AdminUserDO> selectListByStatus(Integer status) {
        return selectList(AdminUserDO::getStatus, status);
    }

    default List<AdminUserDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(AdminUserDO::getDeptId, deptIds);
    }

    default List<AdminUserDO> selectByRealname(String realname){
        return selectList(new LambdaQueryWrapperX<AdminUserDO>().eq(AdminUserDO::getRealname, realname));
    }

}
