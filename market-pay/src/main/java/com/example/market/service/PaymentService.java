package com.example.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.market.dto.request.PayCallbackRequest;
import com.example.market.dto.request.PayCreateRequest;
import com.example.market.dto.request.RefundRequest;
import com.example.market.dto.response.PayCreateResponse;
import com.example.market.dto.response.PayQueryResponse;
import com.example.market.dto.response.RefundResponse;
import com.example.market.entity.Payment;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<Payment> {
    /**
     * 创建支付（生成支付记录，模拟支付链接）
     * @param request 创建支付请求
     * @param userId 当前登录用户ID
     * @return 支付创建结果
     */
    PayCreateResponse createPay(PayCreateRequest request, Long userId);

    /**
     * 处理支付回调
     * @param request 回调请求参数
     * @return 回调处理结果（成功/失败）
     */
    Boolean handlePayCallback(PayCallbackRequest request);

    /**
     * 申请退款
     * @param request 退款请求参数
     * @param userId 当前登录用户ID
     * @return 退款结果
     */
    RefundResponse applyRefund(RefundRequest request, Long userId);

    /**
     * 根据订单ID查询支付状态
     * @param orderId 订单ID
     * @param userId 当前登录用户ID
     * @return 支付查询结果
     */
    PayQueryResponse queryPayByOrderId(Long orderId, Long userId);

    /**
     * 验证回调签名（模拟）
     * @param request 回调请求参数
     * @return 签名是否有效
     */
    Boolean verifyCallbackSign(PayCallbackRequest request);
}