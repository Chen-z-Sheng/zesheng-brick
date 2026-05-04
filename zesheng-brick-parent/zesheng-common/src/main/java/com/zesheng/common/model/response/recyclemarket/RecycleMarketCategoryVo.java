package com.zesheng.common.model.response.recyclemarket;

import lombok.Data;

import java.util.List;

/**
 * 行情分类（一级+二级树形，小程序/对外开放查询）
 */
@Data
public class RecycleMarketCategoryVo {

    private Long id;
    private String name;
    private Integer sortOrder;
    private List<RecycleMarketSubCategoryVo> children;
}
