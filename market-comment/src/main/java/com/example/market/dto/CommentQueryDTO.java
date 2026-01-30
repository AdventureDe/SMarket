package com.example.market.dto;

import lombok.Data;

@Data
public class CommentQueryDTO {
    private Long productId;
    private Long userId;
    private Integer score;          // 筛选评分：1-5
    private Boolean hasImage;       // 是否有图
    private Integer sortType = 1;   // 1-默认时间倒序，2-点赞数，3-评分

    // 分页参数
    private Integer page = 1;
    private Integer size = 10;

    public Integer getOffset() {
        return (page - 1) * size;
    }
}