package com.example.market.dto;


import lombok.Data;
import javax.validation.constraints.*;
import java.util.List;

@Data
public class CommentCreateDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 5, message = "评分最大为5")
    private Integer score;

    @Size(max = 500, message = "评价内容不能超过500字")
    private String content;

    private List<String> images;
    private Boolean isAnonymous;

    // 验证方法
    public void validate() {
        if (images != null && images.size() > 9) {
            throw new IllegalArgumentException("最多上传9张图片");
        }
    }
}