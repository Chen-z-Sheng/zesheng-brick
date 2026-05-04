package com.zesheng.common.dto.logistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单列表展示的物流摘要
 */
@Data
@Schema(description = "物流摘要")
public class LogisticsSummaryVo {

    private boolean success;

    private String stateText;

    private String lastInfo;

    private String lastTime;

    private String errorMessage;
}
