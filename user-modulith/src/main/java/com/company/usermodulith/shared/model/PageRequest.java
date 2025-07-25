package com.company.usermodulith.shared.model;

import lombok.Data;

/**
 * 通用分页请求对象
 * <p>
 * 封装分页查询的基本参数，提供默认值和校验
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@Data
public class PageRequest {

    /** 页码，从1开始 */
    private int pageNo = 1;

    /** 每页大小 */
    private int pageSize = 10;

    /**
     * 默认构造函数
     */
    public PageRequest() {
    }

    /**
     * 构造函数
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     */
    public PageRequest(int pageNo, int pageSize) {
        this.pageNo = Math.max(pageNo, 1); // 确保页码至少为1
        this.pageSize = Math.max(pageSize, 1); // 确保每页大小至少为1
    }

    /**
     * 静态工厂方法
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return PageRequest实例
     */
    public static PageRequest of(int pageNo, int pageSize) {
        return new PageRequest(pageNo, pageSize);
    }

    /**
     * 设置页码，确保不小于1
     *
     * @param pageNo 页码
     */
    public void setPageNo(int pageNo) {
        this.pageNo = Math.max(pageNo, 1);
    }

    /**
     * 设置每页大小，确保不小于1
     *
     * @param pageSize 每页大小
     */
    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(pageSize, 1);
    }
}