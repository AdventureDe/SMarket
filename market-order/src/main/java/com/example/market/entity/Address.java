package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("addresses")
public class Address {
    @TableId(type = IdType.AUTO)
    private Long addressId;

    private Long userId;
    private String recipient;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    private String street;
    private Boolean isDefault;
    private String stamp; // 对应 Go 的 Stamp
}