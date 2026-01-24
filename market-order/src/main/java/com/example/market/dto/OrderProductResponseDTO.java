package com.example.market.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderProductResponseDTO {
    private Long productId;
    private String productName;
    private BigDecimal price; // 对应 Go float32
    private String imageUrl;
    private Integer quantity;
}