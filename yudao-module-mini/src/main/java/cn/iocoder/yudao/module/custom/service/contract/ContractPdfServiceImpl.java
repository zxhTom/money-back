package cn.iocoder.yudao.module.custom.service.contract;

import cn.iocoder.yudao.module.custom.dal.dataobject.contract.ContractDO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zxhtom
 * 12/23/25
 */
@Service
public class ContractPdfServiceImpl implements ContractPdfService{
    
    /**
     * 生成完整的借款协议文本内容，使用 ContractDO 的字段动态替换
     */
    private List<String> buildAgreementContent(ContractDO contract) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        
        // 格式化日期
        String startDate = contract.getStartDate() != null ? contract.getStartDate().format(dateFormatter) : "";
        String endDate = contract.getEndDate() != null ? contract.getEndDate().format(dateFormatter) : "";
        String createDate = contract.getCreateTime() != null ? contract.getCreateTime().format(dateTimeFormatter) : "";
        
        // 格式化金额（分转元）
        String salaryYuan = contract.getSalary() != null ? String.format("%.2f", contract.getSalary() / 1.0) : "0.00";
        String interestYuan = contract.getInterest() != null ? String.format("%.2f", contract.getInterest()) : "0.00";
        
        // 计算本息合计和待还金额
        double salaryAmount = contract.getSalary() != null ? contract.getSalary() / 100.0 : 0;
        double interestAmount = contract.getInterest() != null ? contract.getInterest() : 0;
        double refundAmount = contract.getRefund() != null ? contract.getRefund() : 0;
        double totalAmount = salaryAmount + interestAmount;
        double remainingAmount = totalAmount - refundAmount;
        
        // 格式化费率（tariff 可能是百分比，需要确认单位，这里假设是百分比）
        String tariffPercent = contract.getTariff() != null ? String.valueOf(contract.getTariff()) : "0";
        
        // 完整的借款协议文本内容，按行分割，使用 ContractDO 字段动态替换
        return Arrays.asList(
            "借款协议",
            "",
            "协议编号：" + safeStr(String.valueOf(contract.getId())),
            "请您认真阅读并充分理解《借款协议》（以下简称\"本协议\"），您一经点击已阅读并同意本协议，即视为对本协议条款的理解和接受，您同意本协议对您具有法律约束力。如果您不同意本协议的任一内容或无法准确理解相关条款，请不要进行后续操作。",
            "",
            "本协议项下各方均已在\"极速合约\"平台注册，同意遵守\"极速合约\"平台的各项交易规则，各方在充分阅读理解本协议条款情形下，本着诚信自愿的原则签订本协议。",
            "",
            "甲方（借款人）：" + safeStr(contract.getIndebtedName()),
            "身份证号：" + safeStr(contract.getIndebtedId()),
            "乙方（出借人）：" + safeStr(contract.getCreditorName()),
            "身份证号：" + safeStr(contract.getCreditorId()),
            "",
            "鉴于：",
            "1.甲方与乙方自愿使用\"极速合约\"平台登记确认双方基于借款形成的债权债务法律关系；",
            "2.甲方与乙方知悉并同意本协议下各项条款，并自愿遵守相关约定内容。",
            "为此，双方经协商一致，在北京市海淀区签订本协议，达成协议条款如下：",
            "",
            "一、借款主要内容",
            "",
            "借款金额 " + salaryYuan + " 元",
            "年化利率 " + tariffPercent + " %",
            "应收利息 " + interestYuan + " 元",
            "本息合计 " + String.format("%.2f", totalAmount) + " 元",
            "待还金额 " + String.format("%.2f", remainingAmount) + " 元",
            "还款方式 " + safeStr(contract.getReturnType()),
            "借款日期 " + startDate,
            "还款日期 " + endDate,
            "借款用途 " + safeStr(contract.getReasonType()) + (contract.getDetailReason() != null && !contract.getDetailReason().isEmpty() ? "，" + contract.getDetailReason() : ""),
            "注：1.还款总额=借款金额×（1+借款时长/365×年化利率），其中借款时长为借款日期和还款日期间的自然日天数；此处的\"借款日期\"是指乙方向甲方实际提供借款且借款已到账的日期。甲方可多次还款直至待还本息全部还清，但提前还款并不减少待还本息。",
            "2.如无特别约定，本协议所指\"日\"均为日历日。",
            "",
            "二、偿还方式",
            "",
            "1.甲方必须按本协议的约定按时、足额偿还乙方的本金和利息，否则会对甲方的信用记录造成不良影响。还款日不受法定假日或公休日的影响，还款日当日甲方需履行还款义务。",
            "2.甲方可以通过线上或者线下的方式进行还款，线上还款是指甲方通过\"极速合约\"平台的账户系统将待还本息支付至乙方的账户中，如果甲方采取此种还款方式，需向\"极速合约\"平台支付线上还款手续费。",
            "3.如果甲方通过支付宝、微信或现金等线下方式将待还本息偿还给了乙方，乙方确认收到甲方的线下还款后，则视为甲方履行完毕本协议项下的还款义务。",
            "",
            "三、承诺与保证",
            "",
            "1.甲方与乙方承诺与保证",
            "（1）甲方与乙方在此确认：各自为具有完全民事权利能力和完全民事行为能力的主体，有权签订并履行本协议，并充分知晓其行为可能存在的各类风险，由本协议引起的违约偿还及其他风险等均由甲方与乙方自行承担；",
            "（2）甲方与乙方在此确认：双方在本协议项下的债权债务关系由双方自行撮合并最终达成，甲方与乙方基于该债权债务关系实施的任何行为及产生的任何后果均不依托于就该债权债务关系为甲方与乙方提供任何相关功能、工具或服务的主体；",
            "（3）甲方与乙方保证：本协议的签订及本协议项下约定的各项内容是双方真实意思表示，且本协议项下债权债务关系符合法律法规规定，否则因此产生的一切法律后果均由甲方与乙方自行承担。",
            "2.甲方承诺并保证：其自身具有与借款金额相匹配的还款能力并按照本协议约定还款，且还款资金来源是其合法合规的自有资金，不存在\"以贷养贷\"、\"多头借贷\"等违法违规行为。",
            "",
            "四、还款能力降低与违约救济",
            "",
            "1.甲方行为符合以下情形中之任意一种即视为还款能力降低：",
            "（1）在乙方以外与任何第三方的其他借款、担保、赔偿、承诺或任何其他债务出现严重违约情况，影响或可能影响甲方在本协议下的还款能力的；",
            "（2）部分或者全部丧失民事行为能力、死亡、被宣告死亡或者被宣告失踪，影响或可能影响甲方还款能力的；",
            "（3）甲方被采取刑事强制措施，影响或可能影响甲方还款能力的；",
            "（4）甲方财产被没收、征用、查封、损坏、扣押、冻结的或甲方财产遭受重大损失，影响或可能影响甲方还款能力的；",
            "（5）甲方因与本债务无关的其他争议致使支付账户被冻结、扣划或被采取纳入失信人名单等执行措施，影响或可能影响甲方还款能力的；",
            "（6）甲方为他人债务提供担保或以其主要财产向第三人抵押、质押，影响或可能影响甲方还款能力的；",
            "（7）出现任何其他影响或可能影响甲方还款能力的事件。",
            "2.若乙方判断甲方发生前述还款能力降低事件，乙方有权自行或委托\"极速合约\"平台或其指定的业务运营方等服务方采取下列一种或多种措施进行债权救济：",
            "（1）要求甲方立即偿还所有应付款项；",
            "（2）对甲方账户进行冻结等操作；",
            "（3）通过甲方提供的其亲朋好友、紧急联系人、指定联系人等第三人提醒、敦促甲方履行还款义务；",
            "（4）采取法律、法规以及本协议约定的其他救济措施。",
            "3.甲方行为符合以下情形中任意一种的即视为违约：",
            "（1）甲方借款到期未足额偿还应付款项的；",
            "（2）甲方提供虚假资料或隐瞒重要事实的。",
            "4.双方同意，若甲方发生违约行为，乙方有权自行或委托\"极速合约\"平台或其指定的业务运营方等服务方采取下列一种或多种措施协助乙方予以债权救济：",
            "（1）要求甲方立即偿还所有应付款；",
            "（2）对甲方账户进行冻结等操作；",
            "（3）自行或委托第三方通过电话/短信及其他合法方式实施委托提醒还款与调解；",
            "（4）在\"极速合约\"平台或其他渠道披露甲方的违约信息；",
            "（5）向甲方提供的其亲朋好友、紧急联系人、指定联系人等第三人提醒、敦促甲方履行还款义务；",
            "（6）将甲方违约情况提供给依法成立的个人征信机构；",
            "（7）将与甲方违约行为相关的判决书、裁定书、仲裁裁决书等文书用于督促还款用途的公开披露；",
            "（8）通过本协议及其从协议、补充协议约定的方式或其他途径收集、核验、储存甲方的信息并用于争议解决的目的，并有权向乙方及其他主体披露甲方相关信息；",
            "（9）向有关部门或者单位予以通报；",
            "（10）采取法律、法规以及本协议约定的其他救济措施。",
            "",
            "五、罚息计算及还款顺序",
            "",
            "1.自还款日的次日起计算罚息，以截至当日未偿还借款本金利息之和为基数，每日按年化利率14 %计收罚息。",
            "2.自还款日次日起，甲方的每笔还款金额包括借款本金、利息、罚息等其他费用。",
            "3.借款本金、利息、罚息的金额按照罚息、利息、借款本金的顺序依次偿还。",
            "",
            "六、送达",
            "",
            "1.本协议经甲方与乙方通过网络在线点击确认的方式进行签订，各方点击确认后本协议生效。",
            "2.各方同意以签约身份证广播所在地作为联络方式为各方之间履行本协议项下义务及诉讼/仲裁纠纷等相关材料送达地址。",
            "3.乙方和甲方知晓并同意：采取多种方式向本人送达的，送达时间以上述送达方式中最先送达的为准；在本协议的成立、履行、变更、解除和争议解决中，按上述送达地址邮寄、发送相关文件时，邮寄材料一经寄出或发送的电子数据、通讯信息一经发出，即视为成功送达；若发生送达障碍情形（包括但不限于收件人身份不明、无人签收、地址欠详、地址搬迁、长期未自取、被退回、拒收、电子邮箱不存在、格式无效、通讯工具停机或空号等），以邮寄文件邮戳记载的交寄时间、投递公司网站等载体载明的交寄时间或电子邮箱、短信、彩信、微信、系统推送信息等电子处理方式显示的发出时间视为送达时间。",
            "4.前述送达地址需要变更时，乙方及甲方应于变更事项发生始三个工作日内尽快通过\"极速合约\"平台修改并通知相关方，在修改变更生效前，原址码及送达方式合法有效，因怠于履行送达址码变更通知义务而造成的一切后果（包括但不限于特定权利丧失及损失等）由乙方或甲方自行承担。",
            "",
            "七、适用法律及争议解决",
            "",
            "1.本协议的签订、履行、终止、解释均适用中华人民共和国法律。",
            "2.本协议与附属文件，以及从协议项下产生的一切争议，双方方应协商解决，协商不成的，各方均同意通过第（）种方式予以解决争议。",
            "（一）若甲方与乙方发生任何纠纷或争议，首先应友好协商解决，协商不成的，双方均有权向债务人住所地、债权人（如有债权转让，最终债权受让人为债权人）住所地、保证人（如有）住所地、协议签订地、协议履行地有管辖权的人民法院起诉。",
            "（二）提交北海国际仲裁院仲裁，开庭地点为上海。各方同意共同委托仲裁院主任，结合案件实际情况指定仲裁庭组成方式及指定仲裁员。各方同意不开庭审理。各方确认仲裁程序中所有仲裁法律文书以电子送达方或进行送达，送达地址以各方在本协议及相关法律文件中的约定为准，包括但不限于电子邮箱、短信、传真、即时通讯工具等。若需要以其他方式进行送达的，由仲裁机构或仲裁庭决定。如发生送达地址变更，需提前3天书面通知他方，未经书面通知导致文书无法送达的，则该文书被退回之日或文书规定的期限届满之日起，视为送达。",
            "3.如相关服务方作为诉讼参与方的，由本协议签订地北京市海淀区有管辖权的人民法院管辖。",
            "4.相关的诉讼费用、仲裁费用、律师代理费等因追索权利而发生的合理费用均由败诉方承担。",
            "",
            "八、其他",
            "",
            "1.本协议采用电子文本形式制成，经双方在线同意后生效；如有补充协议，与本协议具有同等法律效力，双方均认可电子文本形式的协议效力。",
            "2.乙方和甲方均委托\"极速合约\"平台通过其设立的专用服务器保管所有与本协议及补充协议有关的书面文件和电子信息。甲方与乙方在\"极速合约\"平台的《用户注册协议》等内容适用于本协议并具有约束力。",
            "3.本协议任何条款的无效或不可强制执行并不影响本协议其它条款的效力及可强制执行性。",
            "4.本协议及补充协议的任何修改、补充均以电子文本形式作出。本协议双方确认并同意由\"极速合约\"平台或其指定的业务运营方提供的与本协议有关的书面文件或电子信息在无明显错误的情况下应作为本协议有关事项的终局证明。",
            "",
            "甲方（借款人）：" + safeStr(contract.getIndebtedName()),
            "乙方（出借人）：" + safeStr(contract.getCreditorName()),
            "日期：" + createDate
        );
    }
    
    /**
     * 安全获取字符串，避免 null
     */
    private String safeStr(String value) {
        return value != null ? value : "";
    }

    /**
     * 使用 Apache PDFBox 和 PDType0Font 生成 PDF
     * @return 包含 PDF 数据的字节数组
     * @throws IOException 如果字体加载或 PDF 写入失败
     */
    @Override
    public byte[] generateLoanAgreementPdf(ContractDO contractDO) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 1. 加载中文字体文件
            PDType0Font font;
            try (InputStream fontStream = new ClassPathResource("fonts/NotoSansSC-Regular.ttf").getInputStream()) {
                font = PDType0Font.load(document, fontStream);
            } catch (IOException e) {
                System.err.println("无法加载中文字体文件: " + e.getMessage());
                throw new IOException("Missing or invalid font file: SimSun.ttf", e);
            }

            // 页面设置
            PDRectangle mediaBox = PDRectangle.A4;
            float margin = 50;
            float width = mediaBox.getWidth() - 2 * margin;
            float yStart = mediaBox.getHeight() - margin;
            float yPosition = yStart;
            float leading = 18; // 行高
            float fontSize = 12;

            PDPage page = new PDPage(mediaBox);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(font, fontSize);
            contentStream.setLeading(leading);

            // 2. 构建动态内容
            List<String> agreementContent = buildAgreementContent(contractDO);

            // 3. 写入内容
            for (String line : agreementContent) {
                // 优化：根据内容长度动态调整字体大小或行高，这里保持一致

                // 标题处理
                if (line.equals("借款协议")) {
                    contentStream.setFont(font, 20);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= 30; // 标题后留更多空隙
                    contentStream.setFont(font, fontSize); // 恢复正文字体大小
                    continue;
                }

                // 章节标题处理
                if (line.matches("^[一二三四五六七八]、.*")) {
                    contentStream.setFont(font, 14);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= 25; // 章节标题后留空隙
                    contentStream.setFont(font, fontSize); // 恢复正文字体大小
                    continue;
                }

                // 空行处理
                if (line.trim().isEmpty()) {
                    yPosition -= leading / 2;
                    continue;
                }

                // 文本换行处理
                List<String> lines = splitTextToFit(line, font, fontSize, width);

                for (String text : lines) {
                    if (yPosition < margin) {
                        // 换页
                        contentStream.close();
                        page = new PDPage(mediaBox);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(font, fontSize);
                        contentStream.setLeading(leading);
                        yPosition = yStart;
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(text);
                    contentStream.endText();
                    yPosition -= leading;
                }
            }

            // 确保最后一个内容流被关闭
            contentStream.close();

            // 3. 保存文档到字节数组
            document.save(baos);
            return baos.toByteArray();

        }
    }

    /**
     * 精确的文本换行工具，基于字体宽度计算
     */
    private List<String> splitTextToFit(String text, PDFont font, float fontSize, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        while (text.length() > 0) {
            int startIndex = 0;
            int endIndex = text.length();

            // 尝试找到能容纳的最大子串
            while (startIndex < endIndex) {
                String subString = text.substring(startIndex, endIndex);
                float size = font.getStringWidth(subString) / 1000 * fontSize;

                if (size > width) {
                    // 文本太长，需要缩短
                    endIndex--;
                } else {
                    // 文本合适，跳出内层循环
                    break;
                }
            }

            // 如果 endIndex == 0，说明单个字符都放不下，这通常是字体或宽度设置问题，
            // 但为了健壮性，我们至少取一个字符
            if (endIndex == 0) {
                endIndex = 1;
            }

            // 提取合适的行
            String line = text.substring(0, endIndex);
            lines.add(line);

            // 移除已提取的行
            text = text.substring(endIndex).trim();
        }
        return lines;
    }
}
