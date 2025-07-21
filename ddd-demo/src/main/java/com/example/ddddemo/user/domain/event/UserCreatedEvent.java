package com.example.ddddemo.user.domain.event;

import com.example.ddddemo.shared.domain.event.DomainEvent;
import com.example.ddddemo.user.domain.entity.User;

/**
 * 用户创建事件
 * <p>
 * 当用户被创建时发布的事件
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class UserCreatedEvent extends DomainEvent {

    private final User user;

    public UserCreatedEvent(User user) {
        super(user.getId() != null ? user.getId().toString() : null);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getEventType() {
        return "UserCreated";
    }

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "userId=" + user.getId() +
                ", username='" + user.getUsername() + '\'' +
                ", email='" + user.getEmail() + '\'' +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
} 