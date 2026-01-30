package com.example.market.dto;

import lombok.Data;

@Data
public class CommentLikeResultDTO {
    private Long commentId;
    private Boolean liked;        // 当前点赞状态
    private Integer likeCount;    // 当前点赞总数
    private String message;       // 操作结果消息
}