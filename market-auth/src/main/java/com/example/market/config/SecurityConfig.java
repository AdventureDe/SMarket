package com.example.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; //用于Spring Boot 3.x关闭csrf

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * 作用：放行所有接口，禁用CSRF（防止跨域报错），模仿 Go 的直接暴露行为
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (微服务/API模式通常需要禁用)
                .csrf(AbstractHttpConfigurer::disable)
                // 允许所有 HTTP 请求 (之后可以改为只允许 /auth/**)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * 将 PasswordEncoder 注册为 Bean
     * 这样在 Service 中就可以使用 @Autowired 注入，而不是 new 对象了（可选优化）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}