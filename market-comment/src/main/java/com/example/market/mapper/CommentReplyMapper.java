package com.example.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.market.entity.CommentReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentReplyMapper extends BaseMapper<CommentReply> {

    @Select("SELECT * FROM comment_reply " +
            "WHERE comment_id = #{commentId} AND deleted = 0 " +
            "ORDER BY create_time ASC")
    List<CommentReply> selectByCommentId(Long commentId);
}