package com.zesheng.client.service;

import com.zesheng.common.model.response.recyclemarket.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 行情 Service（客户端/小程序）
 */
public interface IRecycleMarketService {

    /**
     * 一级分类列表（含二级分类树）
     */
    List<RecycleMarketCategoryVo> listCategories();

    /**
     * 二级分类列表（按一级分类）
     */
    List<RecycleMarketSubCategoryVo> listSubCategories(Long level1Id);

    List<RecycleMarketThirdCategoryVo> listThirdCategories(Long level2Id);

    /**
     * 商品列表（二级分类 + 各自最新行情）
     * 每个商品显示该二级分类下最新一条行情（按 price_date 取最大），不要求同一天有数据。
     * 当 level1Id 有值时返回该一级下所有二级；当 level2Id 有值时只返回该二级。
     */
    List<RecycleMarketProductVo> listProducts(Long level1Id, Long level2Id, Long level3Id, LocalDate priceDate);

    RecycleMarketProductVo getProductDetail(Long level3Id, LocalDate priceDate);

    List<RecycleMarketPriceHistoryVo> getPriceHistory(Long level3Id, int limit);
}
