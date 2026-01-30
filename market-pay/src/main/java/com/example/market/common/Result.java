package com.example.market.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局统一返回结果
 */
@Data
@NoArgsConstructor // 1. 生成无参构造
@AllArgsConstructor // 2. 生成全参构造
@ApiModel(value = "统一返回结果")
public class Result<T> {

    @ApiModelProperty(value = "响应码（200-成功，其他-失败）")
    private Integer code;

    @ApiModelProperty(value = "响应信息")
    private String msg;

    @ApiModelProperty(value = "响应数据")
    private T data;

    // 成功响应（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功响应（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 失败响应
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // 自定义失败响应
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // 判断是否成功
    public boolean isSuccess() {
        // 3. 基本类型不能直接调方法，建议先判空再比较，或者用 equals
        return this.code != null && this.code == 200;
    }
}