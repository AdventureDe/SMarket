package com.example.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.market.common.Result;
import com.example.market.dto.request.PayCallbackRequest;
import com.example.market.dto.request.PayCreateRequest;
import com.example.market.dto.request.RefundRequest;
import com.example.market.dto.response.PayCreateResponse;
import com.example.market.dto.response.PayQueryResponse;
import com.example.market.dto.response.RefundResponse;
import com.example.market.entity.Payment;
import com.example.market.enums.PayStatusEnum;
import com.example.market.enums.PayTypeEnum;
import com.example.market.exception.PayErrorCodeEnum;
import com.example.market.exception.PayException;
import com.example.market.feign.OrderFeignClient;
import com.example.market.mapper.PaymentMapper;
import com.example.market.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 支付服务实现类
 */
@Slf4j
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 模拟支付链接前缀
     */
    private static final String PAY_URL_PREFIX = "https://mock-pay.example.com/pay?no=";

    /**
     * 签名密钥（模拟）
     */
    private static final String SIGN_SECRET = "market-pay-secret-2026";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateResponse createPay(PayCreateRequest request, Long userId) {
        log.info("创建支付请求：userId={}, request={}", userId, request);

        // 1. 调用订单服务验证订单状态（必须为待支付）
        Result<Boolean> orderValidateResult = orderFeignClient.validateOrderStatus(request.getOrderId(), userId);
        if (!orderValidateResult.isSuccess() || !orderValidateResult.getData()) {
            log.error("订单状态验证失败：orderId={}, userId={}", request.getOrderId(), userId);
            throw new PayException(PayErrorCodeEnum.ORDER_STATUS_ERROR);
        }

        // 2. 生成支付单号（UUID简化）
        String payNo = "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 3. 构建支付实体
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setPayType(request.getPayType());
        payment.setPayStatus(PayStatusEnum.UNPAID.getCode()); // 待支付
        payment.setPayNo(payNo);
        payment.setCallbackUrl(request.getCallbackUrl());
        payment.setCallbackStatus(0); // 未回调
        payment.setRefundStatus(0); // 未退款
        payment.setRemark(request.getRemark());
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        // 4. 保存支付记录
        boolean saveSuccess = save(payment);
        if (!saveSuccess) {
            log.error("创建支付记录失败：payment={}", payment);
            throw new PayException(PayErrorCodeEnum.PAY_CREATE_FAILED);
        }

        // 5. 构建响应结果（模拟支付链接）
        PayCreateResponse response = new PayCreateResponse();
        response.setPayId(payment.getId());
        response.setPayNo(payNo);
        response.setPayUrl(PAY_URL_PREFIX + payNo);
        response.setPayStatus(PayStatusEnum.UNPAID.getCode());
        response.setPayStatusDesc(PayStatusEnum.UNPAID.getMsg());

        log.info("创建支付成功：payId={}, payNo={}", payment.getId(), payNo);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handlePayCallback(PayCallbackRequest request) {
        log.info("处理支付回调请求：request={}", request);

        // 1. 验证签名
        Boolean signValid = verifyCallbackSign(request);
        if (!signValid) {
            log.error("回调签名验证失败：request={}", request);
            throw new PayException(PayErrorCodeEnum.CALLBACK_SIGN_ERROR);
        }

        // 2. 查询支付记录
        Payment payment = paymentMapper.selectByPayNo(request.getPayNo());
        if (payment == null) {
            log.error("支付记录不存在：payNo={}", request.getPayNo());
            throw new PayException(PayErrorCodeEnum.PAY_NOT_FOUND);
        }

        // 3. 验证支付状态（只能处理待支付状态）
        if (!PayStatusEnum.UNPAID.getCode().equals(payment.getPayStatus())) {
            log.error("支付状态不允许回调：payId={}, currentStatus={}", payment.getId(), payment.getPayStatus());
            throw new PayException(PayErrorCodeEnum.PAY_STATUS_ERROR);
        }

        // 4. 更新支付记录状态
        payment.setPayStatus(request.getPayStatus() == 1 ? PayStatusEnum.PAID.getCode() : PayStatusEnum.PAY_FAILED.getCode());
        payment.setCallbackStatus(1); // 已回调
        payment.setCallbackTime(LocalDateTime.now());
        if (request.getPayStatus() == 1) {
            // 支付成功：更新支付时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            payment.setPayTime(LocalDateTime.parse(request.getPayTime(), formatter));
            payment.setRemark("支付成功");
        } else {
            payment.setRemark("支付失败：第三方回调标记失败");
        }
        payment.setUpdateTime(LocalDateTime.now());

        boolean updateSuccess = updateById(payment);
        if (!updateSuccess) {
            log.error("回调更新支付记录失败：payment={}", payment);
            throw new PayException(PayErrorCodeEnum.CALLBACK_HANDLER_FAILED);
        }

        // 5. 支付成功时，调用订单服务更新订单状态
        if (request.getPayStatus() == 1) {
            Result<Boolean> orderUpdateResult = orderFeignClient.updateOrderPayStatus(payment.getOrderId(), payment.getPayNo());
            if (!orderUpdateResult.isSuccess() || !orderUpdateResult.getData()) {
                log.error("调用订单服务更新支付状态失败：orderId={}, payNo={}", payment.getOrderId(), payment.getPayNo());
                // 此处可添加补偿机制（如消息队列重试）
                throw new PayException(PayErrorCodeEnum.CALLBACK_HANDLER_FAILED);
            }
        }

        log.info("支付回调处理成功：payId={}, payStatus={}", payment.getId(), payment.getPayStatus());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResponse applyRefund(RefundRequest request, Long userId) {
        log.info("申请退款请求：userId={}, request={}", userId, request);

        // 1. 查询支付记录
        Payment payment = getById(request.getPayId());
        if (payment == null) {
            log.error("支付记录不存在：payId={}", request.getPayId());
            throw new PayException(PayErrorCodeEnum.PAY_NOT_FOUND);
        }

        // 2. 验证权限（只能退自己的订单）
        if (!payment.getUserId().equals(userId)) {
            log.error("无退款权限：userId={}, payUserId={}", userId, payment.getUserId());
            throw new PayException(PayErrorCodeEnum.REFUND_FAILED);
        }

        // 3. 验证支付状态（必须已支付）
        if (!PayStatusEnum.PAID.getCode().equals(payment.getPayStatus())) {
            log.error("支付状态不允许退款：payId={}, payStatus={}", payment.getId(), payment.getPayStatus());
            throw new PayException(PayErrorCodeEnum.REFUND_STATUS_ERROR);
        }

        // 4. 验证退款状态（未退款才能申请）
        if (payment.getRefundStatus() != 0) {
            log.error("退款状态不允许重复申请：payId={}, refundStatus={}", payment.getId(), payment.getRefundStatus());
            throw new PayException(PayErrorCodeEnum.REFUND_STATUS_ERROR);
        }

        // 5. 验证退款金额（不能超过支付金额）
        if (request.getRefundAmount().compareTo(payment.getAmount()) > 0) {
            log.error("退款金额超出支付金额：payAmount={}, refundAmount={}", payment.getAmount(), request.getRefundAmount());
            throw new PayException(PayErrorCodeEnum.REFUND_FAILED);
        }

        // 6. 更新支付记录退款状态（模拟退款流程，实际需调用第三方支付平台退款接口）
        payment.setRefundStatus(PayStatusEnum.REFUNDING.getCode()); // 退款中
        payment.setRefundTime(LocalDateTime.now());
        payment.setRemark(request.getRefundReason());
        payment.setUpdateTime(LocalDateTime.now());

        boolean updateSuccess = updateById(payment);
        if (!updateSuccess) {
            log.error("更新退款状态失败：payment={}", payment);
            throw new PayException(PayErrorCodeEnum.REFUND_FAILED);
        }

        // 7. 调用订单服务更新订单退款状态
        Result<Boolean> orderUpdateResult = orderFeignClient.updateOrderRefundStatus(payment.getOrderId(), PayStatusEnum.REFUNDING.getCode());
        if (!orderUpdateResult.isSuccess() || !orderUpdateResult.getData()) {
            log.error("调用订单服务更新退款状态失败：orderId={}", payment.getOrderId());
            throw new PayException(PayErrorCodeEnum.REFUND_FAILED);
        }

        // 8. 模拟退款成功（实际场景需等待第三方支付平台回调）
        payment.setRefundStatus(PayStatusEnum.REFUNDED.getCode()); // 已退款
        updateById(payment);
        orderFeignClient.updateOrderRefundStatus(payment.getOrderId(), PayStatusEnum.REFUNDED.getCode());

        // 9. 构建响应结果
        RefundResponse response = new RefundResponse();
        response.setPayId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setRefundStatus(PayStatusEnum.REFUNDED.getCode());
        response.setRefundStatusDesc(PayStatusEnum.REFUNDED.getMsg());
        response.setRemark(request.getRefundReason());

        log.info("退款申请成功：payId={}, orderId={}", payment.getId(), payment.getOrderId());
        return response;
    }

    @Override
    public PayQueryResponse queryPayByOrderId(Long orderId, Long userId) {
        log.info("查询支付状态：orderId={}, userId={}", orderId, userId);

        // 1. 查询支付记录
        Payment payment = paymentMapper.selectByOrderId(orderId);
        if (payment == null) {
            log.error("支付记录不存在：orderId={}", orderId);
            throw new PayException(PayErrorCodeEnum.PAY_NOT_FOUND);
        }

        // 2. 验证权限（只能查询自己的订单）
        if (!payment.getUserId().equals(userId)) {
            log.error("无查询权限：userId={}, payUserId={}", userId, payment.getUserId());
            throw new PayException(PayErrorCodeEnum.PAY_NOT_FOUND);
        }

        // 3. 构建响应结果
        PayQueryResponse response = new PayQueryResponse();
        BeanUtils.copyProperties(payment, response);
        // 补充枚举描述
        response.setPayType(PayTypeEnum.getByCode(payment.getPayType()) != null ? PayTypeEnum.getByCode(payment.getPayType()).getName() : "未知");
        response.setPayStatusDesc(PayStatusEnum.getByCode(payment.getPayStatus()) != null ? PayStatusEnum.getByCode(payment.getPayStatus()).getMsg() : "未知");
        response.setRefundStatusDesc(PayStatusEnum.getByCode(payment.getRefundStatus()) != null ? PayStatusEnum.getByCode(payment.getRefundStatus()).getMsg() : "未知");

        log.info("查询支付状态成功：orderId={}, payStatus={}", orderId, payment.getPayStatus());
        return response;
    }

    @Override
    public Boolean verifyCallbackSign(PayCallbackRequest request) {
        // 模拟签名验证：sign = MD5(payNo + orderId + payStatus + secret)
        String signSource = request.getPayNo() + request.getOrderId() + request.getPayStatus() + SIGN_SECRET;
        String calculatedSign = org.springframework.util.DigestUtils.md5DigestAsHex(signSource.getBytes());
        return calculatedSign.equals(request.getSign());
    }
}