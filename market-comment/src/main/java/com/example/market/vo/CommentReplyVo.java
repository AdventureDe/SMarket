package com.example.market.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentReplyVo {
    private Long id;
    private Long commentId;
    private Long userId;
    private Integer userType;      // 1-买家，2-商家，3-系统
    private String content;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
}