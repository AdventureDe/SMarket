package com.example.market.controller;

import com.example.market.common.Result; // 假设你有一个通用的返回结果封装类，下文会提供
import com.example.market.dto.LoginResponseDTO;
import com.example.market.dto.UserLoginDTO;
import com.example.market.dto.UserRegisterDTO;
import com.example.market.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
// 对应 Go 中的 RegisterAuthRoutes，这里通常加一个前缀，也可以不加
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 注册接口
     * 对应 Go: r.POST("/register", ...)
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO input) {
        // Go: c.ShouldBindJSON(&input) -> Java: @RequestBody 自动完成
        // Go: service.RegisterUser(...)
        try {
            String message = authService.registerUser(input);
            // Go: c.JSON(http.StatusCreated, gin.H{"message": message})
            return Result.success(message);
        } catch (Exception e) {
            // Go: c.JSON(http.StatusBadRequest, ...)
            // 实际项目中通常使用全局异常处理器(@ControllerAdvice)，这里为了还原逻辑先用try-catch
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 登录接口
     * 对应 Go: r.POST("/login", ...)
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestBody UserLoginDTO input) {
        try {
            // Go: service.LoginUser(...)
            LoginResponseDTO userInfo = authService.loginUser(input);

            // Go: c.JSON(http.StatusOK, gin.H{ "message": "登录成功", ... })
            return Result.success(userInfo, "登录成功");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}