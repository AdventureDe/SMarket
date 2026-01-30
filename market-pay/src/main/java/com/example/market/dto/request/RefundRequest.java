package com.example.market.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 退款请求DTO
 */
@Data
@ApiModel(value = "退款请求参数")
public class RefundRequest {
    @ApiModelProperty(value = "支付ID", required = true)
    @NotNull(message = "支付ID不能为空")
    private Long payId;

    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "退款金额", required = true)
    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款原因", required = true)
    @NotBlank(message = "退款原因不能为空")
    private String refundReason;
}