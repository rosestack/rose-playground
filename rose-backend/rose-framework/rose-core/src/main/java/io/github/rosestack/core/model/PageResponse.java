package io.github.rosestack.core.model;

import lombok.Data;

import java.util.List;

/**
 * 通用分页响应对象
 * <p>
 * 支持泛型的分页响应，不依赖任何 ORM 框架
 * </p>
 *
 * @param <T> 数据类型
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
public class PageResponse<T> {

    /** 当前页码 */
    private Long current;

    /** 每页大小 */
    private Long size;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 数据列表 */
    private List<T> records;

    /** 是否有下一页 */
    private Boolean hasNext;

    /** 是否有上一页 */
    private Boolean hasPrevious;

    /**
     * 创建分页响应对象
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param total 总记录数
     * @param records 数据列表
     * @param <T> 数据类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> of(Long current, Long size, Long total, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setCurrent(current);
        response.setSize(size);
        response.setTotal(total);
        response.setRecords(records);
        
        // 计算总页数
        long pages = (total + size - 1) / size;
        response.setPages(pages);
        
        // 计算是否有上一页和下一页
        response.setHasPrevious(current > 1);
        response.setHasNext(current < pages);
        
        return response;
    }

    /**
     * 创建空的分页响应对象
     *
     * @param <T> 数据类型
     * @return 空的分页响应对象
     */
    public static <T> PageResponse<T> empty() {
        return of(1L, 10L, 0L, List.of());
    }
} 