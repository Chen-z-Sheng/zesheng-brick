package com.zesheng.common.dto.logistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 物流查询结果（列表摘要与详情共用结构，详情带全量轨迹）
 */
@Data
@Schema(description = "物流查询结果")
public class LogisticsTraceVo {

    @Schema(description = "是否查询成功")
    private boolean success;

    @Schema(description = "失败原因")
    private String errorMessage;

    @Schema(description = "运单号")
    private String trackingNo;

    @Schema(description = "快递100快递公司编码")
    private String comCode;

    @Schema(description = "快递100状态码")
    private String state;

    @Schema(description = "状态中文描述")
    private String stateText;

    @Schema(description = "最新一条物流描述")
    private String lastInfo;

    @Schema(description = "最新一条时间")
    private String lastTime;

    @Schema(description = "完整轨迹，时间倒序")
    private List<LogisticsTraceItemVo> traces = new ArrayList<>();

    public static LogisticsTraceVo fail(String message) {
        LogisticsTraceVo vo = new LogisticsTraceVo();
        vo.setSuccess(false);
        vo.setErrorMessage(message);
        return vo;
    }

    public LogisticsSummaryVo toSummary() {
        LogisticsSummaryVo s = new LogisticsSummaryVo();
        s.setSuccess(this.success);
        s.setStateText(this.stateText);
        s.setLastInfo(this.lastInfo);
        s.setLastTime(this.lastTime);
        s.setErrorMessage(this.errorMessage);
        return s;
    }
}
