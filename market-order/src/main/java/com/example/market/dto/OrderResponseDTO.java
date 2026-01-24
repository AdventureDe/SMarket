package com.example.market.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long orderId;
    private LocalDateTime createAt;
    private BigDecimal totalPrice;
    private Long addressId;
    private String status;
    private List<OrderProductResponseDTO> products;
}