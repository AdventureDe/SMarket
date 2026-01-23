package com.example.market.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;

// Servlet API 相关
import javax.servlet.http.HttpServletRequest;

import com.example.market.common.Result;
import com.example.market.service.UserService;
import com.example.market.dto.UserPasswordDTO;
import com.example.market.dto.UserUpdateDTO;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. 修改资料
    @PostMapping("/update")
    public Result<String> updateProfile(@RequestBody UserUpdateDTO input, HttpServletRequest request) {
        // 从 Header 拿 Token 解析出 userId (或者在拦截器里存入 ThreadLocal)
        String token = request.getHeader("Authorization").substring(7);
        userService.updateProfile(token, input);
        return Result.success("修改成功");
    }

    // 2. 修改密码
    @PostMapping("/password")
    public Result<String> changePassword(@RequestBody UserPasswordDTO input, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        userService.changePassword(token, input);
        return Result.success("密码修改成功，请重新登录");
    }
}