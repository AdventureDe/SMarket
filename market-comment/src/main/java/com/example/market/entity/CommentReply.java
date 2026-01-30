package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment_reply")
public class CommentReply {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;
    private Long userId;
    private Integer userType; // 1-买家，2-商家，3-系统
    private String content;

    @TableLogic
    private Boolean deleted;

    private LocalDateTime createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
}