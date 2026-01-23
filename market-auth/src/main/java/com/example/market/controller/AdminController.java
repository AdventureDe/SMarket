package com.example.market.controller;

import com.example.market.common.Result;
import com.example.market.dto.AdminBanDTO;
import com.example.market.dto.AdminResetPwdDTO;
import com.example.market.entity.SysLog;
import com.example.market.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 1. 封禁或解封用户
    @PostMapping("/user/ban")
    public Result<String> banUser(@RequestBody AdminBanDTO input, HttpServletRequest request) {
        // 获取当前管理员 Token
        String token = request.getHeader("Authorization").substring(7);
        adminService.banUser(token, input);
        return Result.success(input.getStatus() == 0 ? "封禁成功" : "解封成功");
    }

    // 2. 重置任意用户密码
    @PostMapping("/user/reset-password")
    public Result<String> resetPassword(@RequestBody AdminResetPwdDTO input, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        adminService.resetPassword(token, input);
        return Result.success("密码已重置");
    }

    // 3. 查看系统日志 (简单的列表查询)
    @GetMapping("/logs")
    public Result<List<SysLog>> getSystemLogs(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        List<SysLog> logs = adminService.getLogs(token);
        return Result.success(logs);
    }
}