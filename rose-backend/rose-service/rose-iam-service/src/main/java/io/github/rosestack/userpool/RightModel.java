package io.github.rosestack.userpool;

import lombok.Data;

// 权益
@Data
public class RightModel {
    private String code;
    private String name;

    // 1：数量类型，2：数字只读，不做计量，3：断言类型，4：字符串只读
    private Integer dataType;
    private String dataValue;
}
