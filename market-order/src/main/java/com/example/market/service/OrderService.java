package com.example.market.service;

import com.example.market.dto.OrderCreateDTO;
import com.example.market.dto.OrderResponseDTO;
import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderCreateDTO input);
    List<OrderResponseDTO> getOrders(Long userId);
    // 支付成功 (待支付 -> 已支付)
    void paySuccess(Long orderId);

    // 商家发货 (已支付 -> 已发货)
    void shipOrder(Long orderId);

    // 确认收货 (已发货 -> 已完成)
    void completeOrder(Long userId, Long orderId);

    // 取消订单 (待支付/已支付 -> 已取消)
    void cancelOrder(Long userId, Long orderId); // 加上 userId 防止越权
}