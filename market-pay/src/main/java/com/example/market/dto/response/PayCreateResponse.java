package com.example.market.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 创建支付响应DTO
 */
@Data
@ApiModel(value = "创建支付响应结果")
public class PayCreateResponse {
    @ApiModelProperty(value = "支付ID")
    private Long payId;

    @ApiModelProperty(value = "支付单号")
    private String payNo;

    @ApiModelProperty(value = "支付链接（模拟）")
    private String payUrl;

    @ApiModelProperty(value = "支付状态（0-待支付）")
    private Integer payStatus;

    @ApiModelProperty(value = "支付状态描述")
    private String payStatusDesc;
}