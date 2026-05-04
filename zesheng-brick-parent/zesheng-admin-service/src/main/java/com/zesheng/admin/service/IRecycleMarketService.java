package com.zesheng.admin.service;

import com.zesheng.admin.entity.CategoryLevel1;
import com.zesheng.admin.entity.CategoryLevel2;
import com.zesheng.admin.entity.CategoryLevel3;
import com.zesheng.admin.entity.RecyclePrice;
import com.zesheng.admin.model.request.*;
import com.zesheng.admin.model.response.RecyclePricePageResponse;
import com.zesheng.admin.model.response.RecyclePriceVo;
import com.zesheng.common.response.R;

import java.time.LocalDate;
import java.util.List;

/**
 * 行情管理 Service
 */
public interface IRecycleMarketService {

    // ========== 一级分类 ==========
    List<CategoryLevel1> listLevel1();

    R<CategoryLevel1> saveLevel1(CategoryLevel1SaveRequest request);

    R<CategoryLevel1> updateLevel1(Long id, CategoryLevel1UpdateRequest request);

    R<Integer> deleteLevel1(Long id);

    // ========== 二级分类 ==========
    List<CategoryLevel2> listLevel2(Long level1Id);

    R<CategoryLevel2> saveLevel2(CategoryLevel2SaveRequest request);

    R<CategoryLevel2> updateLevel2(Long id, CategoryLevel2UpdateRequest request);

    R<Integer> deleteLevel2(Long id);

    // ========== 三级分类 ==========
    List<CategoryLevel3> listLevel3(Long level2Id);

    R<CategoryLevel3> saveLevel3(CategoryLevel3SaveRequest request);

    R<CategoryLevel3> updateLevel3(Long id, CategoryLevel3UpdateRequest request);

    R<Integer> deleteLevel3(Long id);

    // ========== 行情 ==========
    LocalDate getLatestPriceDate();

    R<RecyclePricePageResponse> pagePrice(RecyclePricePageRequest request);

    R<RecyclePriceVo> getPriceById(Long id);

    R<RecyclePrice> savePrice(RecyclePriceSaveRequest request);

    R<RecyclePrice> updatePrice(Long id, RecyclePriceUpdateRequest request);

    R<Integer> deletePrice(Long id);

    /**
     * 按日期查询当日所有二级分类的回收价格（用于行情报单详情自动填充单价）
     */
    List<RecyclePriceVo> listPricesByDate(LocalDate date);
}
