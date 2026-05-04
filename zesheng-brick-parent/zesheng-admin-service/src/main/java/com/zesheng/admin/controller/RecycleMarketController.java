package com.zesheng.admin.controller;

import com.zesheng.admin.entity.CategoryLevel1;
import com.zesheng.admin.entity.CategoryLevel2;
import com.zesheng.admin.entity.CategoryLevel3;
import com.zesheng.admin.entity.RecyclePrice;
import com.zesheng.admin.model.request.*;
import com.zesheng.admin.model.response.RecyclePricePageResponse;
import com.zesheng.admin.model.response.RecyclePriceVo;
import com.zesheng.admin.service.IRecycleMarketService;
import com.zesheng.common.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端-行情管理
 */
@Validated
@RestController
@RequestMapping("recycle-market")
@Tag(name = "管理端-行情管理", description = "一级/二级/三级分类、回收行情增删改查")
@RequiredArgsConstructor
public class RecycleMarketController {

    private final IRecycleMarketService recycleMarketService;

    @GetMapping("level1")
    @Operation(summary = "一级分类列表")
    @PreAuthorize("hasAuthority('admin:recycle-market:list')")
    public R<List<CategoryLevel1>> listLevel1() {
        return R.success(recycleMarketService.listLevel1());
    }

    @PostMapping("level1")
    @Operation(summary = "新增一级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:add')")
    public R<CategoryLevel1> saveLevel1(@Validated @RequestBody CategoryLevel1SaveRequest request) {
        return recycleMarketService.saveLevel1(request);
    }

    @PatchMapping("level1/{id}")
    @Operation(summary = "更新一级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:update')")
    public R<CategoryLevel1> updateLevel1(@PathVariable Long id,
                                          @Validated @RequestBody CategoryLevel1UpdateRequest request) {
        return recycleMarketService.updateLevel1(id, request);
    }

    @DeleteMapping("level1/{id}")
    @Operation(summary = "删除一级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:delete')")
    public R<Integer> deleteLevel1(@PathVariable Long id) {
        return recycleMarketService.deleteLevel1(id);
    }

    @GetMapping("level2")
    @Operation(summary = "二级分类列表")
    @PreAuthorize("hasAuthority('admin:recycle-market:list')")
    public R<List<CategoryLevel2>> listLevel2(@RequestParam @NotNull(message = "一级分类不能为空") Long level1Id) {
        return R.success(recycleMarketService.listLevel2(level1Id));
    }

    @PostMapping("level2")
    @Operation(summary = "新增二级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:add')")
    public R<CategoryLevel2> saveLevel2(@Validated @RequestBody CategoryLevel2SaveRequest request) {
        return recycleMarketService.saveLevel2(request);
    }

    @PatchMapping("level2/{id}")
    @Operation(summary = "更新二级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:update')")
    public R<CategoryLevel2> updateLevel2(@PathVariable Long id,
                                         @Validated @RequestBody CategoryLevel2UpdateRequest request) {
        return recycleMarketService.updateLevel2(id, request);
    }

    @DeleteMapping("level2/{id}")
    @Operation(summary = "删除二级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:delete')")
    public R<Integer> deleteLevel2(@PathVariable Long id) {
        return recycleMarketService.deleteLevel2(id);
    }

    @GetMapping("level3")
    @Operation(summary = "三级分类列表")
    @PreAuthorize("hasAuthority('admin:recycle-market:list')")
    public R<List<CategoryLevel3>> listLevel3(@RequestParam @NotNull(message = "二级分类不能为空") Long level2Id) {
        return R.success(recycleMarketService.listLevel3(level2Id));
    }

    @PostMapping("level3")
    @Operation(summary = "新增三级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:add')")
    public R<CategoryLevel3> saveLevel3(@Validated @RequestBody CategoryLevel3SaveRequest request) {
        return recycleMarketService.saveLevel3(request);
    }

    @PatchMapping("level3/{id}")
    @Operation(summary = "更新三级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:update')")
    public R<CategoryLevel3> updateLevel3(@PathVariable Long id,
                                          @Validated @RequestBody CategoryLevel3UpdateRequest request) {
        return recycleMarketService.updateLevel3(id, request);
    }

    @DeleteMapping("level3/{id}")
    @Operation(summary = "删除三级分类")
    @PreAuthorize("hasAuthority('admin:recycle-market:delete')")
    public R<Integer> deleteLevel3(@PathVariable Long id) {
        return recycleMarketService.deleteLevel3(id);
    }

    @GetMapping("price/latest-date")
    @Operation(summary = "获取数据库中最新的行情日期")
    @PreAuthorize("hasAuthority('admin:recycle-market:list')")
    public R<LocalDate> getLatestPriceDate() {
        return R.success(recycleMarketService.getLatestPriceDate());
    }

    @GetMapping("price/page")
    @Operation(summary = "行情分页查询")
    @PreAuthorize("hasAuthority('admin:recycle-market:list')")
    public R<RecyclePricePageResponse> pagePrice(@Validated RecyclePricePageRequest request) {
        return recycleMarketService.pagePrice(request);
    }

    @GetMapping("price/detail/{id}")
    @Operation(summary = "行情详情")
    @PreAuthorize("hasAuthority('admin:recycle-market:list')")
    public R<RecyclePriceVo> getPriceById(@PathVariable Long id) {
        return recycleMarketService.getPriceById(id);
    }

    @PostMapping("price")
    @Operation(summary = "新增行情")
    @PreAuthorize("hasAuthority('admin:recycle-market:add')")
    public R<RecyclePrice> savePrice(@Validated @RequestBody RecyclePriceSaveRequest request) {
        return recycleMarketService.savePrice(request);
    }

    @PatchMapping("price/detail/{id}")
    @Operation(summary = "更新行情")
    @PreAuthorize("hasAuthority('admin:recycle-market:update')")
    public R<RecyclePrice> updatePrice(@PathVariable Long id,
                                       @Validated @RequestBody RecyclePriceUpdateRequest request) {
        return recycleMarketService.updatePrice(id, request);
    }

    @DeleteMapping("price/detail/{id}")
    @Operation(summary = "删除行情")
    @PreAuthorize("hasAuthority('admin:recycle-market:delete')")
    public R<Integer> deletePrice(@PathVariable Long id) {
        return recycleMarketService.deletePrice(id);
    }

    @GetMapping("price/by-date")
    @Operation(summary = "按日期查询当日所有品类回收价格（行情报单详情用）")
    @PreAuthorize("hasAuthority('admin:recycle-market:list')")
    public R<List<RecyclePriceVo>> listPricesByDate(@RequestParam @NotNull(message = "行情日期不能为空") LocalDate date) {
        return R.success(recycleMarketService.listPricesByDate(date));
    }
}
