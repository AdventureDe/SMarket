package com.example.market.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CommentStatisticVo {
    private Long productId;
    private Integer totalCount;     // 总评价数
    private Integer goodCount;      // 好评数
    private Integer mediumCount;    // 中评数
    private Integer badCount;       // 差评数
    private BigDecimal avgScore;    // 平均分
    private Integer imageCount;     // 有图评价数

    // 百分比
    private BigDecimal goodRate;
    private BigDecimal mediumRate;
    private BigDecimal badRate;

    // 评分分布
    private Integer score1Count;    // 1分数量
    private Integer score2Count;    // 2分数量
    private Integer score3Count;    // 3分数量
    private Integer score4Count;    // 4分数量
    private Integer score5Count;    // 5分数量

    public void calculateRates() {
        if (totalCount > 0) {
            BigDecimal total = BigDecimal.valueOf(totalCount);

            goodRate = calculateRate(goodCount, total);
            mediumRate = calculateRate(mediumCount, total);
            badRate = calculateRate(badCount, total);
        } else {
            goodRate = BigDecimal.ZERO.setScale(2);
            mediumRate = BigDecimal.ZERO.setScale(2);
            badRate = BigDecimal.ZERO.setScale(2);
        }
    }

    private BigDecimal calculateRate(Integer count, BigDecimal total) {
        if (count == null || count == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(count)
                .divide(total, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}