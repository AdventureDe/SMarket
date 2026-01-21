package com.example.market.service;

import com.example.market.dto.UserLoginDTO;
import com.example.market.dto.LoginResponseDTO;
import com.example.market.dto.UserRegisterDTO;

public interface AuthService {
    /**
     * 注册用户
     * @param request 注册请求参数
     * @return 注册结果消息
     */
    String registerUser(UserRegisterDTO request);

    /**
     * 登录用户
     * @param request 登录请求参数
     * @return 登录成功后的用户信息
     */
    LoginResponseDTO loginUser(UserLoginDTO request);
}