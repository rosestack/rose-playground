package io.github.rosestack.notice.domain.entity;

import io.github.rosestack.notice.domain.value.NoticeChannelType;
import java.util.Map;
import lombok.Data;

/**
 * 渠道配置聚合根
 */
@Data
public class NoticeChannel {
    private String id;
    private String tenantId;
    private NoticeChannelType channelType;
    private Map<String, Object> config;
    private boolean enabled;

    /**
     * 通道优先级，数值越小优先级越高
     */
    private Integer priority;
}
