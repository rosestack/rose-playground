package com.example.ddddemo.shared.domain.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 基础实体类
 * <p>
 * 所有实体都应该继承此类，提供通用的属性和方法
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    private Boolean deleted = false;

    /**
     * 设置创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 设置更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}