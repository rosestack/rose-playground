package io.github.rosestack.iam.userpool;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Package {
    private Long id;

    private String accountNo;
    private String instanceNo;

    private String name;
    private String code;

    // 套餐包版本: standard,Enterprise
    private String group;

    // 套餐包场景编码: B2E,B2C
    private String sceneCode;

    // 套餐包单价
    private Long unitPrice;

    // 套餐包 MAU 数量
    private Long amount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
