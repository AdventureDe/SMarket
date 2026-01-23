package com.example.market.service;

import com.example.market.dto.AdminBanDTO;
import com.example.market.dto.AdminResetPwdDTO;
import com.example.market.entity.SysLog;

import java.util.List;

/**
 * 管理员服务接口
 * 提供管理员相关的核心操作，包括用户封禁、密码重置、系统日志查询等功能
 */
public interface AdminService {

    /**
     * 封禁/解封用户
     * @param adminToken 管理员的登录令牌，用于验证管理员身份和权限
     * @param input 封禁/解封用户的请求参数（包含用户ID、封禁状态、封禁原因等）
     */
    void banUser(String adminToken, AdminBanDTO input);

    /**
     * 重置用户密码
     * @param adminToken 管理员的登录令牌，用于验证管理员身份和权限
     * @param input 重置密码的请求参数（包含用户ID、新密码等）
     */
    void resetPassword(String adminToken, AdminResetPwdDTO input);

    /**
     * 查询系统日志
     * @param adminToken 管理员的登录令牌，用于验证管理员身份和权限
     * @return 系统日志列表
     */
    List<SysLog> getLogs(String adminToken);
}