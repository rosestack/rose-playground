package io.github.rosestack.audit.util;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.rosestack.core.jackson.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计JSON处理工具类
 * <p>
 * 基于 JsonUtils 的适配器，提供审计特定的JSON处理功能。
 * 主要用于敏感数据脱敏处理。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
public final class AuditJsonUtils {

    private AuditJsonUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 将对象转换为JSON字符串
     * 直接委托给 JsonUtils
     *
     * @param object 待转换的对象
     * @return JSON字符串
     */
    public static String toJsonString(Object object) {
        return JsonUtils.toString(object);
    }

    /**
     * 将对象转换为JSON字符串并进行脱敏处理
     *
     * @param object 待转换的对象
     * @return 脱敏后的JSON字符串
     */
    public static String toMaskedJsonString(Object object) {
        if (object == null) {
            return null;
        }

        try {
            String jsonString = JsonUtils.toString(object);
            if (!StringUtils.hasText(jsonString)) {
                return jsonString;
            }

            // 解析JSON并进行脱敏
            Map<String, Object> dataMap = JsonUtils.fromString(jsonString, new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> maskedMap = maskJsonData(dataMap);
            return JsonUtils.toString(maskedMap);
        } catch (Exception e) {
            log.error("对象转脱敏JSON失败: {}", e.getMessage(), e);
            return JsonUtils.toString(object);
        }
    }

    /**
     * 从JSON字符串解析为指定类型的对象
     * 直接委托给 JsonUtils
     *
     * @param jsonString JSON字符串
     * @param clazz      目标类型
     * @param <T>        泛型类型
     * @return 解析后的对象
     */
    public static <T> T fromJsonString(String jsonString, Class<T> clazz) {
        return JsonUtils.fromString(jsonString, clazz);
    }

    /**
     * 从JSON字符串解析为指定类型的对象
     * 直接委托给 JsonUtils
     *
     * @param jsonString    JSON字符串
     * @param typeReference 类型引用
     * @param <T>           泛型类型
     * @return 解析后的对象
     */
    public static <T> T fromJsonString(String jsonString, TypeReference<T> typeReference) {
        return JsonUtils.fromString(jsonString, typeReference);
    }


    /**
     * 对JSON数据进行脱敏处理
     *
     * @param dataMap    数据映射
     * @return 脱敏后的数据映射
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> maskJsonData(Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }

        Map<String, Object> maskedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String maskedValue = AuditMaskingUtils.maskByFieldName(key, (String) value);
                maskedMap.put(key, maskedValue);
            } else if (value instanceof Map) {
                Map<String, Object> nestedMap = maskJsonData((Map<String, Object>) value);
                maskedMap.put(key, nestedMap);
            } else if (value instanceof List) {
                List<Object> maskedList = maskJsonList((List<Object>) value);
                maskedMap.put(key, maskedList);
            } else {
                maskedMap.put(key, value);
            }
        }

        return maskedMap;
    }

    /**
     * 对JSON列表进行脱敏处理
     */
    @SuppressWarnings("unchecked")
    private static List<Object> maskJsonList(List<Object> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }

        List<Object> maskedList = new ArrayList<>();

        for (Object item : dataList) {
            if (item instanceof Map) {
                Map<String, Object> maskedMap = maskJsonData((Map<String, Object>) item);
                maskedList.add(maskedMap);
            } else if (item instanceof List) {
                List<Object> maskedSubList = maskJsonList((List<Object>) item);
                maskedList.add(maskedSubList);
            } else {
                maskedList.add(item);
            }
        }

        return maskedList;
    }
}