package com.zesheng.common.model.response.recyclemarket;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 行情商品（二级分类+价格，小程序/对外开放查询）
 */
@Data
public class RecycleMarketProductVo {

    private Long id;
    private Long level2Id;
    private Long level3Id;
    private String name;
    private BigDecimal price;
    private LocalDate priceDate;
    private String updateDate;
    private String remark;
}
