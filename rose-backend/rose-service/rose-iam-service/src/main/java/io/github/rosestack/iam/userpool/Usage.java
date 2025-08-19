package io.github.rosestack.iam.userpool;

import lombok.Data;

@Data
public class Usage {
    private Long id;

    private Long packageId;

    // 权益总量
    private Long amount;

    // 权益当前使用量
    private Long current;

    // 是否是体验期权益
    private Boolean experience;

    // 权益编码
    private String modelCode;

    // 权益名称
    private String modelName;
}
