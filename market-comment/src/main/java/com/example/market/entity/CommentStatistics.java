package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("comment_statistics")
public class CommentStatistics {
    @TableId
    private Long productId;

    private Integer totalCount;
    private Integer goodCount;     // 4-5分
    private Integer mediumCount;   // 3分
    private Integer badCount;      // 1-2分
    private BigDecimal avgScore;
    private Integer imageCount;    // 有图评价数

    private LocalDateTime updateTime;
}