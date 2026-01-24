package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("order_products")
public class OrderProduct {
    @TableId(type = IdType.AUTO)
    private Long id; // Go代码里没显式定义主键，但MyBatisPlus通常需要一个

    private Long orderId;
    private Long productId;
    private Integer num; // 对应 Go 的 Quantity/Num
}