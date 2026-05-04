package com.zesheng.common.model.response.recyclemarket;

import lombok.Data;

import java.util.List;

/**
 * 行情二级分类（小程序/对外开放查询）
 */
@Data
public class RecycleMarketSubCategoryVo {

    private Long id;
    private Long level1Id;
    private String name;
    private Integer sortOrder;
    private List<RecycleMarketThirdCategoryVo> children;
}
