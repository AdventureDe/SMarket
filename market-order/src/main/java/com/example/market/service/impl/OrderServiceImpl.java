package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.dto.OrderCreateDTO;
import com.example.market.dto.OrderDetailResponseDTO;
import com.example.market.dto.OrderProductResponseDTO;
import com.example.market.dto.OrderResponseDTO;
import com.example.market.entity.Address;
import com.example.market.entity.Order;
import com.example.market.entity.OrderProduct;
import com.example.market.enums.OrderStatus; // 务必确保引入了枚举
import com.example.market.mapper.AddressMapper;
import com.example.market.mapper.OrderMapper;
import com.example.market.mapper.OrderProductMapper;
import com.example.market.service.CartService;
import com.example.market.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderProductMapper orderProductMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private CartService cartService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO createOrder(OrderCreateDTO input) {
        // 1. 验证输入
        if (input.getUserId() == null || input.getUserId() == 0) {
            throw new RuntimeException("用户未登录");
        }
        if (input.getTotalPrice() == null || input.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("无效的订单总价");
        }
        if (input.getProductIds() == null || input.getProductQuantities() == null ||
                input.getProductIds().size() != input.getProductQuantities().size()) {
            throw new RuntimeException("商品ID与数量不匹配");
        }

        // 2. 处理地址
        Long finalAddressId = input.getAddressId();
        if (finalAddressId == null || finalAddressId == 0) {
            Address defaultAddr = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, input.getUserId())
                    .eq(Address::getIsDefault, true));
            if (defaultAddr != null) {
                finalAddressId = defaultAddr.getAddressId();
            } else {
                throw new RuntimeException("请选择收货地址或设置默认地址");
            }
        }

        // 3. 创建订单
        Order newOrder = new Order();
        newOrder.setUserId(input.getUserId());
        newOrder.setTotalPrice(input.getTotalPrice());

        // 【修改】使用枚举设置初始状态
        newOrder.setStatus(OrderStatus.PENDING_PAYMENT);
        newOrder.setPaymentStatus("未付款");
        newOrder.setAddressId(finalAddressId);
        newOrder.setCreatedAt(LocalDateTime.now());
        newOrder.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(newOrder);

        List<OrderProductResponseDTO> productResponseList = new ArrayList<>();

        for (int i = 0; i < input.getProductIds().size(); i++) {
            Long pid = input.getProductIds().get(i);
            Integer num = input.getProductQuantities().get(i);

            // 4.1 插入订单项
            OrderProduct op = new OrderProduct();
            op.setOrderId(newOrder.getOrderId());
            op.setProductId(pid);
            op.setNum(num);
            orderProductMapper.insert(op);

            // 4.2 【核心新增】从购物车移除该商品
            // 调用你 CartService 里写好的 removeCartItem 方法
            // 这个方法不仅会删数据库，还会帮你删 Redis 缓存，保证一致性
            try {
                cartService.removeCartItem(input.getUserId(), pid);
            } catch (Exception e) {
                // 这里的异常处理策略取决于业务要求：
                // 策略A (推荐)：记录日志但不回滚订单。毕竟订单生成了更重要，购物车没删干净也就是多显示一次。
                System.err.println("移除购物车商品失败: " + pid + ", 原因: " + e.getMessage());

                // 策略B (严格)：throw e; 让事务回滚，订单创建失败。
            }

            // 4.3 构建返回数据
            OrderProductResponseDTO pResp = new OrderProductResponseDTO();
            pResp.setProductId(pid);
            pResp.setQuantity(num);
            productResponseList.add(pResp);
        }

        // 5. 构建响应
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(newOrder.getOrderId());
        response.setCreateAt(newOrder.getCreatedAt());
        response.setTotalPrice(newOrder.getTotalPrice());
        response.setAddressId(newOrder.getAddressId());

        // 返回状态的描述（如 "待支付"）或 code，视 DTO 定义而定
        response.setStatus(newOrder.getStatus().getDesc());
        response.setProducts(productResponseList);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        // 状态校验：只有待支付和已支付可以取消
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT &&
                order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("当前状态不可取消");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) return;

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("订单[{}]非待支付状态，忽略支付回调", orderId);
            return;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        order.setPaymentStatus("已付款");
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("只有已支付的订单才能发货");
        }

        order.setStatus(OrderStatus.SHIPPED);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作");

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("只有已发货的订单才能确认收货");
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderMapper.updateById(order);
    }

    @Override
    public List<OrderResponseDTO> getOrders(Long userId) {
        List<Map<String, Object>> rawRows = orderMapper.getOrdersWithProducts(userId);
        Map<Long, OrderResponseDTO> orderMap = new LinkedHashMap<>();

        for (Map<String, Object> row : rawRows) {
            // 【安全转换1】ID类
            Long orderId = parseLong(row.get("order_id"));

            OrderResponseDTO dto = orderMap.computeIfAbsent(orderId, k -> {
                OrderResponseDTO newDto = new OrderResponseDTO();
                newDto.setOrderId(orderId);

                // 【安全转换2】时间类
                Object createdAt = row.get("created_at");
                if (createdAt instanceof Timestamp) {
                    newDto.setCreateAt(((Timestamp) createdAt).toLocalDateTime());
                } else if (createdAt instanceof LocalDateTime) {
                    newDto.setCreateAt((LocalDateTime) createdAt);
                }

                // 【安全转换3】状态类
                // 数据库返回的是 int (0, 1, 2...)，需要转回 Enum 再获取描述
                Integer statusCode = parseInteger(row.get("status"));
                // 简单的遍历查找，也可以在 Enum 里写个 getByCode 方法
                String statusDesc = "未知状态";
                for (OrderStatus os : OrderStatus.values()) {
                    if (os.getCode().equals(statusCode)) {
                        statusDesc = os.getDesc();
                        break;
                    }
                }
                newDto.setStatus(statusDesc);

                // 【安全转换4】金额类
                newDto.setTotalPrice(parseBigDecimal(row.get("total_price")));
                newDto.setAddressId(parseLong(row.get("address_id")));
                newDto.setProducts(new ArrayList<>());
                return newDto;
            });

            OrderProductResponseDTO productDTO = new OrderProductResponseDTO();
            productDTO.setProductId(parseLong(row.get("product_id")));
            productDTO.setQuantity(parseInteger(row.get("quantity")));
            productDTO.setProductName((String) row.get("product_name"));
            productDTO.setImageUrl((String) row.get("image_url"));
            productDTO.setPrice(parseBigDecimal(row.get("price")));

            dto.getProducts().add(productDTO);
        }

        return new ArrayList<>(orderMap.values());
    }

    // --- 辅助转换方法 ---

    private Long parseLong(Object obj) {
        if (obj == null) return null;
        return Long.valueOf(obj.toString());
    }

    private Integer parseInteger(Object obj) {
        if (obj == null) return null;
        return Integer.valueOf(obj.toString()); // 更加宽容，可以处理 Long 转 Integer
    }

    private BigDecimal parseBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        return new BigDecimal(obj.toString());
    }

    @Override
    public OrderDetailResponseDTO getOrderDetail(Long userId, Long orderId) {
        // 1. 查询订单主表
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 权限校验 (防止用户通过改 URL ID 查看别人的订单)
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }

        // 3. 查询关联的商品信息 (使用之前的 Join 查询逻辑或者再次查表)
        // 这里为了简单，我们重用之前的 Mapper 逻辑或者直接查 OrderProduct 表
        // 假设我们复用 getOrdersWithProducts 的逻辑，但加上 orderId 过滤
        List<Map<String, Object>> productRows = orderMapper.getOrderDetails(orderId);
        // *注：需要在 Mapper XML 中写这个 SQL，下文会给

        // 4. 查询收货地址信息
        Address address = addressMapper.selectById(order.getAddressId());

        // 5. 组装 DTO
        OrderDetailResponseDTO dto = new OrderDetailResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getStatus().getDesc());
        dto.setStatusCode(order.getStatus().getCode());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setCreateAt(order.getCreatedAt());
        dto.setPayTime(order.getPayTime());

        // 组装地址 (如果地址被物理删除了，address 可能是 null)
        if (address != null) {
            dto.setRecipient(address.getRecipient());
            dto.setPhone(address.getPhone());
            dto.setFullAddress(String.format("%s %s %s %s %s",
                    address.getCountry(), address.getProvince(), address.getCity(),
                    address.getDistrict(), address.getStreet()));
        } else {
            dto.setFullAddress("该地址已失效");
        }

        // 组装商品列表
        List<OrderProductResponseDTO> productList = new ArrayList<>();
        for (Map<String, Object> row : productRows) {
            OrderProductResponseDTO p = new OrderProductResponseDTO();
            p.setProductId(parseLong(row.get("product_id")));
            p.setProductName((String) row.get("product_name"));
            p.setImageUrl((String) row.get("image_url"));
            p.setPrice(parseBigDecimal(row.get("price")));
            p.setQuantity(parseInteger(row.get("quantity")));
            productList.add(p);
        }
        dto.setProducts(productList);

        return dto;
    }
}