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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync // 开启异步支持
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;
    // 自注入用于调用 @Async 方法
    @Autowired
    @Lazy
    private CartServiceImpl self;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<CartItemResponseDTO> getCartItems(Long userId) {
        String key = "cart:" + userId;
        long start = System.currentTimeMillis();

        // 1. 尝试从 Redis 获取 (逻辑不变)
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(key);
        if (!cartMap.isEmpty()) {
            log.info("Redis 命中，耗时: {}ms", System.currentTimeMillis() - start);
            return buildCartFromRedis(cartMap); // 假设你这个方法已经写好了
        }

        // 2. Redis 无数据，回源查询
        start = System.currentTimeMillis();

        // 2.1 查自己的数据库，拿到只有 ID 的购物车数据
        List<CartItem> cartItems = cartMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId));

        List<CartItemResponseDTO> results = new ArrayList<>();

        if (!cartItems.isEmpty()) {
            // 2.2 提取商品ID列表
            List<Long> productIds = cartItems.stream()
                    .map(CartItem::getProductId)
                    .collect(Collectors.toList());

            // 2.3 远程调用商品服务
            // 就像调用本地方法一样调用远程接口
            Result<List<Product>> remoteResult = productClient.getProductsByIds(productIds);

            // 2.4 安全检查：远程调用可能会失败，需要判空
            List<Product> products = null;
            if (remoteResult != null && remoteResult.getCode() == 200) {
                products = remoteResult.getData();
            } else {
                // 如果商品服务挂了，这里可以选择抛异常，或者返回空列表，看业务容忍度
                log.error("商品服务调用失败");
                products = new ArrayList<>();
            }

            // 2.5 转 Map 方便匹配 (逻辑不变)
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, p -> p));

            // 2.6 组装结果 (逻辑不变)
            for (CartItem item : cartItems) {
                Product p = productMap.get(item.getProductId());
                // 注意：如果商品被下架删除了，p 可能是 null，要处理
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

        // 3. 写入 Redis 缓存 (逻辑不变)
        // 注意：这里要把 results 存进去，不仅减轻了 DB 压力，也减轻了 Feign 网络调用的压力
        if (!results.isEmpty()) {
            cacheCartItems(userId, results);
        }

        return results;
    }
    /**
     * 异步方法：将DB结果缓存到Redis
     * Go: cacheCartItems
     */
    @Async
    public void cacheCartItems(Long userId, List<CartItemResponseDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        String key = "cart:" + userId;

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] keyBytes = key.getBytes();

            for (CartItemResponseDTO item : items) {
                byte[] field = String.valueOf(item.getProductId()).getBytes();
                byte[] value = String.valueOf(item.getQuantity()).getBytes();
                connection.hSet(keyBytes, field, value);
            }

            connection.expire(keyBytes, 24 * 60 * 60);
            return null;
        });
    }

    /**
     * 从 Redis 数据构建响应
     * Go: buildCartFromRedis
     */
    private List<CartItemResponseDTO> buildCartFromRedis(Map<Object, Object> cartMap) {
        List<Long> productIds = new ArrayList<>();
        for (Object k : cartMap.keySet()) {
            productIds.add(Long.valueOf(k.toString()));
        }

        if (productIds.isEmpty()) return new ArrayList<>();

        // 从数据库获取商品详情
        List<Product> products = productMapper.selectBatchIds(productIds);

        List<CartItemResponseDTO> results = new ArrayList<>();
        for (Product p : products) {
            String qtyStr = (String) cartMap.get(String.valueOf(p.getProductId()));
            Integer qty = qtyStr != null ? Integer.valueOf(qtyStr) : 0;

            CartItemResponseDTO dto = new CartItemResponseDTO();
            dto.setProductId(p.getProductId());
            dto.setProductName(p.getProductName());
            dto.setProductDescription(p.getProductDescription());
            dto.setPrice(p.getPrice());
            dto.setImageUrl(p.getImageUrl());
            dto.setQuantity(qty);
            // 注意：Redis缓存模式下，cart_id 可能拿不到，除非存在 Redis value 里，这里暂空
            results.add(dto);
        }
        return results;
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

        // 3. 更新数据库 (模拟 Go 的 ON DUPLICATE KEY UPDATE)
        // Java JPA/MyBatis 通常做法是先查后改
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

        // 4. 更新 Redis (原子操作 HIncrBy)
        String key = "cart:" + input.getUserId();
        redisTemplate.opsForHash().increment(key, String.valueOf(input.getProductId()), quantity);

        // 5. 设置过期时间
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItem(Long userId, Long productId) {
        if (userId == null || productId == null) return;

        // 先操作数据库
        cartMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));

        // 再操作 Redis
        String key = "cart:" + userId;
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) throw new RuntimeException("数量必须大于0");

        // 先更新数据库
        CartItem item = cartMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));

        if (item == null) {
            throw new RuntimeException("购物车中未找到该商品");
        }

        item.setQuantity(quantity);
        item.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(item);

        // 再更新 Redis (HSet)
        String key = "cart:" + userId;
        redisTemplate.opsForHash().put(key, String.valueOf(productId), String.valueOf(quantity));
    }
}