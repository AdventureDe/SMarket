package com.example.market.vo; // 建议新建 vo 包，或者放在 dto 包也行

import com.example.market.entity.Category;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryVO extends Category {
    // 继承了 Category 的所有字段 (id, name, parentId...)

    // 额外增加一个字段，存放子分类
    private List<CategoryVO> children = new ArrayList<>();

    // 静态工厂方法：把 Entity 转为 VO
    public static CategoryVO fromEntity(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}