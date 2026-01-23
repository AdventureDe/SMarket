package com.example.market.service;

import com.example.market.dto.UserLoginDTO;
import com.example.market.dto.LoginResponseDTO;
import com.example.market.dto.UserPasswordDTO;
import com.example.market.dto.UserUpdateDTO;

public interface UserService {

    /**
     * 修改个人资料
     * @param token 用户Token（用于识别是谁）
     * @param input 修改的参数
     */
    void updateProfile(String token, UserUpdateDTO input);

    /**
     * 修改密码
     * @param token 用户Token
     * @param input 包含旧密码和新密码
     */
    void changePassword(String token, UserPasswordDTO input);
}