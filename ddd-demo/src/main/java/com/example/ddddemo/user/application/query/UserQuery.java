package com.example.ddddemo.user.application.query;

/**
 * 用户查询对象
 * <p>
 * 用于用户查询的参数封装
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class UserQuery {

    /** 页码（从1开始） */
    private int page = 1;
    
    /** 每页大小 */
    private int size = 10;
    
    /** 用户状态 */
    private Integer status;
    
    /** 搜索关键词 */
    private String keyword;

    // 构造函数
    public UserQuery() {}

    public UserQuery(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public UserQuery(int page, int size, Integer status) {
        this.page = page;
        this.size = size;
        this.status = status;
    }

    public UserQuery(int page, int size, String keyword) {
        this.page = page;
        this.size = size;
        this.keyword = keyword;
    }

    // Getter和Setter方法
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
} 