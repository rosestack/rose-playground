package io.github.rosestack.mybatis.desensitization;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import io.github.rosestack.mybatis.annotation.SensitiveField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * 敏感数据脱敏工具类
 * <p>
 * 统一处理敏感字段脱敏，支持单个对象、集合对象的自动脱敏。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class SensitiveDataProcessor {

    /**
     * 对对象进行脱敏处理
     *
     * @param obj 待脱敏的对象
     * @return 脱敏后的对象
     */
    public static Object desensitizeObject(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            // 处理集合类型
            if (obj instanceof Collection) {
                Collection<?> collection = (Collection<?>) obj;
                collection.forEach(SensitiveDataProcessor::desensitizeObject);
                return obj;
            }

            // 处理单个对象
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                SensitiveField sensitiveField = field.getAnnotation(SensitiveField.class);
                if (sensitiveField != null) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);
                    if (fieldValue instanceof String) {
                        String desensitizedValue = desensitize((String) fieldValue,
                                sensitiveField.value(), sensitiveField.customRule());
                        field.set(obj, desensitizedValue);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("对象脱敏处理失败: {}", e.getMessage());
        }

        return obj;
    }

    /**
     * 脱敏处理
     *
     * @param originalValue 原始值
     * @param sensitiveType 脱敏类型
     * @param customRule    自定义规则
     * @return 脱敏后的值
     */
    public static String desensitize(String originalValue, SensitiveType sensitiveType, String customRule) {
        if (!StringUtils.hasText(originalValue)) {
            return originalValue;
        }

        try {
            switch (sensitiveType) {
                case NAME:
                    return desensitizeName(originalValue);
                case ADDRESS:
                    return desensitizeAddress(originalValue);
                case PHONE:
                    return desensitizePhone(originalValue);
                case EMAIL:
                    return desensitizeEmail(originalValue);
                case ID_CARD:
                    return desensitizeIdCard(originalValue);
                case BANK_CARD:
                    return desensitizeBankCard(originalValue);
                case PLATE_CARD:
                    return desensitizePlateCard(originalValue);
                case SECRET:
                    return "******";
                case CUSTOM:
                    return desensitizeCustom(originalValue, customRule);
                default:
                    return originalValue;
            }
        } catch (Exception e) {
            log.warn("脱敏处理失败，返回原始值: {}", e.getMessage());
            return originalValue;
        }
    }

    public static String deSensitive(String origin, int prefixKeep, int suffixKeep, String mask) {
        if (StringUtils.isEmpty(origin)) {
            return StringPool.EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = origin.length(); i < n; i++) {
            if (i < prefixKeep) {
                sb.append(origin.charAt(i));
                continue;
            }
            if (i > (n - suffixKeep - 1)) {
                sb.append(origin.charAt(i));
                continue;
            }
            sb.append(mask);
        }
        return sb.toString();
    }

    /**
     * 手机号脱敏：138****8000
     */
    public static String desensitizePhone(String phone) {
        return deSensitive(phone, 3, 4, StringPool.ASTERISK);
    }

    /**
     * 身份证号脱敏：110101****1234
     */
    public static String desensitizeIdCard(String idCard) {
        if (idCard.length() < 8) {
            return idCard;
        }
        return deSensitive(idCard, 6, 4, StringPool.ASTERISK);
    }

    /**
     * 邮箱脱敏：abc***@example.com
     */
    public static String desensitizeEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 0) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 3) {
            return username.charAt(0) + "***" + domain;
        } else {
            return username.substring(0, 3) + "***" + domain;
        }
    }

    /**
     * 银行卡号脱敏：6222****1234
     */
    public static String desensitizeBankCard(String bankCard) {
        return deSensitive(bankCard, 4, 4, StringPool.ASTERISK);
    }

    public static String desensitizePlateCard(String carLicense) {
        return deSensitive(carLicense, 2, 1, StringPool.ASTERISK);
    }

    /**
     * 姓名脱敏：张*三
     */
    public static String desensitizeName(String name) {
        if (name.length() <= 1) {
            return name;
        } else if (name.length() == 2) {
            return name.charAt(0) + "*";
        } else {
            return deSensitive(name, 1, 1, StringPool.ASTERISK);
        }
    }

    /**
     * 地址脱敏：北京市***区
     */
    public static String desensitizeAddress(String address) {
        if (address.length() <= 6) {
            return address.substring(0, 1) + "***";
        }
        return deSensitive(address, 3, 1, StringPool.ASTERISK);
    }

    /**
     * 自定义脱敏：根据规则保留前N位和后N位
     */
    public static String desensitizeCustom(String value, String rule) {
        if (!StringUtils.hasText(rule)) {
            rule = "3,4";
        }

        String[] parts = rule.split(",");
        if (parts.length != 2) {
            return value;
        }

        try {
            int prefixLength = Integer.parseInt(parts[0].trim());
            int suffixLength = Integer.parseInt(parts[1].trim());

            return deSensitive(value, prefixLength, suffixLength, StringPool.ASTERISK);
        } catch (NumberFormatException e) {
            log.warn("自定义脱敏规则格式错误: {}", rule);
            return value;
        }
    }
}
