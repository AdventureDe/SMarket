package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.client.ProductClient;
import com.example.market.common.Result;
import com.example.market.dto.CartAddDTO;
import com.example.market.dto.CartItemResponseDTO;
import com.example.market.entity.CartItem;
import com.example.market.entity.Product;
import com.example.market.mapper.CartMapper;
import com.example.market.mapper.ProductMapper;
import com.example.market.service.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductClient productClient;

    // 1. 统一使用 StringRedisTemplate，避免序列化问题
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 2. 自注入，用于在类内部调用 @Async 方法使其生效
    @Autowired
    @Lazy
    private CartServiceImpl self;

    @Override
    public List<CartItemResponseDTO> getCartItems(Long userId) {
        String key = "cart:" + userId;
        long start = System.currentTimeMillis();

        // 3. 尝试从 Redis 获取
        Map<Object, Object> rawMap = stringRedisTemplate.opsForHash().entries(key);

        // 【关键】只有 Redis 有数据时才返回
        // 因为我们在写入时采用了“全量删除”策略，所以只要 Key 存在，数据一定是完整的
        if (!rawMap.isEmpty()) {
            log.info("Redis 命中");
            return buildCartFromRedis(rawMap);
        }

        // 4. Redis 无数据，回源查询
        start = System.currentTimeMillis();

        // 4.1 查数据库
        List<CartItem> cartItems = cartMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId));

        List<CartItemResponseDTO> results = new ArrayList<>();

        if (!cartItems.isEmpty()) {
            // 4.2 提取商品ID列表
            List<Long> productIds = cartItems.stream()
                    .map(CartItem::getProductId)
                    .collect(Collectors.toList());

            // 4.3 远程调用商品服务
            Result<List<Product>> remoteResult = productClient.getProductsByIds(productIds);

            // 4.4 安全检查
            List<Product> products = null;
            if (remoteResult != null && remoteResult.getCode() == 200) {
                products = remoteResult.getData();
            } else {
                log.error("商品服务调用失败");
                products = new ArrayList<>();
            }

            // 4.5 转 Map
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, p -> p));

            // 4.6 组装结果
            for (CartItem item : cartItems) {
                Product p = productMap.get(item.getProductId());
                if (p != null) {
                    CartItemResponseDTO dto = new CartItemResponseDTO();
                    dto.setCartId(item.getCartId());
                    dto.setProductId(p.getProductId());
                    dto.setProductName(p.getProductName());
                    dto.setProductDescription(p.getProductDescription());
                    dto.setPrice(p.getPrice());
                    dto.setImageUrl(p.getImageUrl());
                    dto.setQuantity(item.getQuantity());
                    results.add(dto);
                }
            }
        }

        log.info("回源查询耗时: {}ms", System.currentTimeMillis() - start);

        // 5. 写入 Redis 缓存 (异步)
        if (!results.isEmpty()) {
            self.cacheCartItems(userId, results);
        }

        return results;
    }

    /**
     * 异步方法：将DB结果缓存到Redis
     */
    @Async
    public void cacheCartItems(Long userId, List<CartItemResponseDTO> items) {
        if (items == null || items.isEmpty()) return;
        String key = "cart:" + userId;

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> dataToCache = new HashMap<>();

        for (CartItemResponseDTO item : items) {
            try {
                String field = String.valueOf(item.getProductId());
                // 将对象转为 JSON 字符串存储
                String value = mapper.writeValueAsString(item);
                dataToCache.put(field, value);
            } catch (JsonProcessingException e) {
                log.error("序列化失败", e);
            }
        }

        if (!dataToCache.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(key, dataToCache);
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }

    private List<CartItemResponseDTO> buildCartFromRedis(Map<Object, Object> cartMap) {
        List<CartItemResponseDTO> list = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Map.Entry<Object, Object> entry : cartMap.entrySet()) {
            String jsonStr = (String) entry.getValue();

            // 简单校验是否为 JSON 格式
            if (!StringUtils.hasText(jsonStr) || !jsonStr.trim().startsWith("{")) {
                continue;
            }

            try {
                CartItemResponseDTO dto = mapper.readValue(jsonStr, CartItemResponseDTO.class);
                list.add(dto);
            } catch (Exception e) {
                log.error("JSON转对象失败: {}", jsonStr, e);
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(CartAddDTO input) {
        // 1. 参数验证
        if (input.getUserId() == null || input.getProductId() == null) {
            throw new RuntimeException("invalid user/product ID");
        }
        Integer quantity = (input.getQuantity() == null || input.getQuantity() <= 0) ? 1 : input.getQuantity();

        // 2. 检查商品是否存在
        Product product = productMapper.selectById(input.getProductId());
        if (product == null) {
            throw new RuntimeException("product not exist");
        }

        // 3. 更新数据库
        CartItem existingItem = cartMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, input.getUserId())
                .eq(CartItem::getProductId, input.getProductId()));

        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existingItem);
        } else {
            // 插入新条目
            CartItem newItem = new CartItem();
            newItem.setUserId(input.getUserId());
            newItem.setProductId(input.getProductId());
            newItem.setQuantity(quantity);
            newItem.setCreateTime(LocalDateTime.now());
            newItem.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(newItem);
        }

        // 4. 【核心修复】Redis 缓存失效策略
        // 直接删除该用户的整个购物车 Key，强制下次读取时从 DB 加载最新全量数据
        // 解决了 "添加新商品后，Redis 只有旧数据，导致显示不全" 的问题
        String key = "cart:" + input.getUserId();
        stringRedisTemplate.delete(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItem(Long userId, Long productId) {
        if (userId == null || productId == null) return;

        // 1. 操作数据库
        cartMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));

        // 2. 操作 Redis：直接删除 Key
        String key = "cart:" + userId;
        stringRedisTemplate.delete(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) throw new RuntimeException("数量必须大于0");

        // 1. 更新数据库
        CartItem item = cartMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));

        if (item == null) {
            throw new RuntimeException("购物车中未找到该商品");
        }

        item.setQuantity(quantity);
        item.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(item);

        // 2. 更新 Redis：直接删除 Key
        String key = "cart:" + userId;
        stringRedisTemplate.delete(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItemsBatch(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        // 1. 批量删 DB
        cartMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getProductId, productIds)); // 使用 IN 查询

        // 2. 删 Redis Key (直接删整个购物车 Key 最省事)
        stringRedisTemplate.delete("cart:" + userId);
    }
}