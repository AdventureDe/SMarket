package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表
 * 对应数据库表：payments
 */
@Data
@TableName("payments")
public class Payment {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID（关联订单表orders.id）
     */
    private Long orderId;

    /**
     * 用户ID（关联用户表users.id）
     */
    private Long userId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付方式（1-微信支付，2-支付宝支付，3-银联支付）
     * 对应PayTypeEnum
     */
    private Integer payType;

    /**
     * 支付状态（0-待支付，1-已支付，2-支付失败，3-退款中，4-已退款，5-退款失败）
     * 对应PayStatusEnum
     */
    private Integer payStatus;

    /**
     * 支付单号（第三方支付平台单号，模拟生成）
     */
    private String payNo;

    /**
     * 回调地址
     */
    private String callbackUrl;

    /**
     * 回调状态（0-未回调，1-已回调）
     */
    private Integer callbackStatus;

    /**
     * 退款状态（0-未退款，1-退款中，2-已退款，3-退款失败）
     */
    private Integer refundStatus;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 备注（支付失败原因、退款原因等）
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}