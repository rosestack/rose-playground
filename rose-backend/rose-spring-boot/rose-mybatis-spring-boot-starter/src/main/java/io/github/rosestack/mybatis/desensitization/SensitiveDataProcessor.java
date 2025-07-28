package io.github.rosestack.mybatis.desensitization;

import io.github.rosestack.mybatis.annotation.SensitiveField;
import io.github.rosestack.mybatis.enums.SensitiveType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 简化的敏感数据脱敏工具类
 * <p>
 * 提供基础的脱敏功能，支持常见的敏感数据类型脱敏。
 * 使用简单直接的方式，无复杂的策略和规则。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public class SensitiveDataProcessor {

    // 手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    
    // 身份证正则
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{15}|\\d{18}$");
    
    // 银行卡正则
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("^\\d{16,19}$");

    /**
     * 对对象进行脱敏处理
     */
    public static Object desensitizeObject(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            // 处理集合
            if (obj instanceof Collection) {
                Collection<?> collection = (Collection<?>) obj;
                collection.forEach(SensitiveDataProcessor::desensitizeObject);
                return obj;
            }

            // 处理数组
            if (obj.getClass().isArray()) {
                Object[] array = (Object[]) obj;
                for (Object item : array) {
                    desensitizeObject(item);
                }
                return obj;
            }

            // 处理普通对象
            Class<?> clazz = obj.getClass();
            
            // 跳过基本类型和包装类型
            if (clazz.isPrimitive() || 
                clazz.getPackage() != null && clazz.getPackage().getName().startsWith("java.")) {
                return obj;
            }

            // 处理对象字段
            ReflectionUtils.doWithFields(clazz, field -> {
                SensitiveField annotation = AnnotationUtils.findAnnotation(field, SensitiveField.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    
                    if (value instanceof String) {
                        String originalValue = (String) value;
                        String desensitizedValue = desensitizeByType(originalValue, annotation.value(), annotation.customRule());
                        field.set(obj, desensitizedValue);
                        
                        log.debug("字段脱敏: {} = {} -> {}", field.getName(), originalValue, desensitizedValue);
                    }
                }
            });

            return obj;
        } catch (Exception e) {
            log.error("对象脱敏失败: {}", e.getMessage(), e);
            return obj;
        }
    }

    /**
     * 根据类型进行脱敏
     */
    private static String desensitizeByType(String value, SensitiveType type, String customRule) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        switch (type) {
            case NAME:
                return desensitizeName(value);
            case PHONE:
                return desensitizePhone(value);
            case EMAIL:
                return desensitizeEmail(value);
            case ID_CARD:
                return desensitizeIdCard(value);
            case BANK_CARD:
                return desensitizeBankCard(value);
            case ADDRESS:
                return desensitizeAddress(value);
            case CUSTOM:
                return desensitizeCustom(value, customRule);
            default:
                return value;
        }
    }

    /**
     * 姓名脱敏
     * 规则：保留第一个和最后一个字符，中间用*替换
     */
    public static String desensitizeName(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }
        
        if (name.length() == 1) {
            return name;
        }
        
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        
        return name.charAt(0) + "*" + name.charAt(name.length() - 1);
    }

    /**
     * 手机号脱敏
     * 规则：保留前3位和后4位，中间用****替换
     */
    public static String desensitizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return phone; // 格式不正确，返回原值
        }
        
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏
     * 规则：保留前3位和@后的内容，中间用***替换
     */
    public static String desensitizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return email;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return email; // 格式不正确，返回原值
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 3) {
            return email; // 用户名太短，返回原值
        }
        
        return email.substring(0, 3) + "***" + email.substring(atIndex);
    }

    /**
     * 身份证脱敏
     * 规则：保留前6位和后4位，中间用****替换
     */
    public static String desensitizeIdCard(String idCard) {
        if (!StringUtils.hasText(idCard)) {
            return idCard;
        }
        
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return idCard; // 格式不正确，返回原值
        }
        
        if (idCard.length() == 15) {
            return idCard.substring(0, 6) + "****" + idCard.substring(11);
        } else {
            return idCard.substring(0, 6) + "****" + idCard.substring(14);
        }
    }

    /**
     * 银行卡脱敏
     * 规则：保留前4位和后4位，中间用****替换
     */
    public static String desensitizeBankCard(String bankCard) {
        if (!StringUtils.hasText(bankCard)) {
            return bankCard;
        }
        
        if (!BANK_CARD_PATTERN.matcher(bankCard).matches()) {
            return bankCard; // 格式不正确，返回原值
        }
        
        return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 地址脱敏
     * 规则：保留前3个字符和最后1个字符，中间用***替换
     */
    public static String desensitizeAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return address;
        }
        
        if (address.length() <= 4) {
            return address; // 太短，返回原值
        }
        
        return address.substring(0, 3) + "***" + address.substring(address.length() - 1);
    }

    /**
     * 自定义脱敏
     * 规则：根据参数指定保留前后字符数
     * 
     * @param value 原始值
     * @param params 参数格式："前保留位数,后保留位数"，如 "2,3"
     */
    public static String desensitizeCustom(String value, String params) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        
        if (!StringUtils.hasText(params)) {
            return value;
        }
        
        try {
            String[] parts = params.split(",");
            if (parts.length != 2) {
                return value;
            }
            
            int prefixLen = Integer.parseInt(parts[0].trim());
            int suffixLen = Integer.parseInt(parts[1].trim());
            
            if (prefixLen < 0 || suffixLen < 0) {
                return value;
            }
            
            if (prefixLen + suffixLen >= value.length()) {
                return value; // 保留位数太多，返回原值
            }
            
            String prefix = value.substring(0, prefixLen);
            String suffix = value.substring(value.length() - suffixLen);
            int maskLen = value.length() - prefixLen - suffixLen;
            
            StringBuilder mask = new StringBuilder();
            for (int i = 0; i < maskLen; i++) {
                mask.append("*");
            }
            
            return prefix + mask + suffix;
        } catch (Exception e) {
            log.warn("自定义脱敏参数解析失败: {}", params, e);
            return value;
        }
    }
}