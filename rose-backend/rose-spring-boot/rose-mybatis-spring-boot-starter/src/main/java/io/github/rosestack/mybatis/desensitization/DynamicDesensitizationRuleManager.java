package io.github.rosestack.mybatis.desensitization;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 动态脱敏规则管理器
 * <p>
 * 支持运行时配置和修改脱敏规则，包括：
 * 1. 动态脱敏规则配置
 * 2. 角色相关脱敏
 * 3. 地域相关脱敏
 * 4. 时间相关脱敏
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class DynamicDesensitizationRuleManager {

    /**
     * 动态脱敏规则存储
     */
    private final Map<String, DynamicDesensitizationRule> rules = new ConcurrentHashMap<>();
    
    /**
     * 角色脱敏配置
     */
    private final Map<String, RoleDesensitizationConfig> roleConfigs = new ConcurrentHashMap<>();
    
    /**
     * 地域脱敏配置
     */
    private final Map<String, RegionDesensitizationConfig> regionConfigs = new ConcurrentHashMap<>();
    
    /**
     * 内置脱敏函数
     */
    private final Map<String, Function<String, String>> builtinFunctions = new ConcurrentHashMap<>();

    /**
     * 支持参数的脱敏函数
     */
    private final Map<String, BiFunction<String, String, String>> parametricFunctions = new ConcurrentHashMap<>();

    public DynamicDesensitizationRuleManager() {
        initBuiltinFunctions();
        initParametricFunctions();
        initDefaultRoleConfigs();
        initDefaultRegionConfigs();
    }

    /**
     * 添加动态脱敏规则
     */
    public void addRule(String ruleId, String fieldPattern, String rolePattern,
                       String regionPattern, String desensitizationFunction, int priority) {
        addRule(ruleId, fieldPattern, rolePattern, regionPattern, desensitizationFunction, null, priority);
    }

    /**
     * 添加动态脱敏规则（支持函数参数）
     */
    public void addRule(String ruleId, String fieldPattern, String rolePattern,
                       String regionPattern, String desensitizationFunction, String functionParams, int priority) {

        DynamicDesensitizationRule rule = new DynamicDesensitizationRule();
        rule.setRuleId(ruleId);
        rule.setFieldPattern(Pattern.compile(fieldPattern));
        rule.setRolePattern(rolePattern != null ? Pattern.compile(rolePattern) : null);
        rule.setRegionPattern(regionPattern != null ? Pattern.compile(regionPattern) : null);
        rule.setDesensitizationFunction(desensitizationFunction);
        rule.setFunctionParams(functionParams);
        rule.setPriority(priority);
        rule.setEnabled(true);
        rule.setCreatedTime(LocalDateTime.now());

        rules.put(ruleId, rule);
        log.info("添加动态脱敏规则: {}, 函数: {}, 参数: {}", ruleId, desensitizationFunction, functionParams);
    }

    /**
     * 根据上下文获取脱敏规则
     */
    public String applyDesensitization(String fieldName, String originalValue, DesensitizationContext context) {
        // 获取匹配的规则
        List<DynamicDesensitizationRule> matchedRules = getMatchedRules(fieldName, context);
        
        if (matchedRules.isEmpty()) {
            return originalValue; // 无匹配规则，返回原值
        }
        
        // 按优先级排序，取最高优先级的规则
        DynamicDesensitizationRule rule = matchedRules.stream()
                .filter(DynamicDesensitizationRule::isEnabled)
                .min(Comparator.comparingInt(DynamicDesensitizationRule::getPriority))
                .orElse(null);
        
        if (rule == null) {
            return originalValue;
        }
        
        // 应用脱敏函数
        String desensitizedValue = applyDesensitizationFunction(rule.getDesensitizationFunction(), originalValue, context, rule.getFunctionParams());
        
        log.debug("应用脱敏规则: 字段={}, 规则={}, 用户角色={}, 地域={}", 
                fieldName, rule.getRuleId(), context.getUserRole(), context.getRegion());
        
        return desensitizedValue;
    }

    /**
     * 获取匹配的规则
     */
    private List<DynamicDesensitizationRule> getMatchedRules(String fieldName, DesensitizationContext context) {
        List<DynamicDesensitizationRule> matched = new ArrayList<>();
        
        for (DynamicDesensitizationRule rule : rules.values()) {
            if (!rule.isEnabled()) {
                continue;
            }
            
            // 检查字段匹配
            if (!rule.getFieldPattern().matcher(fieldName).matches()) {
                continue;
            }
            
            // 检查角色匹配
            if (rule.getRolePattern() != null && context.getUserRole() != null) {
                if (!rule.getRolePattern().matcher(context.getUserRole()).matches()) {
                    continue;
                }
            }
            
            // 检查地域匹配
            if (rule.getRegionPattern() != null && context.getRegion() != null) {
                if (!rule.getRegionPattern().matcher(context.getRegion()).matches()) {
                    continue;
                }
            }
            
            matched.add(rule);
        }
        
        return matched;
    }

    /**
     * 应用脱敏函数
     */
    private String applyDesensitizationFunction(String functionName, String originalValue, DesensitizationContext context) {
        return applyDesensitizationFunction(functionName, originalValue, context, null);
    }

    /**
     * 应用脱敏函数（支持参数）
     */
    private String applyDesensitizationFunction(String functionName, String originalValue, DesensitizationContext context, String functionParams) {
        // 优先尝试参数化函数
        if (functionParams != null && parametricFunctions.containsKey(functionName)) {
            BiFunction<String, String, String> parametricFunction = parametricFunctions.get(functionName);
            return parametricFunction.apply(originalValue, functionParams);
        }

        // 尝试普通函数
        Function<String, String> function = builtinFunctions.get(functionName);
        if (function != null) {
            return function.apply(originalValue);
        }

        // 角色相关脱敏
        RoleDesensitizationConfig roleConfig = roleConfigs.get(context.getUserRole());
        if (roleConfig != null) {
            return applyRoleBasedDesensitization(originalValue, roleConfig, functionName);
        }

        // 地域相关脱敏
        RegionDesensitizationConfig regionConfig = regionConfigs.get(context.getRegion());
        if (regionConfig != null) {
            return applyRegionBasedDesensitization(originalValue, regionConfig, functionName);
        }

        log.warn("未找到脱敏函数: {}", functionName);
        return originalValue;
    }

    /**
     * 应用基于角色的脱敏
     */
    private String applyRoleBasedDesensitization(String originalValue, RoleDesensitizationConfig config, String functionName) {
        // 检查角色是否为管理员
        if ("ADMIN".equals(config.getRoleName())) {
            return originalValue; // 管理员不脱敏
        }

        switch (config.getDesensitizationLevel()) {
            case NONE:
                return originalValue;
            case PARTIAL:
                return applyPartialDesensitization(originalValue, config.getVisibleRatio());
            case FULL:
                return applyFullDesensitization(originalValue);
            case CUSTOM:
                Function<String, String> customFunction = builtinFunctions.get(functionName);
                return customFunction != null ? customFunction.apply(originalValue) : originalValue;
            default:
                return originalValue;
        }
    }

    /**
     * 应用基于地域的脱敏
     */
    private String applyRegionBasedDesensitization(String originalValue, RegionDesensitizationConfig config, String functionName) {
        // 根据地域法规要求进行脱敏
        if (config.isStrictMode()) {
            return applyFullDesensitization(originalValue);
        } else {
            return applyPartialDesensitization(originalValue, 0.5);
        }
    }

    /**
     * 部分脱敏
     */
    private String applyPartialDesensitization(String value, double visibleRatio) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        int visibleLength = (int) (value.length() * visibleRatio);
        int maskLength = value.length() - visibleLength;
        
        if (visibleLength <= 0) {
            return "*".repeat(value.length());
        }
        
        String visiblePart = value.substring(0, visibleLength);
        String maskPart = "*".repeat(maskLength);
        
        return visiblePart + maskPart;
    }

    /**
     * 完全脱敏
     */
    private String applyFullDesensitization(String value) {
        return value != null ? "*".repeat(value.length()) : null;
    }

    /**
     * 初始化内置脱敏函数
     * 复用 SensitiveDataProcessor 中的静态方法，避免代码重复
     */
    private void initBuiltinFunctions() {
        // 姓名脱敏 - 复用 SensitiveDataProcessor.desensitizeName
        builtinFunctions.put("name", SensitiveDataProcessor::desensitizeName);

        // 手机号脱敏 - 复用 SensitiveDataProcessor.desensitizePhone
        builtinFunctions.put("phone", SensitiveDataProcessor::desensitizePhone);

        // 邮箱脱敏 - 复用 SensitiveDataProcessor.desensitizeEmail
        builtinFunctions.put("email", SensitiveDataProcessor::desensitizeEmail);

        // 身份证脱敏 - 复用 SensitiveDataProcessor.desensitizeIdCard
        builtinFunctions.put("idCard", SensitiveDataProcessor::desensitizeIdCard);

        // 银行卡脱敏 - 复用 SensitiveDataProcessor.desensitizeBankCard
        builtinFunctions.put("bankCard", SensitiveDataProcessor::desensitizeBankCard);

        // 地址脱敏 - 复用 SensitiveDataProcessor.desensitizeAddress
        builtinFunctions.put("address", SensitiveDataProcessor::desensitizeAddress);

        // 自定义脱敏 - 使用默认规则
        builtinFunctions.put("custom", value -> SensitiveDataProcessor.desensitizeCustom(value, "3,4"));
    }

    /**
     * 初始化支持参数的脱敏函数
     * 这些函数可以接受额外的参数来自定义脱敏行为
     */
    private void initParametricFunctions() {
        // 自定义脱敏 - 支持传递规则参数
        parametricFunctions.put("custom", SensitiveDataProcessor::desensitizeCustom);

        // 可以添加更多支持参数的脱敏函数
        // 例如：部分脱敏，可以指定保留的字符数
        parametricFunctions.put("partial", (value, rule) -> {
            if (value == null || value.isEmpty()) return value;
            try {
                int keepChars = Integer.parseInt(rule);
                if (keepChars >= value.length()) return value;
                return value.substring(0, keepChars) + "*".repeat(value.length() - keepChars);
            } catch (NumberFormatException e) {
                return SensitiveDataProcessor.desensitizeCustom(value, rule);
            }
        });
    }

    /**
     * 初始化默认角色配置
     */
    private void initDefaultRoleConfigs() {
        // 管理员 - 无脱敏
        RoleDesensitizationConfig adminConfig = new RoleDesensitizationConfig();
        adminConfig.setRoleName("ADMIN");
        adminConfig.setDesensitizationLevel(DesensitizationLevel.NONE);
        adminConfig.setVisibleRatio(1.0);
        roleConfigs.put("ADMIN", adminConfig);
        
        // 普通用户 - 部分脱敏
        RoleDesensitizationConfig userConfig = new RoleDesensitizationConfig();
        userConfig.setRoleName("USER");
        userConfig.setDesensitizationLevel(DesensitizationLevel.PARTIAL);
        userConfig.setVisibleRatio(0.3);
        roleConfigs.put("USER", userConfig);
        
        // 访客 - 完全脱敏
        RoleDesensitizationConfig guestConfig = new RoleDesensitizationConfig();
        guestConfig.setRoleName("GUEST");
        guestConfig.setDesensitizationLevel(DesensitizationLevel.FULL);
        guestConfig.setVisibleRatio(0.0);
        roleConfigs.put("GUEST", guestConfig);
    }

    /**
     * 初始化默认地域配置
     */
    private void initDefaultRegionConfigs() {
        // 欧盟 - 严格模式（GDPR）
        RegionDesensitizationConfig euConfig = new RegionDesensitizationConfig();
        euConfig.setRegionCode("EU");
        euConfig.setRegionName("European Union");
        euConfig.setStrictMode(true);
        euConfig.setRegulation("GDPR");
        regionConfigs.put("EU", euConfig);
        
        // 中国 - 标准模式
        RegionDesensitizationConfig cnConfig = new RegionDesensitizationConfig();
        cnConfig.setRegionCode("CN");
        cnConfig.setRegionName("China");
        cnConfig.setStrictMode(false);
        cnConfig.setRegulation("PIPL");
        regionConfigs.put("CN", cnConfig);
        
        // 美国 - 标准模式
        RegionDesensitizationConfig usConfig = new RegionDesensitizationConfig();
        usConfig.setRegionCode("US");
        usConfig.setRegionName("United States");
        usConfig.setStrictMode(false);
        usConfig.setRegulation("CCPA");
        regionConfigs.put("US", usConfig);
    }

    /**
     * 更新规则状态
     */
    public void updateRuleStatus(String ruleId, boolean enabled) {
        DynamicDesensitizationRule rule = rules.get(ruleId);
        if (rule != null) {
            rule.setEnabled(enabled);
            rule.setUpdatedTime(LocalDateTime.now());
            log.info("更新脱敏规则状态: {} -> {}", ruleId, enabled);
        }
    }

    /**
     * 删除规则
     */
    public void removeRule(String ruleId) {
        rules.remove(ruleId);
        log.info("删除脱敏规则: {}", ruleId);
    }

    /**
     * 获取所有规则
     */
    public Map<String, DynamicDesensitizationRule> getAllRules() {
        return new HashMap<>(rules);
    }

    /**
     * 动态脱敏规则
     */
    @Data
    public static class DynamicDesensitizationRule {
        private String ruleId;
        private Pattern fieldPattern;
        private Pattern rolePattern;
        private Pattern regionPattern;
        private String desensitizationFunction;
        private String functionParams;  // 脱敏函数参数
        private int priority;
        private boolean enabled;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
    }

    /**
     * 脱敏上下文
     */
    @Data
    public static class DesensitizationContext {
        private String userId;
        private String userRole;
        private String region;
        private String ipAddress;
        private LocalDateTime accessTime;
        private Map<String, Object> additionalContext;
    }

    /**
     * 角色脱敏配置
     */
    @Data
    public static class RoleDesensitizationConfig {
        private String roleName;
        private DesensitizationLevel desensitizationLevel;
        private double visibleRatio;
        private Set<String> allowedFields;
        private Set<String> forbiddenFields;
    }

    /**
     * 地域脱敏配置
     */
    @Data
    public static class RegionDesensitizationConfig {
        private String regionCode;
        private String regionName;
        private boolean strictMode;
        private String regulation;
        private Set<String> sensitiveFields;
    }

    /**
     * 脱敏级别
     */
    public enum DesensitizationLevel {
        NONE,     // 无脱敏
        PARTIAL,  // 部分脱敏
        FULL,     // 完全脱敏
        CUSTOM    // 自定义脱敏
    }
}
