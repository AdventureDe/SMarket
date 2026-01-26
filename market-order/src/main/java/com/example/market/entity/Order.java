package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.market.enums.OrderStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private Long userId;
    private BigDecimal totalPrice;
    private OrderStatus status;       // 待付款, 已取消, etc.
    private String paymentStatus; // 未付款, 已付款
    private LocalDateTime payTime;
    private Long addressId;

    // MyBatis-Plus 默认自动填充创建时间，或者数据库设置默认值
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}