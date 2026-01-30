package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long productId;
    private Long userId;
    private String content;
    private Integer score;
    private Integer imageCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isAnonymous;
    private Integer status;

    @TableLogic
    private Boolean deleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private String nickname;//

    @TableField(exist = false)
    private String avatar;//

    @TableField(exist = false)
    private List<String> images;

    @TableField(exist = false)
    private List<CommentReply> replies;

    @TableField(exist = false)
    private Boolean liked; // 当前用户是否点赞

    @TableField(exist = false)
    private String productName;
}