package com.example.market.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class CommentReplyDTO {
    @NotNull(message = "评价ID不能为空")
    private Long commentId;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 200, message = "回复内容不能超过200字")
    private String content;

    private Integer userType = 2; // 默认商家回复
}