package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("addresses") // 对应数据库表名
public class Address implements Serializable {

    @TableId(value = "address_id", type = IdType.AUTO) // 对应 address_id, 自增
    private Long addressId;

    @TableField("user_id")
    private Long userId;

    private String country;
    private String province;
    private String city;
    private String district;
    private String street;

    @TableField("is_default") // 对应 is_default
    private Boolean isDefault; // tinyint(1) 对应 Boolean

    private String recipient;
    private String phone;
    private String stamp; // 对应 stamp 字段

    // 数据库配置了 DEFAULT_GENERATED，这里设为只读或让MP自动处理
    // 通常如果不手动赋值，数据库会自动填入时间
    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updatedAt;
}