package com.zesheng.common.model.response.recyclemarket;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 行情历史记录（小程序/对外开放查询）
 */
@Data
public class RecycleMarketPriceHistoryVo {

    private LocalDate priceDate;
    private BigDecimal price;
}
