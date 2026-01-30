package com.example.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PayStatusEnum {
    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    PAY_FAILED(2, "支付失败"),
    REFUNDING(3, "退款中"),
    REFUNDED(4, "已退款"),
    REFUND_FAILED(5, "退款失败");

    private final Integer code;
    private final String msg;

    // 根据code获取枚举
    public static PayStatusEnum getByCode(Integer code) {
        for (PayStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}