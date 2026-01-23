package com.example.market.config;

import com.example.market.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(   // 放行以下路径（不需要登录就能访问的）
                        "/auth/login",
                        "/auth/register",
                        "/doc.html",    // Swagger/Knife4j 文档
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs"
                );
    }
}