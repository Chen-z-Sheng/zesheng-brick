package com.zesheng.common.model.response.recyclemarket;

import lombok.Data;

/**
 * 行情三级分类（小程序/对外开放查询）
 */
@Data
public class RecycleMarketThirdCategoryVo {

    private Long id;
    private Long level2Id;
    private String name;
    private Integer sortOrder;
}
