package com.zesheng.common.kuaidi100.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 快递100 poll 实时查询 JSON 结构（与 poll.kuaidi100.com 返回一致，data 必须用泛型 List 才能被 Gson 正确解析）
 */
@Data
public class Kuaidi100PollQueryDto {

    private String message;

    private String nu;

    private String ischeck;

    private String com;

    /**
     * 通讯状态，成功一般为 "200"
     */
    private String status;

    private String state;

    private String condition;

    /**
     * 轨迹节点；必须用 List&lt;Map&gt;，避免 SDK 里 raw List 反序列化后取不到节点
     */
    private List<Map<String, Object>> data;

    private String returnCode;

    /**
     * 部分响应可能不带该字段
     */
    private Boolean result;
}
