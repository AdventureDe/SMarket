package com.example.market.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatus {
    // 状态码, 描述
    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    @EnumValue // MyBatis-Plus 存入数据库的值 (存 int)
    private final Integer code;

    @JsonValue // 前端接收到的中文描述 (或者你也可以返回 code 让前端转义)
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}