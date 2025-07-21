package com.example.ddddemo.user.domain.event;

import com.example.ddddemo.shared.domain.event.DomainEvent;
import com.example.ddddemo.user.domain.entity.User;

/**
 * 用户更新事件
 * <p>
 * 当用户信息被更新时发布的事件
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class UserUpdatedEvent extends DomainEvent {

    private final User user;

    public UserUpdatedEvent(User user) {
        super(user.getId() != null ? user.getId().toString() : null);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getEventType() {
        return "UserUpdated";
    }

    @Override
    public String toString() {
        return "UserUpdatedEvent{" +
                "userId=" + user.getId() +
                ", username='" + user.getUsername() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
} 