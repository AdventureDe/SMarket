package com.example.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.market.common.PageResult;
import com.example.market.dto.CommentCreateDTO;
import com.example.market.dto.CommentLikeResultDTO;
import com.example.market.dto.CommentQueryDTO;
import com.example.market.dto.CommentReplyDTO;
import com.example.market.entity.Comment;
import com.example.market.vo.CommentStatisticVo;
import com.example.market.vo.CommentVo;
import com.example.market.common.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService extends IService<Comment> {

    /**
     * 提交评价
     */
    Long createComment(CommentCreateDTO dto, Long userId);

    /**
     * 分页查询商品评价
     */
    PageResult<CommentVo> queryProductComments(CommentQueryDTO queryDTO, Long currentUserId);

    /**
     * 获取商品评价统计
     */
    CommentStatisticVo getProductStatistic(Long productId);

    /**
     * 回复评价
     */
    void replyComment(CommentReplyDTO dto, Long userId);

    /**
     * 获取评价详情
     */
    CommentVo getCommentDetail(Long commentId, Long currentUserId);

    /**
     * 批量获取评价信息
     */

    List<CommentVo> batchGetComments(List<Long> commentIds, Long currentUserId);
    /**
     * 点赞/取消点赞
     * @return 点赞结果
     */
    CommentLikeResultDTO toggleLike(Long commentId, Long userId);

    /**
     * 检查用户是否点赞
     */
    Boolean checkLiked(Long commentId, Long userId);
}