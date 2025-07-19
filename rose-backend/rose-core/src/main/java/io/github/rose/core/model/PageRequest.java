package io.github.rose.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 分页请求（不可变）
 *
 * @author rose
 */
@Getter
@EqualsAndHashCode
@ToString
public final class PageRequest {
    private final int page;
    private final int size;
    private final String sort;
    private final String direction;
    
    @JsonCreator
    public PageRequest(@JsonProperty("page") Integer page,
                       @JsonProperty("size") Integer size,
                       @JsonProperty("sort") String sort,
                       @JsonProperty("direction") String direction) {
        this.page = page != null && page > 0 ? page : 1;
        this.size = size != null && size > 0 ? Math.min(size, 1000) : 10; // 限制最大1000
        this.sort = sort;
        this.direction = direction != null ? direction.toUpperCase() : "ASC";
    }
    
    /**
     * 创建默认分页请求
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, null, null);
    }
    
    /**
     * 创建带排序的分页请求
     */
    public static PageRequest of(int page, int size, String sort, String direction) {
        return new PageRequest(page, size, sort, direction);
    }
    
    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (page - 1) * size;
    }
    
    /**
     * 是否有排序
     */
    public boolean hasSort() {
        return sort != null && !sort.trim().isEmpty();
    }
    
    /**
     * 是否降序
     */
    public boolean isDesc() {
        return "DESC".equalsIgnoreCase(direction);
    }
}
