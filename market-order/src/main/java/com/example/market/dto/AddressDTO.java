package com.example.market.dto;

import lombok.Data;

@Data
public class AddressDTO {
    /**
     * 地址ID
     * 注意：
     * 1. 新增(Add)时：可为空 (null)
     * 2. 修改(Update)时：必填
     */
    private Long addressId;

    /**
     * 用户ID
     * (通常后端从 Token 解析，前端不需要传，保留此字段可用于内部流转)
     */
    private Long userId;

    private String recipient;  // 收件人姓名
    private String phone;      // 电话号码
    private String country;    // 国家
    private String province;   // 省份
    private String city;       // 城市
    private String district;   // 区/县
    private String street;     // 详细地址/街道

    private Boolean isDefault; // 是否默认地址 (true/false)

    private String stamp;      // 地址标签 (如: 家, 公司, 学校)
}