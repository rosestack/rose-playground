package com.example.ddddemo.shared.domain.entity;

import com.example.ddddemo.shared.domain.event.DomainEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * <p>
 * 所有聚合根都应该继承此类，提供领域事件管理功能
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
@Getter
public abstract class AggregateRoot<ID> extends BaseEntity {

    /**
     * 聚合根ID
     */
    private ID id;

    /**
     * 领域事件列表
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        this.id = id;
    }

    protected AggregateRoot() {
    }

    /**
     * 添加领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 获取所有领域事件
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清除领域事件
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * 设置ID
     */
    protected void setId(ID id) {
        this.id = id;
    }
}