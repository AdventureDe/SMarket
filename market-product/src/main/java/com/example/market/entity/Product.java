package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long productId;

    private Long userId;            // 卖家ID
    private Long categoryId;
    private String productName;     // 商品名称
    private String productDescription; // 描述
    private String origin;          // 发货地/来源
    private BigDecimal price;       // 价格 (Go中可能是float/string，Java涉及金额必须用BigDecimal)
    private String salesPeriod;     // 售卖时间段
    private String imageUrl;        // 图片链接
    private Boolean isActive;       // 是否上架
    private Boolean isViolation;    // 是否违规
    private LocalDateTime publishDate; // 发布时间

    // 这个字段数据库里没有，但我们希望查询的时候带出来
    @TableField(exist = false)
    private String categoryName;
}