package com.example.market.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建支付请求DTO
 */
@Data
@ApiModel(value = "创建支付请求参数")
public class PayCreateRequest {
    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "支付方式（1-微信，2-支付宝，3-银联）", required = true)
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    @ApiModelProperty(value = "支付金额", required = true)
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    @ApiModelProperty(value = "回调地址", required = true)
    @NotBlank(message = "回调地址不能为空")
    private String callbackUrl;

    @ApiModelProperty(value = "备注")
    private String remark;
}