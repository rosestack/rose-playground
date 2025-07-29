package io.github.rosestack.core.jackson.desensitization;

import io.github.rosestack.core.annotation.FieldSensitive;
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
public class Desensitization {
    // 手机号正则
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    // 身份证正则
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{15}|\\d{18}$");

    // 银行卡正则
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("^\\d{16,19}$");

    // 车牌号正则 - 支持新能源和传统车牌
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile(
            "^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$"
    );

    // IPv4地址正则
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );

    public static final char MASK = '*';

    /**
     * 最大脱敏符号数量 - 避免过长的脱敏符号影响显示
     */
    public static final int MAX_MASK_LENGTH = 3;

    public static String desensitize(String value, FieldSensitive fieldSensitive) {
        if (!StringUtils.hasText(value)) {
            return value;
        }

        switch (fieldSensitive.type()) {
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
            case LICENSE_PLATE:
                return desensitizeLicensePlate(value);
            case IPV4:
                return desensitizeIpv4(value);
            default:
                return value;
        }
    }

    public static String desensitizeName(String name) {
        return desensitizeName(name, MASK);
    }

    public static String desensitizeName(String name, char maskChar) {
        if (!StringUtils.hasText(name)) {
            return name;
        }

        if (name.length() <= 2) {
            return desensitizeCustom(name, 1, 0, maskChar);
        }

        return desensitizeCustom(name, 1, 1, maskChar);
    }

    public static String desensitizePhone(String phone) {
        return desensitizePhone(phone, MASK);
    }

    public static String desensitizePhone(String phone, char maskChar) {
        if (!StringUtils.hasText(phone)) {
            return phone;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return phone; // 格式不正确，返回原值
        }

        return phone.substring(0, 3) + generateMask(4, maskChar) + phone.substring(7);
    }

    public static String desensitizeEmail(String email) {
        return desensitizeEmail(email, MASK);
    }

    public static String desensitizeEmail(String email, char maskChar) {
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

        return email.substring(0, 3) + generateMask(3, maskChar) + email.substring(atIndex);
    }

    public static String desensitizeIdCard(String idCard) {
        return desensitizeIdCard(idCard, MASK);
    }

    public static String desensitizeIdCard(String idCard, char maskChar) {
        if (!StringUtils.hasText(idCard)) {
            return idCard;
        }

        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return idCard; // 格式不正确，返回原值
        }

        if (idCard.length() == 15) {
            return idCard.substring(0, 6) + generateMask(5, maskChar) + idCard.substring(11);
        } else {
            return idCard.substring(0, 6) + generateMask(8, maskChar) + idCard.substring(14);
        }
    }

    public static String desensitizeBankCard(String bankCard) {
        return desensitizeBankCard(bankCard, MASK);
    }

    public static String desensitizeBankCard(String bankCard, char maskChar) {
        if (!StringUtils.hasText(bankCard)) {
            return bankCard;
        }

        if (!BANK_CARD_PATTERN.matcher(bankCard).matches()) {
            return bankCard; // 格式不正确，返回原值
        }

        int maskLength = bankCard.length() - 8; // 前4位 + 后4位
        return bankCard.substring(0, 4) + generateMask(maskLength, maskChar) + bankCard.substring(bankCard.length() - 4);
    }

    public static String desensitizeAddress(String address) {
        return desensitizeAddress(address, MASK);
    }

    public static String desensitizeAddress(String address, char maskChar) {
        if (!StringUtils.hasText(address)) {
            return address;
        }

        if (address.length() <= 4) {
            return address; // 太短，返回原值
        }

        int maskLength = address.length() - 4; // 前3位 + 后1位
        return address.substring(0, 3) + generateMask(maskLength, maskChar) + address.substring(address.length() - 1);
    }

    /**
     * 车牌号脱敏
     * 规则：保留前2位和后2位，中间用***替换
     * 例如：京A12345 -> 京A***45
     */
    public static String desensitizeLicensePlate(String licensePlate) {
        return desensitizeLicensePlate(licensePlate, MASK);
    }

    public static String desensitizeLicensePlate(String licensePlate, char maskChar) {
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
        return licensePlate.substring(0, 2) + generateMask(maskLength, maskChar) +
                licensePlate.substring(licensePlate.length() - 2);
    }

    /**
     * IPv4地址脱敏
     * 规则：保留前两段，后两段用***替换
     * 例如：192.168.1.100 -> 192.168.***.***
     */
    public static String desensitizeIpv4(String ipv4) {
        return desensitizeIpv4(ipv4, MASK);
    }

    public static String desensitizeIpv4(String ipv4, char maskChar) {
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

        String maskSegment = generateMask(3, maskChar);
        return parts[0] + "." + parts[1] + "." + maskSegment + "." + maskSegment;
    }

    public static String desensitizeAll(String address) {
        return desensitizeAll(address, MASK);
    }

    public static String desensitizeAll(String address, char maskChar) {
        if (!StringUtils.hasText(address)) {
            return address;
        }
        return desensitizeCustom(address, 0, 0, maskChar);
    }

    public static String desensitizeCustom(String value, int prefixKeep, int suffixKeep, char maskChar) {
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
     * 生成智能脱敏符号
     * <p>
     * 核心优化：限制脱敏符号的最大长度，避免过长的脱敏符号影响显示效果
     * 同时减少信息泄露（过多的*号可能暴露原始数据长度）
     * </p>
     *
     * @param length   需要脱敏的长度
     * @param maskChar 脱敏字符
     * @return 智能生成的脱敏符号
     */
    private static String generateMask(int length, char maskChar) {
        if (length <= 0) {
            return "";
        }

        // 核心优化：限制最大脱敏符号数量
        int actualLength = Math.min(length, MAX_MASK_LENGTH);

        return String.valueOf(maskChar).repeat(actualLength);
    }

    /**
     * 生成智能脱敏符号（使用默认脱敏字符）
     */
    private static String generateMask(int length) {
        return generateMask(length, MASK);
    }
}