package com.example.market.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductUpdateDTO {
    private Long productId;

    private String productName;     // 商品名称
    private Long categoryId;
    private String productDescription; // 描述
    private String origin;          // 发货地/来源
    private BigDecimal price;       // 价格
    private String salesPeriod;     // 售卖时间段
    private String imageUrl;        // 图片链接
}