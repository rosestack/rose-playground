package io.github.rosestack.core.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应对象
 *
 * <p>提供标准的分页响应格式，包含数据列表、分页信息和统计信息
 *
 * @param <T> 数据类型
 * @author rosestack
 * @since 1.0.0
 */
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private long page;

    /**
     * 页大小
     */
    private long size;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 是否有上一页
     */
    private boolean hasPrevious;

    public PageResponse() {}

    public PageResponse(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (long) Math.ceil((double) total / size);
        this.hasNext = page < pages;
        this.hasPrevious = page > 1;
    }

    /**
     * 创建分页响应
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param page    当前页码
     * @param size    页大小
     * @param <T>     数据类型
     * @return 分页响应对象
     */
    public static <T> PageResponse<T> of(List<T> records, long total, long page, long size) {
        return new PageResponse<>(records, total, page, size);
    }

    /**
     * 创建空的分页响应
     *
     * @param page 当前页码
     * @param size 页大小
     * @param <T>  数据类型
     * @return 空的分页响应对象
     */
    public static <T> PageResponse<T> empty(long page, long size) {
        return new PageResponse<>(List.of(), 0, page, size);
    }

    /**
     * 创建空的分页响应（默认参数）
     *
     * @param <T> 数据类型
     * @return 空的分页响应对象
     */
    public static <T> PageResponse<T> empty() {
        return empty(1, 10);
    }

    /**
     * 获取当前页的记录数
     *
     * @return 当前页记录数
     */
    public int getCurrentSize() {
        return records != null ? records.size() : 0;
    }

    /**
     * 判断是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * 判断是否不为空
     *
     * @return 是否不为空
     */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    // Getters and Setters

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "PageResponse{"
                + "records="
                + records
                + ", total="
                + total
                + ", page="
                + page
                + ", size="
                + size
                + ", pages="
                + pages
                + ", hasNext="
                + hasNext
                + ", hasPrevious="
                + hasPrevious
                + '}';
    }
}
