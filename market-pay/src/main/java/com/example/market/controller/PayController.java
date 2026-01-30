package com.example.market.controller;

import com.example.market.common.Result;
import com.example.market.dto.request.PayCallbackRequest;
import com.example.market.dto.request.PayCreateRequest;
import com.example.market.dto.request.RefundRequest;
import com.example.market.dto.response.PayCreateResponse;
import com.example.market.dto.response.PayQueryResponse;
import com.example.market.dto.response.RefundResponse;
import com.example.market.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.security.Principal;

/**
 * 支付模块接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/pay")
@Api(tags = "支付模块接口")
public class PayController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付（需鉴权）
     */
    @PostMapping("/create")
    @ApiOperation(value = "创建支付", notes = "生成支付记录，返回模拟支付链接")
    @PreAuthorize("isAuthenticated()") // 需登录
    public Result<PayCreateResponse> createPay(@Valid @RequestBody PayCreateRequest request, Principal principal) {
        // Principal 获取当前登录用户ID（实际项目中需结合Spring Security+JWT解析）
        Long userId = Long.parseLong(principal.getName());
        PayCreateResponse response = paymentService.createPay(request, userId);
        return Result.success(response);
    }

    /**
     * 支付回调（无需鉴权，第三方调用）
     */
    @PostMapping("/callback")
    @ApiOperation(value = "支付回调", notes = "第三方支付平台回调接口，更新支付状态")
    public Result<Boolean> payCallback(@Valid @RequestBody PayCallbackRequest request) {
        Boolean result = paymentService.handlePayCallback(request);
        return Result.success(result);
    }

    /**
     * 申请退款（需鉴权）
     */
    @PostMapping("/refund")
    @ApiOperation(value = "申请退款", notes = "已支付订单申请退款")
    @PreAuthorize("isAuthenticated()") // 需登录
    public Result<RefundResponse> applyRefund(@Valid @RequestBody RefundRequest request, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        RefundResponse response = paymentService.applyRefund(request, userId);
        return Result.success(response);
    }

    /**
     * 查询支付状态（需鉴权）
     */
    @GetMapping("/{orderId}")
    @ApiOperation(value = "查询支付状态", notes = "根据订单ID查询支付及退款状态")
    @PreAuthorize("isAuthenticated()") // 需登录
    public Result<PayQueryResponse> queryPayStatus(@PathVariable Long orderId, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        PayQueryResponse response = paymentService.queryPayByOrderId(orderId, userId);
        return Result.success(response);
    }
}