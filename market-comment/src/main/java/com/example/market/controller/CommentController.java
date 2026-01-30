package com.example.market.controller;

import com.example.market.dto.*;
import com.example.market.service.CommentService;
import com.example.market.vo.CommentStatisticVo;
import com.example.market.vo.CommentVo;
import com.example.market.common.Result;
import com.example.market.common.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/comment")
@Api(tags = "评价管理")
@Slf4j
@Validated
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/create")
    @ApiOperation("提交评价")
    public Result<Long> createComment(@Valid @RequestBody CommentCreateDTO dto,
                                      HttpServletRequest request) {
        // 从token中获取用户ID（这里模拟）
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            Long commentId = commentService.createComment(dto, userId);
            return Result.success(commentId, "评价提交成功");
        } catch (Exception e) {
            log.error("提交评价失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/product/{productId}")
    @ApiOperation("查询商品评价列表")
    public Result<PageResult<CommentVo>> queryProductComments(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer score,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(required = false, defaultValue = "1") Integer sortType,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request) {

        Long currentUserId = getUserIdFromRequest(request);


        CommentQueryDTO queryDTO = new CommentQueryDTO();
        queryDTO.setProductId(productId);
        queryDTO.setScore(score);
        queryDTO.setHasImage(hasImage);
        queryDTO.setSortType(sortType);
        queryDTO.setPage(page);
        queryDTO.setSize(size);

        try {
            PageResult<CommentVo> result = commentService.queryProductComments(queryDTO, currentUserId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询商品评价失败", e);
            return Result.error(500, "查询失败");
        }
    }

    @GetMapping("/product/{productId}/statistic")
    @ApiOperation("获取商品评价统计")
    public Result<CommentStatisticVo> getProductStatistic(
            @PathVariable Long productId) {
        try {
            CommentStatisticVo statistic = commentService.getProductStatistic(productId);
            return Result.success(statistic);
        } catch (Exception e) {
            log.error("获取评价统计失败", e);
            return Result.error(500, "获取统计失败");
        }
    }

    @PostMapping("/like/toggle")
    @ApiOperation("点赞/取消点赞")
    public Result<CommentLikeResultDTO> toggleLike(@Valid @RequestBody CommentLikeDTO dto,
                                                   HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            CommentLikeResultDTO result = commentService.toggleLike(dto.getCommentId(), userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/like/check/{commentId}")
    @ApiOperation("检查是否点赞")
    public Result<Boolean> checkLiked(@PathVariable Long commentId,
                                      HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            Boolean liked = commentService.checkLiked(commentId, userId);
            return Result.success(liked);
        } catch (Exception e) {
            log.error("检查点赞状态失败", e);
            return Result.error(500, "检查失败");
        }
    }

    @PostMapping("/reply")
    @ApiOperation("回复评价")
    public Result<Void> replyComment(@Valid @RequestBody CommentReplyDTO dto,
                                     HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            commentService.replyComment(dto, userId);
            return Result.success(null, "回复成功");
        } catch (Exception e) {
            log.error("回复评价失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/{commentId}")
    @ApiOperation("获取评价详情")
    public Result<CommentVo> getCommentDetail(@PathVariable Long commentId,
                                              HttpServletRequest request) {
        Long currentUserId = getUserIdFromRequest(request);

        try {
            CommentVo comment = commentService.getCommentDetail(commentId, currentUserId);
            return Result.success(comment);
        } catch (Exception e) {
            log.error("获取评价详情失败", e);
            return Result.error(404, e.getMessage());
        }
    }

    @PostMapping("/batch")
    @ApiOperation("批量获取评价信息")
    public Result<List<CommentVo>> batchGetComments(@RequestBody List<Long> commentIds,
                                                    HttpServletRequest request) {
        Long currentUserId = getUserIdFromRequest(request);

        try {
            List<CommentVo> comments = commentService.batchGetComments(commentIds, currentUserId);
            return Result.success(comments);
        } catch (Exception e) {
            log.error("批量获取评价失败", e);
            return Result.error(500, "批量获取失败");
        }
    }

    @GetMapping("/user/{userId}")
    @ApiOperation("获取用户评价列表")
    public Result<PageResult<CommentVo>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request) {

        Long currentUserId = getUserIdFromRequest(request);

        CommentQueryDTO queryDTO = new CommentQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setPage(page);
        queryDTO.setSize(size);

        try {
            PageResult<CommentVo> result = commentService.queryProductComments(queryDTO, currentUserId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询用户评价失败", e);
            return Result.error(500, "查询失败");
        }
    }

    // 模拟从请求中获取用户ID
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // 实际项目中从token中解析
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userIdStr);
            }
        }
        return null; // 返回null表示未登录
    }
}