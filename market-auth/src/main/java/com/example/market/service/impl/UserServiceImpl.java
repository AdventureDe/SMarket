package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.dto.LoginResponseDTO;
import com.example.market.dto.UserLoginDTO;
import com.example.market.dto.UserPasswordDTO;
import com.example.market.dto.UserUpdateDTO;
import com.example.market.entity.User;
import com.example.market.mapper.UserMapper;
import com.example.market.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 修改个人资料
     */
    @Override
    public void updateProfile(String token, UserUpdateDTO input) {
        // 1. 获取当前用户
        User user = getUserByToken(token);

        // 2. 更新字段 (只更新不为空的字段)
        if (StringUtils.hasText(input.getNickname())) {
            user.setNickname(input.getNickname());
        }
        if (StringUtils.hasText(input.getAvatar())) {
            user.setAvatar(input.getAvatar());
        }
        if (StringUtils.hasText(input.getEmail())) {
            user.setEmail(input.getEmail());
        }

        // 3. 执行更新 SQL
        // MyBatis-Plus 会自动生成 UPDATE user SET nickname=?, avatar=? WHERE user_id=?
        userMapper.updateById(user);
    }

    /**
     * 修改密码
     */
    @Override
    public void changePassword(String token, UserPasswordDTO input) {
        // 1. 获取当前用户
        User user = getUserByToken(token);

        // 2. 校验旧密码是否正确
        // 参数1: 前端传来的明文旧密码, 参数2: 数据库里的加密密码
        if (!passwordEncoder.matches(input.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 3. 加密新密码
        String newEncodedPassword = passwordEncoder.encode(input.getNewPassword());
        user.setPassword(newEncodedPassword);

        // 4. 更新数据库
        userMapper.updateById(user);

        // 5. 强制下线
        // 密码改了，之前的 Token 必须作废，强制用户重新登录
        String redisKey = "login:token:" + token;
        redisTemplate.delete(redisKey);
    }

    /**
     * 私有辅助方法：根据 Token 获取用户信息
     * 避免代码重复
     */
    private User getUserByToken(String token) {
        // 1. 拼装 Key
        String key = "login:token:" + token;

        // 2. 从 Redis 拿 username (登录时我们存的是 username)
        String username = redisTemplate.opsForValue().get(key);

        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("登录已过期或无效Token");
        }

        // 3. 从数据库查完整 User 对象
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}