package com.example.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.market.entity.CommentImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CommentImageMapper extends BaseMapper<CommentImage> {

    @Select("SELECT image_url FROM comment_image " +
            "WHERE comment_id = #{commentId} ORDER BY sort_order ASC")
    List<String> selectImageUrlsByCommentId(@Param("commentId") Long commentId);

    @Select("SELECT * FROM comment_image " +
            "WHERE comment_id = #{commentId} ORDER BY sort_order ASC")
    List<CommentImage> selectByCommentId(@Param("commentId") Long commentId);
}