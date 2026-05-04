package com.zesheng.admin.service;

import com.zesheng.common.model.response.recyclemarket.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 回收行情 C 端只读查询（数据与维护逻辑归属管理端域）
 */
public interface IRecycleMarketClientQueryService {

    List<RecycleMarketCategoryVo> listCategories();

    List<RecycleMarketSubCategoryVo> listSubCategories(Long level1Id);

    List<RecycleMarketThirdCategoryVo> listThirdCategories(Long level2Id);

    List<RecycleMarketProductVo> listProducts(Long level1Id, Long level2Id, Long level3Id, LocalDate priceDate);

    RecycleMarketProductVo getProductDetail(Long level3Id, LocalDate priceDate);

    List<RecycleMarketPriceHistoryVo> getPriceHistory(Long level3Id, int limit);
}
