package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 * 对应数据库表: sys_log
 */
@Data
@TableName("sys_log")
public class SysLog {

    /**
     * 日志主键 ID
     * 对应数据库: log_id BIGINT AUTO_INCREMENT
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 操作人姓名 (通常是管理员账号)
     * 对应数据库: operator_name
     */
    private String operatorName;

    /**
     * 操作类型 (如: 封禁用户, 重置密码)
     * 对应数据库: operation_type
     */
    private String operationType;

    /**
     * 操作详细内容
     * 对应数据库: operation_content
     */
    private String operationContent;

    /**
     * 操作时间
     * 对应数据库: create_time
     */
    private LocalDateTime createTime;
}