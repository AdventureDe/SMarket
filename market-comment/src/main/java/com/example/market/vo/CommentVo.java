package com.example.market.vo;


import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVo {
    private Long id;
    private Long productId;
    private Long userId;
    private String content;
    private Integer score;
    private List<String> images;
    private Integer imageCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isAnonymous;
    private String nickname;
    private String avatar;
    private Boolean liked;          // 当前用户是否点赞
    private LocalDateTime createTime;
    private String productName;

    private List<CommentReplyVo> replies;
}