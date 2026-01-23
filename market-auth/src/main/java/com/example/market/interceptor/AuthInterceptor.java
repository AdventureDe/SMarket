package com.example.market.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 获取 Token
        String token = request.getHeader("Authorization");

        // === 【调试代码】
        System.out.println("========== 拦截器调试 ==========");
        System.out.println("1. 原始 Header: " + token);

        if (!StringUtils.hasText(token)) {
            response.setStatus(401);
            return false;
        }

        // 2. 如果前端传了 "Bearer "，必须把它切掉
        if (token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉前7个字符
        }

        // === 【调试代码】打印处理后的 Token ===
        System.out.println("2. 处理后 Token: " + token);

        // 3. 拼装 Key
        String key = "login:token:" + token;

        // === 【调试代码】打印最终去 Redis 查的 Key ===
        System.out.println("3. 最终 Redis Key: " + key);

        // 4. 查 Redis
        String userInfo = redisTemplate.opsForValue().get(key);

        // === 【调试代码】打印查到了什么 ===
        System.out.println("4. Redis 查询结果: " + userInfo);
        System.out.println("===============================");

        if (!StringUtils.hasText(userInfo)) {
            // 查不到，说明 Token 无效
            response.setStatus(401);
            return false;
        }

        // 续期逻辑...
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
        return true;
    }
}
