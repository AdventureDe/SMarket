package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.dto.LoginResponseDTO;
import com.example.market.dto.UserLoginDTO;
import com.example.market.dto.UserRegisterDTO;
import com.example.market.entity.User;
import com.example.market.mapper.UserMapper;
import com.example.market.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Spring自带的字符串工具

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    // 使用 Spring Security 提供的加密工具
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String registerUser(UserRegisterDTO request) {
        // 1. 默认角色处理
        // Go: if role == 0 { role = 2 }
        if (request.getRole() == null || request.getRole() == 0) {
            request.setRole(2);
        }

        // 2. 唯一性校验：用户名
        // Go: s.DB.Model(...).Where("username = ?", username).Count(&count)
        Long usernameCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (usernameCount > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 3. 唯一性校验：邮箱 (注意判空)
        if (StringUtils.hasText(request.getEmail())) {
            Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getEmail()));
            if (emailCount > 0) {
                throw new RuntimeException("邮箱已存在");
            }
        }

        // 4. 唯一性校验：手机号
        if (StringUtils.hasText(request.getPhone())) {
            Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, request.getPhone()));
            if (phoneCount > 0) {
                throw new RuntimeException("手机号已存在");
            }
        }

        // 5. 密码加密
        // Go: bcrypt.GenerateFromPassword(...)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 6. 创建用户并保存
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(encodedPassword);
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setRole(request.getRole());
        newUser.setRegistrationDate(LocalDateTime.now());

        // Go: s.DB.Create(&newUser)
        int rows = userMapper.insert(newUser);
        if (rows <= 0) {
            throw new RuntimeException("用户创建失败");
        }

        return "注册成功";
    }

    @Override
    public LoginResponseDTO loginUser(UserLoginDTO request) {
        // 1. 参数校验
        if (!StringUtils.hasText(request.getUsername()) ||
                !StringUtils.hasText(request.getPassword()) ||
                request.getRole() == null || request.getRole() == 0) {
            throw new RuntimeException("用户名、密码和角色不能为空");
        }

        // 2. 查找用户
        // Go: s.DB.Where("username = ?", username).First(&user)
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        // Go: errors.Is(err, gorm.ErrRecordNotFound)
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        // 3. 校验密码
        // Go: bcrypt.CompareHashAndPassword(...)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 校验角色
        // Go: if user.Role != role
        if (!user.getRole().equals(request.getRole())) {
            throw new RuntimeException("角色不匹配");
        }

        // 5. 返回结果
        return new LoginResponseDTO(user.getUserId(), user.getRole());
    }
}