package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 管理端首页聚合数据
 */
@Data
@Builder
@Schema(description = "首页仪表盘数据")
public class DashboardOverviewResponse {

    @Schema(description = "固结报单：近 7 日每日提交数")
    private List<DashboardDayCountItem> formSubmissionDaily;

    @Schema(description = "固结报单：今日提交数")
    private long formSubmissionToday;

    @Schema(description = "固结报单：昨日提交数")
    private long formSubmissionYesterday;

    @Schema(description = "小程序用户：累计注册（client_user 行数）")
    private long clientUserTotal;

    @Schema(description = "小程序用户：近 7 日每日新增注册")
    private List<DashboardDayCountItem> clientUserRegisterDaily;

    @Schema(description = "小程序用户：今日新增注册")
    private long clientUserRegisterToday;

    @Schema(description = "小程序用户：昨日新增注册")
    private long clientUserRegisterYesterday;

    @Schema(description = "行情报单：近 7 日每日提交数")
    private List<DashboardDayCountItem> sellOrderDaily;

    @Schema(description = "行情报单：今日提交数")
    private long sellOrderToday;

    @Schema(description = "行情报单：昨日提交数")
    private long sellOrderYesterday;

    @Schema(description = "当前服务运行环境指标")
    private DashboardServerRuntimeVO serverRuntime;
}
