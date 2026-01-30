package com.example.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.market.dto.CommentQueryDTO;
import com.example.market.entity.Comment;
import com.example.market.vo.CommentStatisticVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询商品评价
     */
    Page<Comment> selectCommentPage(Page<Comment> page,
                                    @Param("query") CommentQueryDTO queryDTO);

    /**
     * 获取商品评价统计
     */
    CommentStatisticVo selectCommentStatistic(@Param("productId") Long productId);

    /**
     * 获取评分分布
     */
    @Select("SELECT score, COUNT(*) as count FROM comment " +
            "WHERE product_id = #{productId} AND deleted = 0 AND status = 1 " +
            "GROUP BY score")
    List<ScoreDistribution> selectScoreDistribution(@Param("productId") Long productId);

    /**
     * 查询用户是否已评价订单
     */
    @Select("SELECT COUNT(*) FROM comment " +
            "WHERE order_id = #{orderId} AND user_id = #{userId} AND deleted = 0")
    Integer existsByOrderAndUser(@Param("orderId") Long orderId,
                                 @Param("userId") Long userId);

    class ScoreDistribution {
        private Integer score;
        private Integer count;

        // 构造函数
        public ScoreDistribution() {}

        public ScoreDistribution(Integer score, Integer count) {
            this.score = score;
            this.count = count;
        }

        // Getter和Setter
        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "ScoreDistribution{" +
                    "score=" + score +
                    ", count=" + count +
                    '}';
        }
    }
}

