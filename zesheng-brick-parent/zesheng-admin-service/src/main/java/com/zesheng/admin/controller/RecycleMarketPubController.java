package com.zesheng.admin.controller;

import com.zesheng.admin.service.IRecycleMarketClientQueryService;
import com.zesheng.common.model.response.recyclemarket.*;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 回收行情开放查询（无需登录，供 C 端服务转发/内网调用）
 */
@Validated
@RestController
@RequestMapping("/pub/recycle-market")
@RequiredArgsConstructor
@Tag(name = "回收行情开放接口", description = "管理端数据域，匿名只读")
public class RecycleMarketPubController {

    private final IRecycleMarketClientQueryService clientQueryService;

    @GetMapping("/categories")
    @Operation(summary = "一级分类列表（含二级分类树）")
    public R<List<RecycleMarketCategoryVo>> listCategories() {
        return R.ok(clientQueryService.listCategories());
    }

    @GetMapping("/sub-categories")
    @Operation(summary = "二级分类列表（按一级分类）")
    public R<List<RecycleMarketSubCategoryVo>> listSubCategories(
            @RequestParam @NotNull(message = "一级分类不能为空") Long level1Id) {
        return R.ok(clientQueryService.listSubCategories(level1Id));
    }

    @GetMapping("/third-categories")
    @Operation(summary = "三级分类列表（按二级分类）")
    public R<List<RecycleMarketThirdCategoryVo>> listThirdCategories(
            @RequestParam @NotNull(message = "二级分类不能为空") Long level2Id) {
        return R.ok(clientQueryService.listThirdCategories(level2Id));
    }

    @GetMapping("/products")
    @Operation(summary = "商品列表（二级分类 + 各自最新行情）")
    public R<List<RecycleMarketProductVo>> listProducts(
            @RequestParam(required = false) Long level1Id,
            @RequestParam(required = false) Long level2Id,
            @RequestParam(required = false) Long level3Id,
            @RequestParam(required = false) LocalDate priceDate) {
        return R.ok(clientQueryService.listProducts(level1Id, level2Id, level3Id, priceDate));
    }

    @GetMapping("/product/detail")
    @Operation(summary = "商品详情")
    public R<RecycleMarketProductVo> getProductDetail(
            @RequestParam(required = false) Long level3Id,
            @RequestParam(required = false) LocalDate priceDate) {
        return R.ok(clientQueryService.getProductDetail(level3Id, priceDate));
    }

    @GetMapping("/price-history")
    @Operation(summary = "价格历史")
    public R<List<RecycleMarketPriceHistoryVo>> getPriceHistory(
            @RequestParam(required = false) Long level3Id,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "limit至少为1") @Max(value = 200, message = "limit不能超过200") int limit) {
        return R.ok(clientQueryService.getPriceHistory(level3Id, limit));
    }
}
