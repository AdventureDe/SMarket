package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment_image")
public class CommentImage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createTime;
}