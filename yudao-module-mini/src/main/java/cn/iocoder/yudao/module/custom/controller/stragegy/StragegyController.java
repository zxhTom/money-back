package cn.iocoder.yudao.module.custom.controller.stragegy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.FeeCalculateRequest;
import cn.iocoder.yudao.module.fee.controller.admin.strategy.vo.FeeCalculationResult;
import cn.iocoder.yudao.module.fee.service.strategy.FeeCalculationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 费用策略")
@RestController
@RequestMapping("/custom/strategy")
@Validated
@Slf4j
public class StragegyController {

    @Autowired
    private FeeCalculationService feeCalculationService;

    /**
     * 计算单笔费用
     */
    @PostMapping("/calculate")
    public CommonResult<FeeCalculationResult> calculateFee(@Valid @RequestBody FeeCalculateRequest request) {
        try {
            FeeCalculationResult result;
            if (Boolean.TRUE.equals(request.getUseDatabase())) {
                result = feeCalculationService.calculateFeeFromDB(request.getAmount());
            } else {
                result = feeCalculationService.calculateFee(request.getAmount());
            }
            return success(result);
        } catch (Exception e) {
            log.error("费用计算失败", e);
            return error(1505,e.getMessage());
        }
    }

    /**
     * 批量计算费用
     */
    @PostMapping("/batch-calculate")
    public CommonResult<List<FeeCalculationResult>> batchCalculate(@RequestBody List<BigDecimal> amounts) {
        try {
            List<FeeCalculationResult> results = feeCalculationService.batchCalculate(amounts);
            return success(results);
        } catch (Exception e) {
            log.error("批量费用计算失败", e);
            return error(1505,e.getMessage());
        }
    }

    /**
     * 测试不同金额的费用
     */
    @GetMapping("/test")
    public CommonResult<List<FeeCalculationResult>> testFeeCalculation() {
        List<BigDecimal> testAmounts = Arrays.asList(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(25000)
        );

        List<FeeCalculationResult> results = feeCalculationService.batchCalculate(testAmounts);
        return success(results);
    }
}
