package com.zesheng.common.dto.logistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物流轨迹节点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物流轨迹节点")
public class LogisticsTraceItemVo {

    @Schema(description = "节点时间")
    private String time;

    @Schema(description = "节点描述")
    private String context;
}
