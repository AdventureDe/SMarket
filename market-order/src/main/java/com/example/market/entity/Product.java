package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("products") // 确保这里的表名和你数据库实际表名一致(Go代码里好像是 special_products，请核对)
public class Product {
    @TableId
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal price;
    private String imageUrl;
}