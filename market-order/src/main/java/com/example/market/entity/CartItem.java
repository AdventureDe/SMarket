package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cart_items") // 假设数据库表名
public class CartItem {
    @TableId(type = IdType.AUTO)
    private Long cartId;

    private Long userId;      // 用户ID
    private Long productId;   // 商品ID
    private Integer quantity; // 数量

    // 如果需要展示商品详情，通常这里不存，而是查询时联表或远程调用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}