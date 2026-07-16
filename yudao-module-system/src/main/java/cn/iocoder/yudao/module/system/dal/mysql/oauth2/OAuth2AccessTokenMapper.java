package cn.iocoder.yudao.module.system.dal.mysql.oauth2;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token.UserTokenStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OAuth2AccessTokenMapper extends BaseMapperX<OAuth2AccessTokenDO> {

    @TenantIgnore // 获取 token 的时候，需要忽略租户编号。原因是：一些场景下，可能不会传递 tenant-id 请求头，例如说文件上传、积木报表等等
    default OAuth2AccessTokenDO selectByAccessToken(String accessToken) {
        return selectOne(OAuth2AccessTokenDO::getAccessToken, accessToken);
    }

    default List<OAuth2AccessTokenDO> selectListByRefreshToken(String refreshToken) {
        return selectList(OAuth2AccessTokenDO::getRefreshToken, refreshToken);
    }

    @TenantIgnore
    default List<OAuth2AccessTokenDO> selectListByUserId(Long userId, Integer userType) {
        return selectList(new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .eq(OAuth2AccessTokenDO::getUserId, userId)
                .eq(OAuth2AccessTokenDO::getUserType, userType));
    }

    @Select("SELECT t.user_id AS userId, COUNT(*) AS tokenCount, u.username " +
            "FROM system_oauth2_access_token t " +
            "LEFT JOIN system_users u ON u.id = t.user_id AND u.deleted = 0 " +
            "WHERE t.deleted = 0 AND t.expires_time > NOW() " +
            "GROUP BY t.user_id, u.username ORDER BY tokenCount DESC LIMIT #{limit}")
    List<UserTokenStatVO> selectTopUsersByTokenCount(@Param("limit") int limit);

    /** 批量查询用户当前有效令牌数（内存聚合，避免注解动态 SQL） */
    @TenantIgnore
    default List<UserTokenStatVO> selectTokenCountsByUserIds(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<OAuth2AccessTokenDO> tokens = selectList(new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .in(OAuth2AccessTokenDO::getUserId, userIds)
                .gt(OAuth2AccessTokenDO::getExpiresTime, LocalDateTime.now()));
        java.util.Map<Long, Long> countMap = new java.util.LinkedHashMap<>();
        for (OAuth2AccessTokenDO t : tokens) {
            countMap.merge(t.getUserId(), 1L, Long::sum);
        }
        List<UserTokenStatVO> result = new java.util.ArrayList<>();
        countMap.forEach((uid, cnt) -> {
            UserTokenStatVO vo = new UserTokenStatVO();
            vo.setUserId(uid);
            vo.setTokenCount(cnt);
            result.add(vo);
        });
        return result;
    }

    default PageResult<OAuth2AccessTokenDO> selectPage(OAuth2AccessTokenPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .eqIfPresent(OAuth2AccessTokenDO::getUserId, reqVO.getUserId())
                .eqIfPresent(OAuth2AccessTokenDO::getUserType, reqVO.getUserType())
                .likeIfPresent(OAuth2AccessTokenDO::getClientId, reqVO.getClientId())
                .gt(OAuth2AccessTokenDO::getExpiresTime, LocalDateTime.now())
                .orderByDesc(OAuth2AccessTokenDO::getId));
    }

    /** 某 IP 下当前有效令牌涉及的不同用户ID（用于"一IP多用户"检测） */
    @TenantIgnore
    @Select("SELECT DISTINCT user_id FROM system_oauth2_access_token " +
            "WHERE deleted=0 AND expires_time > NOW() AND ip = #{ip}")
    List<Long> selectDistinctUserIdsByIp(@Param("ip") String ip);

    /** 某用户当前有效令牌涉及的不同 IP（用于"一用户多IP"检测） */
    @TenantIgnore
    @Select("SELECT DISTINCT ip FROM system_oauth2_access_token " +
            "WHERE deleted=0 AND expires_time > NOW() AND user_id = #{userId} AND ip IS NOT NULL AND ip <> ''")
    List<String> selectDistinctIpsByUserId(@Param("userId") Long userId);

    /** 给定一批用户，取其当前有效（未过期、有 IP）的令牌；聚合在 Service 层做，避免动态 SQL */
    @TenantIgnore
    default List<OAuth2AccessTokenDO> selectActiveByUserIds(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .in(OAuth2AccessTokenDO::getUserId, userIds)
                .gt(OAuth2AccessTokenDO::getExpiresTime, LocalDateTime.now())
                .isNotNull(OAuth2AccessTokenDO::getIp)
                .ne(OAuth2AccessTokenDO::getIp, ""));
    }

    /** 给定一批 IP，取其上当前有效的令牌 */
    @TenantIgnore
    default List<OAuth2AccessTokenDO> selectActiveByIps(java.util.Collection<String> ips) {
        if (ips == null || ips.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .in(OAuth2AccessTokenDO::getIp, ips)
                .gt(OAuth2AccessTokenDO::getExpiresTime, LocalDateTime.now()));
    }

}
