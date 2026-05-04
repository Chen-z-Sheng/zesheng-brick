package com.zesheng.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * 系统配置 value 按 value_type 解析；库中存文本，业务层据此转为强类型
 */
public final class ConfigValueParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConfigValueParser() {
    }

    /**
     * 统一类型码：历史 bool 与 boolean 等价
     */
    public static String normalizeValueType(String valueType) {
        if (valueType == null || valueType.isBlank()) {
            return "json";
        }
        if ("bool".equalsIgnoreCase(valueType.trim())) {
            return "boolean";
        }
        return valueType.trim();
    }

    /**
     * 按类型解析配置原文
     *
     * @return json→JsonNode，string→String，number→BigDecimal，boolean→Boolean
     */
    public static Object parse(String raw, String valueType) throws JsonProcessingException {
        if (raw == null) {
            return null;
        }
        String vt = normalizeValueType(valueType);
        String text = raw;
        return switch (vt) {
            case "json" -> MAPPER.readTree(text.trim());
            case "string" -> raw;
            case "number" -> new BigDecimal(text.trim());
            case "boolean" -> parseBooleanLiteral(text);
            default -> throw new IllegalArgumentException("未知配置值类型: " + valueType);
        };
    }

    /**
     * 解析为 JsonNode（仅 value_type 为 json 时合法）
     */
    public static JsonNode parseAsJsonNode(String raw, String valueType) throws JsonProcessingException {
        if (!"json".equals(normalizeValueType(valueType))) {
            throw new IllegalArgumentException("当前配置不是 json 类型");
        }
        return MAPPER.readTree(raw == null ? "null" : raw.trim());
    }

    private static Boolean parseBooleanLiteral(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(s)) {
            return Boolean.TRUE;
        }
        if ("false".equals(s)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("boolean 类型配置值只能是 true 或 false");
    }
}
