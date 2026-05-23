package com.zesheng.common.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 快递单号列表规范化与库内逗号分隔存储
 */
public final class ExpressNoSupport {

    private static final int MAX_COUNT = 20;
    private static final String STORAGE_DELIMITER = ",";

    private ExpressNoSupport() {
    }

    /**
     * 固结报单 dataJson 读取 expressNos
     */
    @SuppressWarnings("unchecked")
    public static List<String> readExpressNosFromFormJson(Map<String, Object> dataJson) {
        if (dataJson == null || dataJson.isEmpty()) {
            return Collections.emptyList();
        }
        Object nosObj = dataJson.get("expressNos");
        return normalizeInputList(toStringList(nosObj));
    }

    /**
     * 固结报单 dataJson 写入 expressNos
     */
    public static void writeExpressNosToFormJson(Map<String, Object> dataJson, List<String> expressNos) {
        if (dataJson == null) {
            return;
        }
        List<String> normalized = normalizeInputList(expressNos);
        dataJson.remove("expressNo");
        if (normalized.isEmpty()) {
            dataJson.remove("expressNos");
            return;
        }
        dataJson.put("expressNos", normalized);
    }

    /**
     * 行情报单提交 logistics 节点读取 nos
     */
    @SuppressWarnings("unchecked")
    public static List<String> readNosFromSellLogistics(Map<String, Object> logistics) {
        if (logistics == null || logistics.isEmpty()) {
            return Collections.emptyList();
        }
        return normalizeInputList(toStringList(logistics.get("nos")));
    }

    /**
     * 库字段 logistics_no：逗号分隔 ↔ 列表
     */
    public static List<String> splitStored(String stored) {
        if (!StringUtils.hasText(stored)) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String part : stored.split(STORAGE_DELIMITER)) {
            String no = part != null ? part.trim() : "";
            if (StringUtils.hasText(no)) {
                set.add(no);
            }
        }
        return limit(new ArrayList<>(set));
    }

    public static String joinStored(List<String> expressNos) {
        List<String> normalized = normalizeInputList(expressNos);
        if (normalized.isEmpty()) {
            return "";
        }
        return String.join(STORAGE_DELIMITER, normalized);
    }

    public static List<String> normalizeInputList(List<String> expressNos) {
        if (expressNos == null || expressNos.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String no : expressNos) {
            if (!StringUtils.hasText(no)) {
                continue;
            }
            set.add(no.trim());
        }
        return limit(new ArrayList<>(set));
    }

    public static String formatDisplay(List<String> expressNos) {
        List<String> normalized = normalizeInputList(expressNos);
        if (normalized.isEmpty()) {
            return "";
        }
        return String.join("、", normalized);
    }

    private static List<String> toStringList(Object nosObj) {
        if (!(nosObj instanceof Collection<?> collection) || collection.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Object item : collection) {
            if (item == null) {
                continue;
            }
            String no = String.valueOf(item).trim();
            if (StringUtils.hasText(no)) {
                set.add(no);
            }
        }
        return new ArrayList<>(set);
    }

    private static List<String> limit(List<String> list) {
        if (list.size() <= MAX_COUNT) {
            return list;
        }
        return new ArrayList<>(list.subList(0, MAX_COUNT));
    }
}
