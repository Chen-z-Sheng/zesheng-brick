package com.zesheng.client.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 我的订单统计（固结+行情合并，按 tab 汇总）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单统计")
public class OrderStatsVo {

    @Schema(description = "全部订单数（排除草稿）")
    private long total;

    @Schema(description = "已寄出（1,2）")
    private long shipped;

    @Schema(description = "已签收（7）")
    private long signed;

    @Schema(description = "运输中（1,2,7）")
    private long transit;

    @Schema(description = "待入库（3）")
    private long storing;

    @Schema(description = "已完成（4）")
    private long completed;

    @Schema(description = "异常（5、6）")
    private long exception;
}
