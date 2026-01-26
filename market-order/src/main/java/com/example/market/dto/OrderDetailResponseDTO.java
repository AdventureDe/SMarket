package com.example.market.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailResponseDTO {
    // --- 订单基础信息 ---
    private Long orderId;
    private String status;        // 状态描述
    private Integer statusCode;   // 状态码（前端可能需要根据状态码判断显示什么按钮）
    private BigDecimal totalPrice;
    private LocalDateTime createAt;
    private LocalDateTime payTime;

    // --- 收货地址信息 (聚合展示) ---
    // 注意：不要只给 addressId，要给详细信息
    private String recipient;     // 收货人
    private String phone;         // 电话
    private String fullAddress;   // 拼接好的完整地址 (省+市+区+街道)

    // --- 商品列表 ---
    private List<OrderProductResponseDTO> products;
}