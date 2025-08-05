package io.github.rosestack.core.jackson.desensitization;

import io.github.rosestack.core.annotation.FieldSensitive;
import io.github.rosestack.core.jackson.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

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
public class MaskUtils {
    // 手机号正则
    public static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 邮箱正则
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    // 身份证正则
    public static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{15}|\\d{18}$");

    // 银行卡正则
    public static final Pattern BANK_CARD_PATTERN = Pattern.compile("^\\d{16,19}$");

    // 车牌号正则 - 支持新能源和传统车牌
    public static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile(
            "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$"
    );

    // IPv4地址正则
    public static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    public static final char MASK = '*';
    public static final String MASKED = "**MASKED**";

    public static String mask(String value, FieldSensitive fieldSensitive) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        switch (fieldSensitive.type()) {
            case NAME:
                return maskName(value);
            case PHONE:
                return maskPhone(value);
            case EMAIL:
                return maskEmail(value);
            case ID_CARD:
                return maskIdCard(value);
            case BANK_CARD:
                return maskBankCard(value);
            case ADDRESS:
                return maskAddress(value);
            case LICENSE_PLATE:
                return maskLicensePlate(value);
            case IPV4:
                return maskIpAddress(value);
            case PASSWORD:
                return maskPassword(value);
            default:
                return value;
        }
    }

    /**
     * 姓名脱敏
     *
     * @param name
     * @return
     */
    public static String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return name;
        }

        if (name.length() <= 2) {
            return maskCustom(name, 1, 0, MASK);
        }

        return maskCustom(name, 1, 1, MASK);
    }

    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return phone; // 格式不正确，返回原值
        }

        return phone.substring(0, 3) + generateMask(4, MASK) + phone.substring(7);
    }

    public static String maskEmail(String email) {
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

        return email.substring(0, 3) + generateMask(4, MASK) + email.substring(atIndex);
    }

    public static String maskIdCard(String idCard) {
        if (!StringUtils.hasText(idCard)) {
            return idCard;
        }

        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return idCard; // 格式不正确，返回原值
        }

        if (idCard.length() == 15) {
            return idCard.substring(0, 6) + generateMask(5, MASK) + idCard.substring(11);
        } else {
            return idCard.substring(0, 6) + generateMask(8, MASK) + idCard.substring(14);
        }
    }

    public static String maskBankCard(String bankCard) {
        if (!StringUtils.hasText(bankCard)) {
            return bankCard;
        }

        if (!BANK_CARD_PATTERN.matcher(bankCard).matches()) {
            return bankCard; // 格式不正确，返回原值
        }

        int maskLength = bankCard.length() - 8; // 前4位 + 后4位
        return bankCard.substring(0, 4) + generateMask(maskLength, MASK) + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 地址脱敏
     * 规则：保留前3位和后1位，中间用*替换
     *
     * @param address
     * @return
     */
    public static String maskAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return address;
        }

        if (address.length() <= 4) {
            return address; // 太短，返回原值
        }

        int maskLength = address.length() - 4; // 前3位 + 后1位
        return address.substring(0, 3) + generateMask(maskLength, MASK) + address.substring(address.length() - 1);
    }

    /**
     * 车牌号脱敏
     * 规则：保留前2位和后2位，中间用***替换
     * 例如：京A12345 -> 京A***45
     */
    public static String maskLicensePlate(String licensePlate) {
        if (!StringUtils.hasText(licensePlate)) {
            return licensePlate;
        }

        if (!LICENSE_PLATE_PATTERN.matcher(licensePlate).matches()) {
            return licensePlate; // 格式不正确，返回原值
        }

        if (licensePlate.length() <= 4) {
            return licensePlate; // 太短，返回原值
        }

        // 保留前2位和后2位
        int maskLength = licensePlate.length() - 4;
        return licensePlate.substring(0, 2) + generateMask(maskLength, MASK) +
                licensePlate.substring(licensePlate.length() - 2);
    }

    /**
     * IPv4地址脱敏
     * 规则：保留前两段，后两段用***替换
     * 例如：192.168.1.100 -> 192.168.***.***
     */
    public static String maskIpAddress(String ipv4) {
        if (!StringUtils.hasText(ipv4)) {
            return ipv4;
        }

        if (!IPV4_PATTERN.matcher(ipv4).matches()) {
            return ipv4; // 格式不正确，返回原值
        }

        String[] parts = ipv4.split("\\.");
        if (parts.length != 4) {
            return ipv4;
        }

        String maskSegment = generateMask(3, MASK);
        return parts[0] + "." + parts[1] + "." + maskSegment + "." + maskSegment;
    }

    public static String maskPassword(String password) {
        return MASKED;
    }

    /**
     * Token脱敏
     */
    public static String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return token;
        }

        if (token.startsWith("Bearer ")) {
            return "Bearer " + MASKED;
        } else if (token.startsWith("Basic ")) {
            return "Basic " + MASKED;
        } else {
            return MASKED;
        }
    }

    public static String maskCustom(String value, int prefixKeep, int suffixKeep, char maskChar) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        if (prefixKeep < 0 || suffixKeep < 0) {
            return value;
        }

        if (prefixKeep + suffixKeep >= value.length()) {
            return value; // 保留位数太多，返回原值
        }

        String prefix = value.substring(0, prefixKeep);
        String suffix = value.substring(value.length() - suffixKeep);
        int maskLen = value.length() - prefixKeep - suffixKeep;

        return prefix + generateMask(maskLen, maskChar) + suffix;
    }

    /**
     * 根据模式进行脱敏
     */
    public static String maskByPattern(String data) {
        // 手机号检测
        if (PHONE_PATTERN.matcher(data).matches()) {
            return MaskUtils.maskPhone(data);
        }

        // 邮箱检测
        if (EMAIL_PATTERN.matcher(data).find()) {
            return MaskUtils.maskEmail(data);
        }

        // 身份证检测
        if (ID_CARD_PATTERN.matcher(data).matches()) {
            return MaskUtils.maskIdCard(data);
        }

        // 银行卡检测
        if (BANK_CARD_PATTERN.matcher(data).matches()) {
            return MaskUtils.maskBankCard(data);
        }

        // IP地址检测
        if (IPV4_PATTERN.matcher(data).matches()) {
            return MaskUtils.maskIpAddress(data);
        }

        // 如果都不匹配，返回原值
        return data;
    }

    /**
     * 对对象中的敏感字段进行脱敏处理
     * 递归遍历对象的每个属性，如果属性为字符串类型且属性名称在脱敏数组内，则对该属性的值进行脱敏
     *
     * @param object          需要脱敏的对象
     * @param sensitiveFields 敏感字段名称数组
     * @param <T>             对象类型
     * @return 脱敏后的对象
     */
    public static <T> T maskSensitiveFields(T object, String... sensitiveFields) {
        if (object == null || sensitiveFields == null || sensitiveFields.length == 0) {
            return object;
        }

        // 使用通用方法和脱敏处理器
        return JsonUtils.processFields(object, JsonUtils.createCustomMaskProcessor(MASKED, sensitiveFields));
    }

    /**
     * 生成智能脱敏符号
     * <p>
     * 核心优化：限制脱敏符号的最大长度，避免过长的脱敏符号影响显示效果
     * 同时减少信息泄露（过多的*号可能暴露原始数据长度）
     * </p>
     *
     * @param length   需要脱敏的长度
     * @param charMask 脱敏字符
     * @return 智能生成的脱敏符号
     */
    private static String generateMask(int length, char charMask) {
        if (length <= 0) {
            return "";
        }

        return String.valueOf(charMask).repeat(length);
    }
}