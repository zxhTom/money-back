package cn.iocoder.yudao.module.custom.framework.text;

import cn.iocoder.yudao.module.custom.controller.admin.text.vo.PageCatalogNodeVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 小程序页面清单，供管理端文案配置导航使用。
 *
 * 静态硬编码维护，不依赖数据库；内容不需要穷尽所有 key，仅搭好分包+页面的骨架。
 */
public class PageCatalog {

    public static List<PageCatalogNodeVO> getCatalogTree() {
        List<PageCatalogNodeVO> tree = new ArrayList<>();
        tree.add(group("root", "主包", "index", "首页", "contract", "合同",
                "credit", "征信", "profile", "我的", "tab", "底部导航"));
        tree.add(group("auth", "登录注册",
                "auth.login", "登录", "auth.register", "注册"));
        tree.add(group("contract", "合同业务",
                "contract.contractDetail", "合同详情",
                "contract.contractManagement", "合同管理",
                "contract.contractShareEntry", "合同分享入口",
                "contract.sharecontract", "分享合同",
                "contract.signContract", "签约",
                "contract.supplementContract", "补充合同",
                "contract.selectDebtor", "选择债务人",
                "contract.iouConfirm", "欠条确认",
                "contract.debtSettlement", "债务结算",
                "contract.extension", "展期",
                "contract.step", "步骤引导"));
        tree.add(group("credit", "征信业务",
                "credit.creditDetail", "征信详情",
                "credit.creditQuery", "征信查询"));
        tree.add(group("user", "用户中心",
                "user.userinfo", "个人信息",
                "user.settings", "设置",
                "user.abouts", "关于我们",
                "user.feedbacks", "意见反馈",
                "user.faceAuth", "人脸认证",
                "user.webView", "网页视图",
                "user.authResult", "认证结果",
                "user.captcha", "验证码",
                "user.privacyPolicy", "隐私政策",
                "user.userAgreement", "用户协议",
                "user.maintenance", "维护中",
                "user.serviceUnavailable", "服务不可用"));
        tree.add(group("admin", "后台管理",
                "admin.dashboard", "数据大盘",
                "admin.dashboardChart", "数据图表",
                "admin.securityAlert", "安全告警",
                "admin.securityBlacklist", "安全黑名单",
                "admin.securityDiagnose", "安全诊断"));
        return tree;
    }

    /** pairs 为 key1, label1, key2, label2... 的二级子节点列表 */
    private static PageCatalogNodeVO group(String key, String label, String... pairs) {
        PageCatalogNodeVO node = new PageCatalogNodeVO();
        node.setKey(key);
        node.setLabel(label);
        List<PageCatalogNodeVO> children = new ArrayList<>();
        List<String> list = Arrays.asList(pairs);
        for (int i = 0; i < list.size(); i += 2) {
            PageCatalogNodeVO child = new PageCatalogNodeVO();
            child.setKey(list.get(i));
            child.setLabel(list.get(i + 1));
            children.add(child);
        }
        node.setChildren(children);
        return node;
    }

}
