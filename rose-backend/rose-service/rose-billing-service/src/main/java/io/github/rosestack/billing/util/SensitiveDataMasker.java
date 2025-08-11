package io.github.rosestack.billing.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class SensitiveDataMasker {
    private static final ObjectMapper M = new ObjectMapper();

    private SensitiveDataMasker() {}

    public static String mask(String rawJson) {
        if (rawJson == null) return null;
        try {
            JsonNode node = M.readTree(rawJson);
            maskNode(node);
            return M.writeValueAsString(node);
        } catch (Exception e) {
            // 回退：仅隐藏 signature 文本，避免编译期复杂转义
            return rawJson.replaceAll("\\\"signature\\\"\\s*:\\s*\\\"[^\\\"]*\\\"", "\\\"signature\\\":\\\"***\\\"");
        }
    }

    private static void maskNode(JsonNode node) {
        if (node == null) return;
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            obj.fieldNames().forEachRemaining(field -> {
                JsonNode val = obj.get(field);
                if (val == null) return;
                String f = field.toLowerCase();
                if (val.isTextual()) {
                    String text = val.asText();
                    if ("cardnumber".equals(f) || "pan".equals(f)) {
                        obj.put(field, maskPan(text));
                    } else if ("phone".equals(f) || "mobile".equals(f)) {
                        obj.put(field, maskPhone(text));
                    } else if ("email".equals(f)) {
                        obj.put(field, maskEmail(text));
                    } else if ("signature".equals(f) || "sign".equals(f) || "token".equals(f)) {
                        obj.put(field, "***");
                    } else {
                        // 保持原值
                    }
                } else {
                    // 递归处理对象/数组
                    maskNode(val);
                }
            });
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < arr.size(); i++) {
                maskNode(arr.get(i));
            }
        }
    }

    private static String maskPan(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D", "");
        if (digits.length() < 10) return "***";
        String prefix = digits.substring(0, 6);
        String suffix = digits.substring(digits.length() - 4);
        return prefix + "******" + suffix;
    }

    private static String maskPhone(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D", "");
        if (digits.length() < 7) return "***";
        return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
    }

    private static String maskEmail(String s) {
        if (s == null) return null;
        int at = s.indexOf('@');
        if (at <= 1) return "***@";
        String name = s.substring(0, at);
        String domain = s.substring(at);
        String left = name.length() <= 3 ? name.substring(0, 1) : name.substring(0, 3);
        return left + "***" + domain;
    }
}

