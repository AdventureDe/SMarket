package com.example.market.controller;

import com.example.market.common.Result;
import com.example.market.dto.OrderCreateDTO;
import com.example.market.dto.OrderDetailResponseDTO;
import com.example.market.dto.OrderResponseDTO;
import com.example.market.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result<OrderResponseDTO> createOrder(@RequestHeader("user_id") Long userId,
                                                @RequestBody OrderCreateDTO input) {
        // 必须将 Header 里的 userId 注入到 DTO 中，因为 Service 做了校验
        input.setUserId(userId);

        OrderResponseDTO order = orderService.createOrder(input);
        return Result.success(order);
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<List<OrderResponseDTO>> getOrders(@RequestHeader("user_id") Long userId) {
        List<OrderResponseDTO> list = orderService.getOrders(userId);
        return Result.success(list);
    }

    /**
     * 取消订单 (用户操作)
     */
    @PostMapping("/cancel/{orderId}")
    public Result<String> cancelOrder(@RequestHeader("user_id") Long userId,
                                      @PathVariable Long orderId) {
        orderService.cancelOrder(userId, orderId);
        return Result.success("订单已取消");
    }

    /**
     * 确认收货 (用户操作)
     */
    @PostMapping("/confirm/{orderId}")
    public Result<String> confirmOrder(@RequestHeader("user_id") Long userId,
                                       @PathVariable Long orderId) {
        orderService.completeOrder(userId, orderId);
        return Result.success("确认收货成功");
    }

    // --- 以下接口通常用于模拟测试，因为实际支付和发货是回调或后台操作 ---

    /**
     * 模拟支付成功 (测试用)
     */
    @PostMapping("/test/pay/{orderId}")
    public Result<String> testPay(@PathVariable Long orderId) {
        orderService.paySuccess(orderId);
        return Result.success("支付成功");
    }

    /**
     * 模拟发货 (测试用，或者后台管理系统调用)
     */
    @PostMapping("/test/ship/{orderId}")
    public Result<String> testShip(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Result.success("发货成功");
    }

    /**
     * 获取订单详情
     * GET /order/detail/{orderId}
     */
    @GetMapping("/detail/{orderId}")
    public Result<OrderDetailResponseDTO> getOrderDetail(@RequestHeader("user_id") Long userId,
                                                         @PathVariable Long orderId) {
        OrderDetailResponseDTO detail = orderService.getOrderDetail(userId, orderId);
        return Result.success(detail);
    }
}