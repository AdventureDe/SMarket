package com.example.market.dto;

import lombok.Data;

@Data
public class CartAddDTO {
    private Long userId; // Go代码中是从Query获取，但这里定义全量字段，Controller负责组装
    private Long productId;
    private Integer quantity;
}