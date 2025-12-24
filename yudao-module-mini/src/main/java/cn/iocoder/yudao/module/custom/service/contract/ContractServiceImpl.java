package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractSaveReqVO;
import cn.iocoder.yudao.module.custom.controller.admin.wechat.vo.TemplateVO;
import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.custom.dal.mysql.contract.ContractMapper;
import cn.iocoder.yudao.module.custom.service.wechat.WechatService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.custom.enums.CustomErrorCodeConstants.CONTRACT_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_PASSWORD_FAILED;

/**
 * 合同 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ContractServiceImpl implements ContractService {

    @Resource
    ContractPdfService contractPdfService;
    @Resource
    private WechatService wechatService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private ContractMapper contractMapper;
    @Autowired
    private AdminUserMapper adminUserMapper;

    @Override
    public Long createContract(ContractSaveReqVO createReqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        AdminUserDO user = adminUserMapper.selectById(loginUserId);
        boolean matches = passwordEncoder.matches(createReqVO.getPassword(), user.getPayPassword());
        if (!matches) {
            throw exception(USER_PASSWORD_FAILED);
        }
        // 插入
        ContractDO contract = BeanUtils.toBean(createReqVO, ContractDO.class);
        contractMapper.insert(contract);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String formattedDate = now.format(formatter);
        TemplateVO templateVO = new TemplateVO();
        templateVO.setIdNo(contract.getCreditorId());
        templateVO.setTemplateId("zQEe3cyJ4Vru_n7SGzSFg4I_2dCs6y8Fp-Xj5blh6oM");
        templateVO.setDatas(new HashMap<String, Object>() {
            {
                put("phrase3", user.getRealname());
                put("character_string9", contract.getId());
                put("time2", contract.getCreateTime().format(formatter));
                put("amount6", contract.getSalary());
            }
        });
        try {
            wechatService.send(templateVO);
            templateVO.setIdNo(contract.getIndebtedId());
            wechatService.send(templateVO);
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        // 返回
        return contract.getId();
    }

    @Override
    public void updateContract(ContractSaveReqVO updateReqVO) {
        // 校验存在
        validateContractExists(updateReqVO.getId());
        // 更新
        ContractDO updateObj = BeanUtils.toBean(updateReqVO, ContractDO.class);
        contractMapper.updateById(updateObj);
    }

    @Override
    public void deleteContract(Long id) {
        // 校验存在
        validateContractExists(id);
        // 删除
        contractMapper.deleteById(id);
    }

    @Override
        public void deleteContractListByIds(List<Long> ids) {
        // 删除
        contractMapper.deleteByIds(ids);
        }


    private void validateContractExists(Long id) {
        if (contractMapper.selectById(id) == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
    }

    @Override
    public ContractDO getContract(Long id) {
        return contractMapper.selectById(id);
    }

    @Override
    public PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO) {
        return contractMapper.selectPage(pageReqVO);
    }

    @Override
    public void exportContractProtocolPdf(Long id, HttpServletResponse response) throws IOException {
        // 1. 校验合同是否存在
        ContractDO contract = contractMapper.selectById(id);
        if (contract == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }

        // 2. 创建新的 PDF 文档，根据 template.pdf 的结构在代码中输出完整内容
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();

            // 3. 加载中文字体（TTF），例如 NotoSansSC-Regular.ttf，放在 resources/fonts 下
            PDType0Font font;
            try (InputStream fontStream = this.getClass().getResourceAsStream("/fonts/NotoSansSC-Regular.ttf")) {
                if (fontStream == null) {
                    throw new IOException("字体文件 /fonts/NotoSansSC-Regular.ttf 未找到，请确认已放到 resources/fonts");
                }
                font = PDType0Font.load(document, fontStream, true);
            }

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 60;
                float y = mediaBox.getHeight() - margin;
                float leading = 20;

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String start = contract.getStartDate() != null ? contract.getStartDate().format(dateFormatter) : "";
                String end = contract.getEndDate() != null ? contract.getEndDate().format(dateFormatter) : "";

                // 标题
                writeTextAt(contentStream, "借款合同（协议）", font, 18,
                        (mediaBox.getWidth() - 18 * 6) / 2, y);
                y -= leading * 2;

                // 合同编号等基础信息（模仿 template.pdf 的头部区域）
                writeTextAt(contentStream, "合同编号：" + safe(contract.getId()), font, 12, margin, y);
                y -= leading;
                writeTextAt(contentStream,
                        "出借人（债权人）： " + safe(contract.getCreditorName()) +
                                "    身份证号码： " + safe(contract.getCreditorId()),
                        font, 12, margin, y);
                y -= leading;
                writeTextAt(contentStream,
                        "借款人（债务人）： " + safe(contract.getIndebtedName()) +
                                "    身份证号码： " + safe(contract.getIndebtedId()),
                        font, 12, margin, y);
                y -= leading;
                writeTextAt(contentStream,
                        "借款金额（大写）： " + safe(contract.getSalary()) +
                                "    借款金额（小写，元）： " + safe(contract.getSalary()),
                        font, 12, margin, y);
                y -= leading;
                writeTextAt(contentStream,
                        "借款期限： " + start + " 至 " + end + "    还款方式： " + safe(contract.getReturnType()),
                        font, 12, margin, y);
                y -= leading;
                writeTextAt(contentStream,
                        "借款事由： " + safe(contract.getReasonType()) + "，" + safe(contract.getDetailReason()),
                        font, 12, margin, y);
                y -= leading * 2;

                float maxWidth = mediaBox.getWidth() - margin * 2;

                // 以下条款内容是根据 template.pdf 的典型借款合同结构手工整理的静态文本，
                // 仅将与合同相关的部分（人名、金额、日期、事由等）用上面的动态字段填充。

                // 第一条 借款金额与用途
                drawParagraph(contentStream,
                        "第一条  借款金额与用途\n" +
                                "1.1 借款人向出借人借款总金额为上述约定金额，借款人确认上述借款金额真实、合法、有效。\n" +
                                "1.2 借款人承诺，前述借款仅用于合法用途，不得用于国家法律、法规及监管机构明令禁止的项目。",
                        font, 12, margin, y, leading, maxWidth);
                y -= leading * 4;

                // 第二条 利息及费用
                drawParagraph(contentStream,
                        "第二条  利息及费用\n" +
                                "2.1 双方同意按照约定的费率及方式计算本合同项下借款所产生的利息及相关费用。\n" +
                                "2.2 借款人在还款时，应一并支付截至当期应付的全部利息及相关费用。",
                        font, 12, margin, y, leading, maxWidth);
                y -= leading * 4;

                // 第三条 还款方式与期限
                drawParagraph(contentStream,
                        "第三条  还款方式与期限\n" +
                                "3.1 借款人应在借款期限届满前，按照本合同约定的方式和时间足额向出借人偿还全部本金、利息及相关费用。\n" +
                                "3.2 借款人可提前还款，但应提前通知出借人，并按双方约定的方式结清相关利息及费用。",
                        font, 12, margin, y, leading, maxWidth);
                y -= leading * 4;

                // 第四条 违约责任
                drawParagraph(contentStream,
                        "第四条  违约责任\n" +
                                "4.1 如借款人未按本合同约定的时间和金额还款，即视为违约，出借人有权要求借款人提前偿还全部借款本息，并承担相应违约责任。\n" +
                                "4.2 因借款人原因导致出借人产生的追索费用（包括但不限于律师费、诉讼费等），均由借款人承担。",
                        font, 12, margin, y, leading, maxWidth);
                y -= leading * 4;

                // 第五条 争议解决
                drawParagraph(contentStream,
                        "第五条  争议解决\n" +
                                "5.1 本合同在履行过程中发生的任何争议，双方应友好协商解决；协商不成的，任一方均可向出借人所在地有管辖权的人民法院提起诉讼。",
                        font, 12, margin, y, leading, maxWidth);
                y -= leading * 3;

                // 签署信息
                drawParagraph(contentStream,
                        "本合同一式两份，出借人和借款人各执一份，具有同等法律效力。合同自双方签字或盖章之日起生效。",
                        font, 12, margin, y, leading, maxWidth);
                y -= leading * 3;

                writeTextAt(contentStream,
                        "出借人（签字）： " + safe(contract.getCreditorName()),
                        font, 12, margin, y);
                y -= leading * 2;
                writeTextAt(contentStream,
                        "借款人（签字）： " + safe(contract.getIndebtedName()),
                        font, 12, margin, y);
            }

            // 5. 设置响应头并输出
            String fileName = ("contract-" + id + ".pdf");
            response.setContentType("application/pdf");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()) + "\"");

//            document.save(response.getOutputStream());
//            response.flushBuffer();
            // 3. 获取输出流并写入字节
            byte[] bytes = contractPdfService.generateLoanAgreementPdf();
            try (OutputStream os = response.getOutputStream()) {
                os.write(bytes); // 写入全部字节
                os.flush(); // 刷新流（确保数据发送）
            }
        }
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 在指定坐标写一行文本
     */
    private void writeTextAt(PDPageContentStream contentStream,
                             String text,
                             PDType0Font font,
                             float fontSize,
                             float x,
                             float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    /**
     * 以简单自动换行的方式输出一段文本，模仿模板中的多行条款
     */
    private void drawParagraph(PDPageContentStream contentStream,
                               String text,
                               PDType0Font font,
                               float fontSize,
                               float startX,
                               float startY,
                               float leading,
                               float maxWidth) throws IOException {
        float x = startX;
        float y = startY;
        String[] lines = text.split("\n");
        for (String line : lines) {
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                current.append(c);
                float w = font.getStringWidth(current.toString()) / 1000 * fontSize;
                if (w > maxWidth) {
                    writeTextAt(contentStream, current.toString(), font, fontSize, x, y);
                    y -= leading;
                    current.setLength(0);
                }
            }
            if (current.length() > 0) {
                writeTextAt(contentStream, current.toString(), font, fontSize, x, y);
                y -= leading;
            }
        }
    }

}