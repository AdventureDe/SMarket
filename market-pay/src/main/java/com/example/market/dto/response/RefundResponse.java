package com.example.market.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 退款响应DTO
 */
@Data
@ApiModel(value = "退款响应结果")
public class RefundResponse {
    @ApiModelProperty(value = "支付ID")
    private Long payId;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "退款状态（1-退款中，2-已退款，3-退款失败）")
    private Integer refundStatus;

    @ApiModelProperty(value = "退款状态描述")
    private String refundStatusDesc;

    @ApiModelProperty(value = "退款备注")
    private String remark;
}