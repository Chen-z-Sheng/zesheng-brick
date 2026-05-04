package com.zesheng.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按日统计项（首页图表）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按日统计")
public class DashboardDayCountItem {

    @Schema(description = "横轴标签，如 MM-dd")
    private String label;

    @Schema(description = "当日数量")
    private long count;
}
