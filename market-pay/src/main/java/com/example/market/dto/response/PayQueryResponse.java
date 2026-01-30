package com.example.market.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付查询响应DTO
 */
@Data
@ApiModel(value = "支付查询响应结果")
public class PayQueryResponse {
    @ApiModelProperty(value = "支付ID")
    private Long payId;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "支付状态")
    private Integer payStatus;

    @ApiModelProperty(value = "支付状态描述")
    private String payStatusDesc;

    @ApiModelProperty(value = "支付单号")
    private String payNo;

    @ApiModelProperty(value = "支付时间")
    private LocalDateTime payTime;

    @ApiModelProperty(value = "退款状态")
    private Integer refundStatus;

    @ApiModelProperty(value = "退款状态描述")
    private String refundStatusDesc;
}