package io.github.rosestack.core.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页请求对象
 *
 * <p>提供标准的分页参数，包括页码、页大小和排序信息
 *
 * @author rosestack
 * @since 1.0.0
 */
public class PageRequest implements Serializable {

	/**
	 * 默认页码
	 */
	public static final int DEFAULT_PAGE = 1;
	/**
	 * 默认页大小
	 */
	public static final int DEFAULT_SIZE = 10;
	/**
	 * 最大页大小
	 */
	public static final int MAX_SIZE = 1000;

	@Serial
	private static final long serialVersionUID = 1L;
	/**
	 * 页码（从1开始）
	 */
	private int page = DEFAULT_PAGE;

	/**
	 * 页大小
	 */
	private int size = DEFAULT_SIZE;

	/**
	 * 排序字段
	 */
	private String sortBy;

	/**
	 * 排序方向（asc/desc）
	 */
	private String sortDirection = "desc";

	public PageRequest() {
	}

	public PageRequest(int page, int size) {
		this.page = page;
		this.size = size;
	}

	public PageRequest(int page, int size, String sortBy, String sortDirection) {
		this.page = page;
		this.size = size;
		this.sortBy = sortBy;
		this.sortDirection = sortDirection;
	}

	/**
	 * 创建分页请求
	 *
	 * @param page 页码
	 * @param size 页大小
	 * @return 分页请求对象
	 */
	public static PageRequest of(int page, int size) {
		return new PageRequest(page, size);
	}

	/**
	 * 创建分页请求（默认参数）
	 *
	 * @return 分页请求对象
	 */
	public static PageRequest of() {
		return new PageRequest();
	}

	/**
	 * 获取偏移量
	 *
	 * @return 偏移量
	 */
	public int getOffset() {
		return (page - 1) * size;
	}

	/**
	 * 获取限制数量
	 *
	 * @return 限制数量
	 */
	public int getLimit() {
		return size;
	}

	/**
	 * 验证并修正分页参数
	 *
	 * @return 修正后的分页请求对象
	 */
	public PageRequest validate() {
		if (page < 1) {
			page = DEFAULT_PAGE;
		}
		if (size < 1) {
			size = DEFAULT_SIZE;
		}
		if (size > MAX_SIZE) {
			size = MAX_SIZE;
		}
		return this;
	}

	// Getters and Setters

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}

	@Override
	public String toString() {
		return "PageRequest{"
			+ "page="
			+ page
			+ ", size="
			+ size
			+ ", sortBy='"
			+ sortBy
			+ '\''
			+ ", sortDirection='"
			+ sortDirection
			+ '\''
			+ '}';
	}
}
