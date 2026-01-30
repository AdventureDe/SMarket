package com.example.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.market.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    @Select("SELECT COUNT(*) FROM comment_like " +
            "WHERE comment_id = #{commentId} AND user_id = #{userId}")
    Integer existsByCommentAndUser(@Param("commentId") Long commentId,
                                   @Param("userId") Long userId);
}