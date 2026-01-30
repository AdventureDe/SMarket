package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.market.common.PageResult;
import com.example.market.dto.CommentCreateDTO;
import com.example.market.dto.CommentLikeResultDTO;
import com.example.market.dto.CommentQueryDTO;
import com.example.market.dto.CommentReplyDTO;
import com.example.market.entity.*;
import com.example.market.mapper.*;
import com.example.market.service.CommentService;
import com.example.market.vo.CommentReplyVo;
import com.example.market.vo.CommentStatisticVo;

import com.example.market.vo.CommentVo;
import com.example.market.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentImageMapper commentImageMapper;

    @Autowired
    private CommentReplyMapper commentReplyMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private CommentStatisticsMapper commentStatisticsMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String COMMENT_LIKE_KEY = "comment:like:%s"; // 点赞缓存
    private static final String COMMENT_STAT_KEY = "comment:stat:%s"; // 统计缓存

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentCreateDTO dto, Long userId) {
        // 1. 参数验证
        dto.validate();

        // 2. 检查是否已评价（一个订单只能评价一次）
        Integer exists = commentMapper.existsByOrderAndUser(dto.getOrderId(), userId);
        if (exists > 0) {
            throw new RuntimeException("该订单已评价");
        }

        // 3. 保存评价主信息
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(userId);
        comment.setScore(dto.getScore());
        comment.setImageCount(dto.getImages() != null ? dto.getImages().size() : 0);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setStatus(1); // 正常
        comment.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        commentMapper.insert(comment);
        Long commentId = comment.getId();

        // 4. 保存评价图片
        if (!CollectionUtils.isEmpty(dto.getImages())) {
            int sortOrder = 0;
            for (String imageUrl : dto.getImages()) {
                CommentImage image = new CommentImage();
                image.setCommentId(commentId);
                image.setImageUrl(imageUrl);
                image.setSortOrder(sortOrder++);
                image.setCreateTime(LocalDateTime.now());
                commentImageMapper.insert(image);
            }
        }

        // 5. 更新评价统计
        updateCommentStatistics(dto.getProductId());

        // 6. 清除缓存
        clearCommentCache(dto.getProductId());

        return commentId;
    }

    @Override
    public PageResult<CommentVo> queryProductComments(CommentQueryDTO queryDTO, Long currentUserId) {
        // 1. 构建查询条件
        Page<Comment> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<Comment> resultPage = commentMapper.selectCommentPage(page, queryDTO);

        // 2. 转换为VO
        List<CommentVo> commentVOs = convertToVO(resultPage.getRecords(), currentUserId);

        // 3. 构建分页结果
        PageResult<CommentVo> pageResult = PageResult.of(
                     commentVOs,
                     resultPage.getTotal(),
                     queryDTO.getPage(),
                    queryDTO.getSize()
                 );
        return pageResult;
    }


    public CommentStatisticVo getProductStatistic(Long productId) {
        // 1. 尝试从缓存获取
        String cacheKey = String.format(COMMENT_STAT_KEY, productId);
        CommentStatisticVo cached = (CommentStatisticVo) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 从数据库查询基础统计
        CommentStatisticVo statistic = commentMapper.selectCommentStatistic(productId);
        if (statistic == null) {
            statistic = new CommentStatisticVo();
            statistic.setProductId(productId);
            statistic.setTotalCount(0);
            statistic.setGoodCount(0);
            statistic.setMediumCount(0);
            statistic.setBadCount(0);
            statistic.setAvgScore(BigDecimal.ZERO);
            statistic.setImageCount(0);
        }

        // 3. 获取评分分布数据
        List<CommentMapper.ScoreDistribution> distributions = commentMapper.selectScoreDistribution(productId);

        // 4. 将评分分布数据转换为Map
        Map<Integer, Integer> scoreMap = new HashMap<>();
        for (CommentMapper.ScoreDistribution distribution : distributions) {
            // 使用内部类的getScore()和getCount()方法
            scoreMap.put(distribution.getScore(), distribution.getCount());
        }

        // 5. 设置评分分布到VO中
        statistic.setScore1Count(scoreMap.getOrDefault(1, 0));
        statistic.setScore2Count(scoreMap.getOrDefault(2, 0));
        statistic.setScore3Count(scoreMap.getOrDefault(3, 0));
        statistic.setScore4Count(scoreMap.getOrDefault(4, 0));
        statistic.setScore5Count(scoreMap.getOrDefault(5, 0));

        // 6. 计算百分比
        if (statistic.getTotalCount() > 0) {
            BigDecimal total = BigDecimal.valueOf(statistic.getTotalCount());

            BigDecimal goodRate = BigDecimal.valueOf(statistic.getGoodCount())
                    .divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal mediumRate = BigDecimal.valueOf(statistic.getMediumCount())
                    .divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal badRate = BigDecimal.valueOf(statistic.getBadCount())
                    .divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            statistic.setGoodRate(goodRate);
            statistic.setMediumRate(mediumRate);
            statistic.setBadRate(badRate);
        } else {
            statistic.setGoodRate(BigDecimal.ZERO);
            statistic.setMediumRate(BigDecimal.ZERO);
            statistic.setBadRate(BigDecimal.ZERO);
        }

        // 7. 存入缓存（5分钟过期）
        redisTemplate.opsForValue().set(cacheKey, statistic, 5, TimeUnit.MINUTES);

        return statistic;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentLikeResultDTO toggleLike(Long commentId, Long userId) {
        // 1. 验证评价是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getDeleted())) {
            throw new RuntimeException("评价不存在");
        }

        // 2. 检查是否已点赞
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);

        CommentLike existingLike = commentLikeMapper.selectOne(queryWrapper);
        boolean liked;
        String message;

        if (existingLike != null) {
            // 已点赞 → 取消点赞
            commentLikeMapper.deleteById(existingLike.getId());

            // 更新评价点赞数
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            liked = false;
            message = "取消点赞成功";

            log.debug("用户 {} 取消点赞评价 {}", userId, commentId);
        } else {
            // 未点赞 → 点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());

            commentLikeMapper.insert(like);

            // 更新评价点赞数
            comment.setLikeCount(comment.getLikeCount() + 1);
            liked = true;
            message = "点赞成功";

            log.debug("用户 {} 点赞评价 {}", userId, commentId);
        }

        // 3. 更新评价
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);

        // 4. 构建返回结果
        CommentLikeResultDTO result = new CommentLikeResultDTO();
        result.setCommentId(commentId);
        result.setLiked(liked);
        result.setLikeCount(comment.getLikeCount());
        result.setMessage(message);

        return result;
    }

    @Override
    public Boolean checkLiked(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            return false;
        }

        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);

        return commentLikeMapper.selectCount(wrapper) > 0;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyComment(CommentReplyDTO dto, Long userId) {
        // 1. 检查评价是否存在
        Comment comment = commentMapper.selectById(dto.getCommentId());
        if (comment == null || comment.getDeleted()) {
            throw new RuntimeException("评价不存在");
        }

        // 2. 保存回复
        CommentReply reply = new CommentReply();
        BeanUtils.copyProperties(dto, reply);
        reply.setUserId(userId);
        reply.setCreateTime(LocalDateTime.now());

        commentReplyMapper.insert(reply);

        // 3. 更新评价回复数
        comment.setReplyCount(comment.getReplyCount() + 1);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
    }

    @Override
    public CommentVo getCommentDetail(Long commentId, Long currentUserId) {
        // 1. 查询评价基本信息
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getDeleted()) {
            throw new RuntimeException("评价不存在");
        }

        // 2. 转换为VO
        CommentVo vo = convertToVO(comment, currentUserId);

        // 3. 查询回复列表
        List<CommentReply> replies = commentReplyMapper.selectByCommentId(commentId);
        List<CommentReplyVo> replyVOs = convertToReplyVO(replies);
        vo.setReplies(replyVOs);

        return vo;
    }

    @Override
    public List<CommentVo> batchGetComments(List<Long> commentIds, Long currentUserId) {
        if (CollectionUtils.isEmpty(commentIds)) {
            return Collections.emptyList();
        }

        // 批量查询
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Comment::getId, commentIds)
                .eq(Comment::getDeleted, false)
                .eq(Comment::getStatus, 1);

        List<Comment> comments = commentMapper.selectList(wrapper);
        return convertToVO(comments, currentUserId);
    }

    // =============== 私有方法 ===============

    private List<CommentVo> convertToVO(List<Comment> comments, Long currentUserId) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        List<CommentVo> result = new ArrayList<>();
        Set<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toSet());

        // 批量查询图片
        Map<Long, List<String>> imageMap = batchGetImages(commentIds);

        // 批量查询用户点赞状态
        Set<Long> likedCommentIds = currentUserId != null ?
                getLikedCommentIds(commentIds, currentUserId) : Collections.emptySet();

        for (Comment comment : comments) {
            CommentVo vo = new CommentVo();
            BeanUtils.copyProperties(comment, vo);

            // 设置图片
            vo.setImages(imageMap.getOrDefault(comment.getId(), Collections.emptyList()));

            // 设置点赞状态
            if (currentUserId != null) {
                vo.setLiked(likedCommentIds.contains(comment.getId()));
            }

            // 匿名处理
            if (Boolean.TRUE.equals(comment.getIsAnonymous())) {
                vo.setNickname("匿名用户");
                vo.setAvatar(null);
            }

            result.add(vo);
        }

        return result;
    }

    private CommentVo convertToVO(Comment comment, Long currentUserId) {
        CommentVo vo = new CommentVo();
        BeanUtils.copyProperties(comment, vo);

        // 查询图片
        List<String> images = commentImageMapper.selectImageUrlsByCommentId(comment.getId());
        vo.setImages(images);

        // 查询点赞状态
        if (currentUserId != null) {
            Integer liked = commentLikeMapper.existsByCommentAndUser(comment.getId(), currentUserId);
            vo.setLiked(liked > 0);
        }

        // 匿名处理
        if (Boolean.TRUE.equals(comment.getIsAnonymous())) {
            vo.setNickname("匿名用户");
            vo.setAvatar(null);
        }

        return vo;
    }

    private List<CommentReplyVo> convertToReplyVO(List<CommentReply> replies) {
        if (CollectionUtils.isEmpty(replies)) {
            return Collections.emptyList();
        }

        return replies.stream().map(reply -> {
            CommentReplyVo vo = new CommentReplyVo();
            BeanUtils.copyProperties(reply, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, List<String>> batchGetImages(Set<Long> commentIds) {
        if (CollectionUtils.isEmpty(commentIds)) {
            return Collections.emptyMap();
        }

        Map<Long, List<String>> result = new HashMap<>();

        // 批量查询图片
        LambdaQueryWrapper<CommentImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CommentImage::getCommentId, commentIds)
                .orderByAsc(CommentImage::getSortOrder);

        List<CommentImage> images = commentImageMapper.selectList(wrapper);

        for (CommentImage image : images) {
            result.computeIfAbsent(image.getCommentId(), k -> new ArrayList<>())
                    .add(image.getImageUrl());
        }

        return result;
    }

    private Set<Long> getLikedCommentIds(Set<Long> commentIds, Long userId) {
        if (CollectionUtils.isEmpty(commentIds) || userId == null) {
            return Collections.emptySet();
        }

        // 使用缓存优化
        Set<Long> likedIds = new HashSet<>();
        for (Long commentId : commentIds) {
            String cacheKey = String.format(COMMENT_LIKE_KEY, commentId) + ":user:" + userId;
            Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);

            if (cached != null) {
                if (cached) likedIds.add(commentId);
                continue;
            }

            // 查数据库
            Integer exists = commentLikeMapper.existsByCommentAndUser(commentId, userId);
            boolean liked = exists > 0;

            if (liked) likedIds.add(commentId);

            // 存入缓存（1小时过期）
            redisTemplate.opsForValue().set(cacheKey, liked, 1, TimeUnit.HOURS);
        }

        return likedIds;
    }

    private void updateCommentStatistics(Long productId) {
        CommentStatisticVo statistic = commentMapper.selectCommentStatistic(productId);

        CommentStatistics stats = new CommentStatistics();
        if (statistic != null) {
            BeanUtils.copyProperties(statistic, stats);
        } else {
            stats.setProductId(productId);
            stats.setTotalCount(0);
            stats.setGoodCount(0);
            stats.setMediumCount(0);
            stats.setBadCount(0);
            stats.setAvgScore(BigDecimal.ZERO);
            stats.setImageCount(0);
        }

        stats.setUpdateTime(LocalDateTime.now());

        // 保存或更新
        CommentStatistics existing = commentStatisticsMapper.selectById(productId);
        if (existing != null) {
            commentStatisticsMapper.updateById(stats);
        } else {
            commentStatisticsMapper.insert(stats);
        }
    }

    private void clearCommentCache(Long productId) {
        // 清除统计缓存
        String statKey = String.format(COMMENT_STAT_KEY, productId);
        redisTemplate.delete(statKey);

        // 清除相关评价的点赞缓存
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getProductId, productId)
                .select(Comment::getId);

        List<Comment> comments = commentMapper.selectList(wrapper);
        for (Comment comment : comments) {
            String likeKey = String.format(COMMENT_LIKE_KEY, comment.getId()) + ":user:*";
            Set<String> keys = redisTemplate.keys(likeKey);
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        }
    }
}