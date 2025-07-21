package com.example.ddddemo.shared.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 领域事件基类
 * <p>
 * 所有领域事件都应该继承此类
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
@Getter
public abstract class DomainEvent {

    /**
     * 事件ID
     */
    private final String eventId;

    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;

    /**
     * 聚合根ID
     */
    private final String aggregateId;

    protected DomainEvent(String aggregateId) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.aggregateId = aggregateId;
    }

    /**
     * 获取事件类型
     */
    public abstract String getEventType();
}