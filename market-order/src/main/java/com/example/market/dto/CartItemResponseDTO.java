package com.example.market.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemResponseDTO {
    private Long cartId;
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal price; // Go是string，Java计算用BigDecimal，展示可转String
    private Integer quantity;
    private String imageUrl;
}