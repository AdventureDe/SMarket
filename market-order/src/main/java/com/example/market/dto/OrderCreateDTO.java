package com.example.market.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDTO {
    private Long userId;
    private BigDecimal totalPrice;
    private Long addressId;
    // 对应 Go 的 ProductIDs []uint
    private List<Long> productIds;
    // 对应 Go 的 ProductQuantities []uint
    private List<Integer> productQuantities;
}