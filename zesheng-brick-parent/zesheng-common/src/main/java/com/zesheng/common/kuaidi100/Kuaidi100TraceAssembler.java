package com.zesheng.common.kuaidi100;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zesheng.common.dto.logistics.LogisticsTraceItemVo;
import com.zesheng.common.dto.logistics.LogisticsTraceVo;
import com.zesheng.common.kuaidi100.dto.Kuaidi100PollQueryDto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 将快递100 poll 接口 JSON 转为前端可用的物流结构（使用泛型 DTO 解析 data 数组，与官方返回一致）
 */
public final class Kuaidi100TraceAssembler {

    private static final Gson GSON = new Gson();

    private Kuaidi100TraceAssembler() {
    }

    /**
     * 解析 poll 查询接口返回的完整 JSON 字符串（poll.kuaidi100.com 实时查询）
     */
    public static LogisticsTraceVo fromPollResponseJson(String json, String trackingNo, String resolvedComCode) {
        if (StringUtils.isBlank(json)) {
            return LogisticsTraceVo.fail("快递100无返回");
        }
        Kuaidi100PollQueryDto dto;
        try {
            dto = GSON.fromJson(json, Kuaidi100PollQueryDto.class);
        } catch (Exception e) {
            return LogisticsTraceVo.fail("物流数据解析失败");
        }
        if (dto == null) {
            return LogisticsTraceVo.fail("快递100无返回");
        }

        LogisticsTraceVo vo = new LogisticsTraceVo();
        vo.setTrackingNo(StringUtils.defaultIfBlank(trackingNo, dto.getNu()));
        vo.setComCode(StringUtils.isNotBlank(resolvedComCode) ? resolvedComCode : dto.getCom());
        vo.setState(dto.getState());
        vo.setStateText(stateToText(dto.getState()));

        List<LogisticsTraceItemVo> traces = parseTraceList(dto.getData());
        vo.setTraces(traces);
        if (!traces.isEmpty()) {
            LogisticsTraceItemVo latest = traces.get(0);
            vo.setLastTime(latest.getTime());
            vo.setLastInfo(latest.getContext());
        } else if (StringUtils.isNotBlank(dto.getState())) {
            vo.setLastInfo("当前状态：" + stateToText(dto.getState()));
        }

        boolean hasTraces = !traces.isEmpty();
        // 官方接口通讯成功为 status=200；result 字段可能缺失
        boolean pollCommOk = "200".equals(StringUtils.trimToEmpty(dto.getStatus()));
        boolean resultFlag = Boolean.TRUE.equals(dto.getResult());
        boolean apiOk = hasTraces || pollCommOk || resultFlag || StringUtils.isNotBlank(dto.getNu());
        vo.setSuccess(apiOk);
        if (!apiOk) {
            String msg = StringUtils.firstNonBlank(dto.getMessage(),
                    dto.getReturnCode(),
                    "暂无物流信息");
            if (isNoiseMessage(msg)) {
                msg = "暂无物流轨迹";
            }
            vo.setErrorMessage(msg);
        }
        return vo;
    }

    private static boolean isNoiseMessage(String msg) {
        if (msg == null) {
            return true;
        }
        String t = msg.trim();
        return t.isEmpty() || "ok".equalsIgnoreCase(t);
    }

    private static List<LogisticsTraceItemVo> parseTraceList(Object data) {
        List<LogisticsTraceItemVo> out = new ArrayList<>();
        Object normalized = data;
        if (data instanceof String s && StringUtils.isNotBlank(s)) {
            try {
                normalized = GSON.fromJson(s.trim(), new TypeToken<List<Map<String, Object>>>() { }.getType());
            } catch (Exception e) {
                normalized = null;
            }
        }
        if (!(normalized instanceof List<?> list) || list.isEmpty()) {
            return out;
        }
        for (Object o : list) {
            if (o instanceof Map<?, ?> m) {
                String time = firstString(m, "ftime", "time", "cTime");
                String ctx = firstString(m, "context", "desc", "status", "areaName");
                out.add(new LogisticsTraceItemVo(
                        time != null ? time : "",
                        ctx != null ? ctx : ""));
            }
        }
        return out;
    }

    private static String firstString(Map<?, ?> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && StringUtils.isNotBlank(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }

    /**
     * 快递100 state 字段含义
     */
    public static String stateToText(String state) {
        if (state == null || state.isEmpty()) {
            return "未知";
        }
        String s = state.trim();
        return switch (s) {
            case "0" -> "在途";
            case "1" -> "揽收";
            case "2" -> "疑难";
            case "3" -> "签收";
            case "4" -> "退签";
            case "5" -> "派件";
            case "6" -> "退回";
            case "7" -> "转投";
            case "10" -> "待清关";
            case "11" -> "清关中";
            case "12" -> "已清关";
            case "13" -> "清关异常";
            case "14" -> "拒签";
            default -> "物流状态(" + s + ")";
        };
    }

    public static String normalizeComCode(String code) {
        if (code == null) {
            return "";
        }
        return code.trim().toLowerCase(Locale.ROOT);
    }
}
