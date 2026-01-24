package com.example.market.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductAddDTO {
    private Long userId;          // 对应 Go: UserID
    private Long categoryId;
    private String productName;   // 对应 Go: Name
    private String productDescription; // 对应 Go: Description
    private String origin;        // 对应 Go: Origin

    // Go 中 Price 是 string，Java 中为了计算通常转为 BigDecimal
    // Jackson 会自动把前端传来的字符串 "12.5" 转为 BigDecimal
    private BigDecimal price;

    private String salesPeriod;   // 对应 Go: SalesPeriod
    private String imageUrl;      // 对应 Go: ImageURL

    // 新增字段 (对应 Go 的 struct tag)
    private Boolean isActive;     // 对应 Go: IsActive
    private Boolean isViolation;  // 对应 Go: IsViolation
}