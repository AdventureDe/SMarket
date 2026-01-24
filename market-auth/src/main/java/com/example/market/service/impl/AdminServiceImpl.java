package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.dto.AdminBanDTO;
import com.example.market.dto.AdminResetPwdDTO;
import com.example.market.entity.SysLog;
import com.example.market.entity.User;
import com.example.market.mapper.SysLogMapper;
import com.example.market.mapper.UserMapper;
import com.example.market.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SysLogMapper sysLogMapper; // 记得创建这个Mapper接口，继承BaseMapper即可
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // === 核心：封禁用户 ===
    @Override
    public void banUser(String adminToken, AdminBanDTO input) {
        // 1. 鉴权：确认操作者是管理员
        checkAdminRole(adminToken);

        // 2. 修改数据库状态
        User targetUser = new User();
        targetUser.setUserId(input.getUserId());
        targetUser.setStatus(input.getStatus()); // 0 或 1
        userMapper.updateById(targetUser);

        // 3. 记录日志
        String action = input.getStatus() == 0 ? "封禁用户" : "解封用户";
        saveLog(adminToken, action, "用户ID: " + input.getUserId());
    }

    // === 重置密码 ===
    @Override
    public void resetPassword(String adminToken, AdminResetPwdDTO input) {
        checkAdminRole(adminToken);

        User targetUser = userMapper.selectById(input.getUserId());
        if (targetUser == null) {
            throw new RuntimeException("目标用户不存在");
        }

        // 设定新密码 (默认123456)
        String rawPassword = StringUtils.hasText(input.getNewPassword()) ? input.getNewPassword() : "123456";
        String encodedPwd = passwordEncoder.encode(rawPassword);

        targetUser.setPassword(encodedPwd);
        userMapper.updateById(targetUser);

        saveLog(adminToken, "重置密码", "重置了用户ID: " + input.getUserId() + " 的密码");
    }

    // === 核心：查看日志 ===
    @Override
    public List<SysLog> getLogs(String adminToken) {
        checkAdminRole(adminToken);
        // 按时间倒序查所有日志
        return sysLogMapper.selectList(new LambdaQueryWrapper<SysLog>()
                .orderByDesc(SysLog::getCreateTime));
    }

    // --- 内部辅助方法：检查是不是管理员 ---
    private void checkAdminRole(String token) {
        String key = "login:token:" + token;
        String username = redisTemplate.opsForValue().get(key); // 假设Redis存的是username

        // 查库确认角色
        User admin = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (admin == null || admin.getRole() != 1) { // 1 是管理员
            throw new RuntimeException("无权操作！非管理员身份");
        }
    }

    // --- 内部辅助方法：写日志 ---
    private void saveLog(String adminToken, String type, String content) {
        String key = "login:token:" + adminToken;
        String adminName = redisTemplate.opsForValue().get(key); // 获取管理员名字

        SysLog log = new SysLog();
        log.setOperatorName(adminName);
        log.setOperationType(type);
        log.setOperationContent(content);
        log.setCreateTime(LocalDateTime.now());

        sysLogMapper.insert(log);
    }
}