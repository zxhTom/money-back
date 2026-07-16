package cn.iocoder.yudao.module.system.controller.admin.monitor.vo;

import lombok.Data;

import java.util.List;

/**
 * 在线 IP/用户 互查结果。
 * type=IP：users 为该 IP 上的在线用户，每个带其在用 IP 数；
 * type=USER：ips 为匹配用户在用的 IP，每个带其在线用户数。
 */
@Data
public class OnlineLookupRespVO {

    private String type;
    private Integer total;
    private List<UserRow> users;
    private List<IpRow> ips;

    @Data
    public static class UserRow {
        private Long userId;
        private String username;
        private String nickname;
        private Integer ipCount;
    }

    @Data
    public static class IpRow {
        private String ip;
        private Integer userCount;
    }
}
