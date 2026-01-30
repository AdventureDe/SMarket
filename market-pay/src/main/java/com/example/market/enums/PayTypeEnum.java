package com.example.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PayTypeEnum {
    WECHAT_PAY(1, "微信支付"),
    ALIPAY(2, "支付宝支付"),
    UNION_PAY(3, "银联支付");

    private final Integer code;
    private final String name;

    public static PayTypeEnum getByCode(Integer code) {
        for (PayTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}