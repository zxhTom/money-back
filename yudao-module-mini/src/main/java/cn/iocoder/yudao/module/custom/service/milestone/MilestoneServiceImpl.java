package cn.iocoder.yudao.module.custom.service.milestone;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.custom.controller.admin.milestone.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.milestone.MilestoneDO;
import cn.iocoder.yudao.module.custom.dal.mysql.milestone.MilestoneMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class MilestoneServiceImpl implements MilestoneService {

    @Resource
    private MilestoneMapper milestoneMapper;

    @Override
    public Long createMilestone(MilestoneCreateReqVO createReqVO) {
        MilestoneDO milestone = BeanUtils.toBean(createReqVO, MilestoneDO.class);
        if (milestone.getStatus() == null) {
            milestone.setStatus(0);
        }
        if (milestone.getRemindDays() == null) {
            milestone.getRemindDays();
            milestone.setRemindDays(30);
        }
        if (milestone.getPriority() == null) {
            milestone.setPriority("MEDIUM");
        }
        milestoneMapper.insert(milestone);
        return milestone.getId();
    }

    @Override
    public void updateMilestone(MilestoneUpdateReqVO updateReqVO) {
        validateMilestoneExists(updateReqVO.getId());
        MilestoneDO updateObj = BeanUtils.toBean(updateReqVO, MilestoneDO.class);
        milestoneMapper.updateById(updateObj);
    }

    @Override
    public void deleteMilestone(Long id) {
        validateMilestoneExists(id);
        milestoneMapper.deleteById(id);
    }

    @Override
    public MilestoneDO getMilestone(Long id) {
        return milestoneMapper.selectById(id);
    }

    @Override
    public PageResult<MilestoneDO> getMilestonePage(MilestonePageReqVO pageReqVO) {
        return milestoneMapper.selectPage(pageReqVO);
    }

    @Override
    public MilestoneSummaryRespVO getMilestoneSummary() {
        List<MilestoneDO> all = milestoneMapper.selectList();
        LocalDate today = LocalDate.now();

        int expiredCount = 0;
        int urgentCount = 0;
        int pendingCount = 0;
        int completedCount = 0;

        List<MilestoneRespVO> upcomingList = new ArrayList<>();

        for (MilestoneDO milestone : all) {
            MilestoneRespVO resp = convertToResp(milestone, today);
            
            if (milestone.getStatus() == 2) {
                completedCount++;
            } else if (milestone.getStatus() == 0 || milestone.getStatus() == 1) {
                pendingCount++;
            }

            if (milestone.getStatus() != 2 && milestone.getStatus() != 3) {
                if (resp.getDaysLeft() < 0) {
                    expiredCount++;
                } else if (resp.getDaysLeft() <= 15) {
                    urgentCount++;
                }
                if (resp.getDaysLeft() <= (milestone.getRemindDays() != null ? milestone.getRemindDays() : 30)) {
                    upcomingList.add(resp);
                }
            }
        }

        upcomingList.sort((a, b) -> Integer.compare(a.getDaysLeft(), b.getDaysLeft()));

        return MilestoneSummaryRespVO.builder()
                .alertCount(expiredCount + urgentCount)
                .expiredCount(expiredCount)
                .urgentCount(urgentCount)
                .pendingCount(pendingCount)
                .completedCount(completedCount)
                .upcomingList(upcomingList)
                .build();
    }

    @Override
    public String syncCertMilestones() {
        String[] domains = new String[]{"brain.zxhtom.store", "org.zxhtom.store", "wecom-kf.zxhtom.store"};
        int updated = 0;
        for (String domain : domains) {
            try {
                javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
                sc.init(null, new javax.net.ssl.TrustManager[]{
                        new javax.net.ssl.X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        }
                }, new java.security.SecureRandom());

                javax.net.ssl.SSLSocketFactory sf = sc.getSocketFactory();
                try (javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) sf.createSocket(domain, 443)) {
                    socket.setSoTimeout(5000);
                    socket.startHandshake();
                    java.security.cert.Certificate[] certs = socket.getSession().getPeerCertificates();
                    if (certs.length > 0 && certs[0] instanceof java.security.cert.X509Certificate) {
                        java.security.cert.X509Certificate x509 = (java.security.cert.X509Certificate) certs[0];
                        java.util.Date notAfter = x509.getNotAfter();
                        LocalDate certDate = notAfter.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                        String title = "域名 " + domain + " SSL证书到期";
                        MilestoneDO exist = milestoneMapper.selectOne(MilestoneDO::getTitle, title);
                        if (exist == null) {
                            MilestoneDO newOne = MilestoneDO.builder()
                                    .title(title)
                                    .category("DOMAIN")
                                    .targetDate(certDate)
                                    .remindDays(20)
                                    .status(0)
                                    .priority("HIGH")
                                    .owner("系统自动探测")
                                    .remark("自动连通 " + domain + ":443 解析得到SSL证书过期时间。")
                                    .build();
                            milestoneMapper.insert(newOne);
                        } else {
                            exist.setTargetDate(certDate);
                            milestoneMapper.updateById(exist);
                        }
                        updated++;
                    }
                }
            } catch (Exception e) {
                // Ignore single domain connection failure
            }
        }
        return "成功完成 " + domains.length + " 个域名SSL证书到期时间探测，同步更新 " + updated + " 条节点。";
    }

    private void validateMilestoneExists(Long id) {
        if (milestoneMapper.selectById(id) == null) {
            throw new cn.iocoder.yudao.framework.common.exception.ServiceException(400, "节点记录不存在");
        }
    }

    public static MilestoneRespVO convertToResp(MilestoneDO milestone, LocalDate today) {
        MilestoneRespVO resp = BeanUtils.toBean(milestone, MilestoneRespVO.class);
        if (milestone.getTargetDate() != null) {
            long days = ChronoUnit.DAYS.between(today, milestone.getTargetDate());
            resp.setDaysLeft((int) days);

            if (milestone.getStatus() == 2 || milestone.getStatus() == 3) {
                resp.setAlertStatus("DONE");
            } else if (days < 0) {
                resp.setAlertStatus("EXPIRED");
            } else if (days <= (milestone.getRemindDays() != null ? milestone.getRemindDays() : 30)) {
                resp.setAlertStatus("URGENT");
            } else {
                resp.setAlertStatus("NORMAL");
            }
        } else {
            resp.setDaysLeft(999);
            resp.setAlertStatus("NORMAL");
        }
        return resp;
    }
}
