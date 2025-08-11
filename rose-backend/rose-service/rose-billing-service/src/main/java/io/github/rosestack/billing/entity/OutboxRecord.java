package io.github.rosestack.billing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.rosestack.billing.enums.OutboxStatus;
import io.github.rosestack.core.entity.BaseTenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbox")
public class OutboxRecord extends BaseTenantEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    private String eventType;
    private String aggregateId;
    private String payload; // JSON 字符串
    private OutboxStatus status;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
}

