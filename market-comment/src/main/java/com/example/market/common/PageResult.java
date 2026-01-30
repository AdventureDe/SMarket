package com.example.market.common;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)  // 启用链式调用
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;

    /**
     * 创建分页结果（推荐使用这个静态工厂方法）
     */
    public static <T> PageResult<T> create() {
        return new PageResult<>();
    }

    /**
     * 快速创建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list != null ? list : Collections.emptyList());
        result.setTotal(total != null ? total : 0L);
        result.setPage(page != null ? page : 1);
        result.setSize(size != null ? size : 10);

        if (result.getSize() > 0 && result.getTotal() > 0) {
            result.setPages((int) Math.ceil((double) result.getTotal() / result.getSize()));
        } else {
            result.setPages(0);
        }

        return result;
    }

    /**
     * 从MyBatis-Plus Page创建
     */
    public static <T> PageResult<T> fromPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        if (page == null) {
            return empty();
        }
        return of(page.getRecords(), page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return of(Collections.emptyList(), 0L, 1, 10);
    }
}