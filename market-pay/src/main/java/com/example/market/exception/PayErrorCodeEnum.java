package com.example.market.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付模块错误码
 */
@Getter
@AllArgsConstructor
public enum PayErrorCodeEnum {
    ORDER_NOT_FOUND(40001, "订单不存在"),
    ORDER_STATUS_ERROR(40002, "订单状态不允许支付"),
    PAY_CREATE_FAILED(40003, "创建支付失败"),
    PAY_NOT_FOUND(40004, "支付记录不存在"),
    PAY_STATUS_ERROR(40005, "支付状态不允许操作"),
    CALLBACK_SIGN_ERROR(40006, "回调签名验证失败"),
    CALLBACK_HANDLER_FAILED(40007, "回调处理失败"),
    REFUND_FAILED(40008, "退款失败"),
    REFUND_STATUS_ERROR(40009, "退款状态不允许操作");

    private final Integer code;
    private final String msg;
}