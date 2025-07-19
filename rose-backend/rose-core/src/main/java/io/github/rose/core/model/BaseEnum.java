package io.github.rose.core.model;

/**
 * 基础枚举接口
 * 为所有业务枚举提供统一的契约
 *
 * @author rose
 */
public interface BaseEnum {

    /**
     * 获取枚举代码
     */
    String getCode();

    /**
     * 获取枚举名称
     */
    String getName();
    
    /**
     * 根据代码查找枚举
     */
    static <E extends Enum<E> & BaseEnum> E fromCode(Class<E> enumClass, String code) {
        if (code == null) {
            return null;
        }
        
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (code.equals(enumConstant.getCode())) {
                return enumConstant;
            }
        }
        
        throw new IllegalArgumentException("No enum constant " + enumClass.getSimpleName() + " with code: " + code);
    }
}
