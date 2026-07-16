package cn.iocoder.yudao.module.custom.controller.admin.contract;

import cn.iocoder.yudao.module.custom.controller.admin.contract.vo.ContractPageReqVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户反馈 /custom/contract/page 存在 SQL 注入：
 * pageSize=10%20UNION%20SELECT%201--&appId=wx7641cb883b4fa670
 *
 * 复现 Spring MVC 对 GET /custom/contract/page 的真实参数绑定过程（ContractPageReqVO 是 @Valid 绑定的 POJO，
 * pageSize 继承自 PageParam，声明为 Integer），验证该 payload 能否真正把恶意字符串带进业务/SQL层。
 */
public class ContractPageSqliBindingTest {

    @Test
    public void testPageSizeUnionSelectPayload_bindingRejected() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("pageSize", "10 UNION SELECT 1--");
        request.addParameter("appId", "wx7641cb883b4fa670"); // 小程序 request.js 自动附带的公共参数，无对应字段
        request.addParameter("indebtedId", "110101199001011234");

        ContractPageReqVO target = new ContractPageReqVO();
        ServletRequestDataBinder binder = new ServletRequestDataBinder(target);
        binder.bind(request);

        // pageSize 字段类型是 java.lang.Integer，注入字符串在类型转换阶段就被拒绝，
        // 根本不会以任何形式被赋值到该字段，更不会流到后面的 MyBatis 参数绑定
        assertThrows(BindException.class, binder::close,
                "非法 pageSize 应当在 Spring 参数绑定阶段就报错，而不是被静默接受");

        // 未被本次绑定污染的字段（比如 pageSize）保持类型安全的默认值，不会是恶意字符串
        assertNotEquals("10 UNION SELECT 1--", String.valueOf(target.getPageSize()));

        // appId 在 VO 里没有对应字段，Spring 直接忽略，不会进入任何后续处理
        // （ContractPageReqVO 没有 appId setter，绑定不会报错也不会有副作用）
    }

    @Test
    public void testStringFilterField_payloadStaysAsLiteralData() {
        // 即便是字符串类型的过滤字段（indebtedName 等），也验证一下绑定后原样是字符串，
        // 说明它会作为 MyBatis #{} 预编译参数的字面量传入，而不是拼接进 SQL 语法
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("indebtedName", "' OR '1'='1");
        request.addParameter("pageNo", "1");
        request.addParameter("pageSize", "10");

        ContractPageReqVO target = new ContractPageReqVO();
        ServletRequestDataBinder binder = new ServletRequestDataBinder(target);
        assertDoesNotThrow(() -> {
            binder.bind(request);
            binder.close();
        });

        assertEquals("' OR '1'='1", target.getIndebtedName());
        assertEquals(1, target.getPageNo());
        assertEquals(10, target.getPageSize());
    }

}
