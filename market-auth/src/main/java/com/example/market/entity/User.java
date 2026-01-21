package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users") // 假设数据库表名叫 users
public class User {
    @TableId(type = IdType.AUTO) // 对应 Go 的 gorm.Model ID
    private Long userId;

    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer role;

    private LocalDateTime registrationDate; // 对应 Go 的 time.Time
}