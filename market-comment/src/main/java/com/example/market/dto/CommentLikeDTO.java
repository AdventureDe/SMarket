package com.example.market.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class CommentLikeDTO {
    @NotNull(message = "评价ID不能为空")
    private Long commentId;

    // 注意：不需要userId，从请求上下文中获取
    // 不需要action字段，因为点赞/取消点赞是同一个接口的切换操作
}