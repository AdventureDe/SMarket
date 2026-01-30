package com.example.market.exception;

import lombok.Getter;

/**
 * 支付模块自定义异常
 */
@Getter
public class PayException extends RuntimeException {
    private final Integer code;
    private final String msg;

    public PayException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public PayException(PayErrorCodeEnum errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }
}