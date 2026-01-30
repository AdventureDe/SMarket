package com.example.market.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 支付回调请求DTO
 */
@Data
@ApiModel(value = "支付回调请求参数")
public class PayCallbackRequest {
    @ApiModelProperty(value = "支付单号", required = true)
    @NotBlank(message = "支付单号不能为空")
    private String payNo;

    @ApiModelProperty(value = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @ApiModelProperty(value = "支付状态（1-成功，0-失败）", required = true)
    @NotNull(message = "支付状态不能为空")
    private Integer payStatus;

    @ApiModelProperty(value = "支付时间（yyyy-MM-dd HH:mm:ss）", required = true)
    @NotBlank(message = "支付时间不能为空")
    private String payTime;

    @ApiModelProperty(value = "签名（模拟验证）", required = true)
    @NotBlank(message = "签名不能为空")
    private String sign;
}