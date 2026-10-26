package com.scaffold.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 充值请求体（POST /auth/recharge）
 */
@Data
public class RechargeRequest {

    /** 充值金额：必须 > 0；上限 10000 防手滑多输几个 0 */
    @NotNull(message = "充值金额不能为空")
    @Positive(message = "充值金额必须大于 0")
    @Max(value = 10000, message = "单次充值不能超过 10000 元")
    private Double amount;

    /** 支付方式（微信支付 / 支付宝支付）——模拟字段，只记录不判断 */
    private String type;
}
