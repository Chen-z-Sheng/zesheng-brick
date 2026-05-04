package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.admin.entity.CategoryLevel1;
import com.zesheng.admin.entity.CategoryLevel2;
import com.zesheng.admin.entity.CategoryLevel3;
import com.zesheng.admin.entity.RecyclePrice;
import com.zesheng.admin.mapper.CategoryLevel1Mapper;
import com.zesheng.admin.mapper.CategoryLevel2Mapper;
import com.zesheng.admin.mapper.CategoryLevel3Mapper;
import com.zesheng.admin.mapper.RecyclePriceMapper;
import com.zesheng.admin.service.IRecycleMarketClientQueryService;
import com.zesheng.common.model.response.recyclemarket.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 回收行情 C 端查询实现
 */
@Service
public class RecycleMarketClientQueryServiceImpl implements IRecycleMarketClientQueryService {

    private static final DateTimeFormatter UPDATE_DATE_FORMAT = DateTimeFormatter.ofPattern("M月d日更新");

    @Resource
    private CategoryLevel1Mapper categoryLevel1Mapper;

    @Resource
    private CategoryLevel2Mapper categoryLevel2Mapper;

    @Resource
    private CategoryLevel3Mapper categoryLevel3Mapper;

    @Resource
    private RecyclePriceMapper recyclePriceMapper;

    @Override
    public List<RecycleMarketCategoryVo> listCategories() {
        List<CategoryLevel1> level1List = categoryLevel1Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel1>()
                        .eq(CategoryLevel1::getStatus, 1)
                        .orderByAsc(CategoryLevel1::getSortOrder));
        if (CollectionUtils.isEmpty(level1List)) {
            return new ArrayList<>();
        }
        List<CategoryLevel2> level2List = categoryLevel2Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel2>()
                        .eq(CategoryLevel2::getStatus, 1)
                        .in(CategoryLevel2::getLevel1Id, level1List.stream().map(CategoryLevel1::getId).collect(Collectors.toList()))
                        .orderByAsc(CategoryLevel2::getSortOrder));
        if (CollectionUtils.isEmpty(level2List)) {
            return level1List.stream().map(l1 -> {
                RecycleMarketCategoryVo vo = new RecycleMarketCategoryVo();
                vo.setId(l1.getId());
                vo.setName(l1.getName());
                vo.setSortOrder(l1.getSortOrder());
                vo.setChildren(new ArrayList<>());
                return vo;
            }).collect(Collectors.toList());
        }
        List<CategoryLevel3> level3List = categoryLevel3Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel3>()
                        .eq(CategoryLevel3::getStatus, 1)
                        .in(CategoryLevel3::getLevel2Id, level2List.stream().map(CategoryLevel2::getId).collect(Collectors.toList()))
                        .orderByAsc(CategoryLevel3::getSortOrder));
        Map<Long, List<CategoryLevel2>> level2ByLevel1 = level2List.stream().collect(Collectors.groupingBy(CategoryLevel2::getLevel1Id));
        Map<Long, List<CategoryLevel3>> level3ByLevel2 = level3List.stream().collect(Collectors.groupingBy(CategoryLevel3::getLevel2Id));

        List<RecycleMarketCategoryVo> result = new ArrayList<>();
        for (CategoryLevel1 l1 : level1List) {
            RecycleMarketCategoryVo vo = new RecycleMarketCategoryVo();
            vo.setId(l1.getId());
            vo.setName(l1.getName());
            vo.setSortOrder(l1.getSortOrder());
            List<CategoryLevel2> children = level2ByLevel1.get(l1.getId());
            if (children != null) {
                vo.setChildren(children.stream().map(l2 -> {
                    RecycleMarketSubCategoryVo sub = new RecycleMarketSubCategoryVo();
                    sub.setId(l2.getId());
                    sub.setLevel1Id(l2.getLevel1Id());
                    sub.setName(l2.getName());
                    sub.setSortOrder(l2.getSortOrder());
                    List<CategoryLevel3> level3Children = level3ByLevel2.get(l2.getId());
                    if (CollectionUtils.isEmpty(level3Children)) {
                        sub.setChildren(new ArrayList<>());
                    } else {
                        sub.setChildren(level3Children.stream().map(l3 -> {
                            RecycleMarketThirdCategoryVo third = new RecycleMarketThirdCategoryVo();
                            third.setId(l3.getId());
                            third.setLevel2Id(l3.getLevel2Id());
                            third.setName(l3.getName());
                            third.setSortOrder(l3.getSortOrder());
                            return third;
                        }).collect(Collectors.toList()));
                    }
                    return sub;
                }).collect(Collectors.toList()));
            } else {
                vo.setChildren(new ArrayList<>());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<RecycleMarketSubCategoryVo> listSubCategories(Long level1Id) {
        List<CategoryLevel2> list = categoryLevel2Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel2>()
                        .eq(CategoryLevel2::getLevel1Id, level1Id)
                        .eq(CategoryLevel2::getStatus, 1)
                        .orderByAsc(CategoryLevel2::getSortOrder));
        return list.stream().map(l2 -> {
            RecycleMarketSubCategoryVo vo = new RecycleMarketSubCategoryVo();
            vo.setId(l2.getId());
            vo.setLevel1Id(l2.getLevel1Id());
            vo.setName(l2.getName());
            vo.setSortOrder(l2.getSortOrder());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<RecycleMarketThirdCategoryVo> listThirdCategories(Long level2Id) {
        List<CategoryLevel3> list = categoryLevel3Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel3>()
                        .eq(CategoryLevel3::getLevel2Id, level2Id)
                        .eq(CategoryLevel3::getStatus, 1)
                        .orderByAsc(CategoryLevel3::getSortOrder));
        return list.stream().map(l3 -> {
            RecycleMarketThirdCategoryVo vo = new RecycleMarketThirdCategoryVo();
            vo.setId(l3.getId());
            vo.setLevel2Id(l3.getLevel2Id());
            vo.setName(l3.getName());
            vo.setSortOrder(l3.getSortOrder());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<RecycleMarketProductVo> listProducts(Long level1Id, Long level2Id, Long level3Id, LocalDate priceDate) {
        List<CategoryLevel3> level3List;
        if (level3Id != null) {
            CategoryLevel3 one = categoryLevel3Mapper.selectOne(
                    new LambdaQueryWrapper<CategoryLevel3>().eq(CategoryLevel3::getId, level3Id).eq(CategoryLevel3::getStatus, 1));
            level3List = one != null ? List.of(one) : new ArrayList<>();
        } else if (level2Id != null) {
            level3List = categoryLevel3Mapper.selectList(
                    new LambdaQueryWrapper<CategoryLevel3>()
                            .eq(CategoryLevel3::getLevel2Id, level2Id)
                            .eq(CategoryLevel3::getStatus, 1)
                            .orderByAsc(CategoryLevel3::getSortOrder));
        } else if (level1Id != null) {
            List<CategoryLevel2> level2List = categoryLevel2Mapper.selectList(
                    new LambdaQueryWrapper<CategoryLevel2>()
                            .eq(CategoryLevel2::getLevel1Id, level1Id)
                            .eq(CategoryLevel2::getStatus, 1));
            if (CollectionUtils.isEmpty(level2List)) {
                return new ArrayList<>();
            }
            level3List = categoryLevel3Mapper.selectList(
                    new LambdaQueryWrapper<CategoryLevel3>()
                            .in(CategoryLevel3::getLevel2Id, level2List.stream().map(CategoryLevel2::getId).collect(Collectors.toList()))
                            .eq(CategoryLevel3::getStatus, 1)
                            .orderByAsc(CategoryLevel3::getSortOrder));
        } else {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(level3List)) {
            return new ArrayList<>();
        }

        List<Long> level3Ids = level3List.stream().map(CategoryLevel3::getId).collect(Collectors.toList());
        List<RecyclePrice> allPrices = recyclePriceMapper.selectList(
                new LambdaQueryWrapper<RecyclePrice>()
                        .in(RecyclePrice::getLevel3Id, level3Ids)
                        .orderByDesc(RecyclePrice::getPriceDate));
        Map<Long, RecyclePrice> level3PriceMap = allPrices.stream()
                .filter(price -> price.getLevel3Id() != null)
                .collect(Collectors.toMap(RecyclePrice::getLevel3Id, p -> p, (a, b) -> a));

        List<RecycleMarketProductVo> result = new ArrayList<>();
        for (CategoryLevel3 l3 : level3List) {
            RecyclePrice price = level3PriceMap.get(l3.getId());
            if (price == null) {
                continue;
            }
            RecycleMarketProductVo vo = new RecycleMarketProductVo();
            vo.setId(price.getId());
            vo.setLevel2Id(l3.getLevel2Id());
            vo.setLevel3Id(l3.getId());
            vo.setName(l3.getName());
            vo.setPrice(price.getRecyclePrice());
            vo.setPriceDate(price.getPriceDate());
            vo.setUpdateDate(price.getPriceDate().format(UPDATE_DATE_FORMAT));
            vo.setRemark(price.getRemark());
            result.add(vo);
        }
        return result;
    }

    @Override
    public RecycleMarketProductVo getProductDetail(Long level3Id, LocalDate priceDate) {
        if (level3Id == null) {
            return null;
        }
        CategoryLevel3 level3 = categoryLevel3Mapper.selectOne(
                new LambdaQueryWrapper<CategoryLevel3>().eq(CategoryLevel3::getId, level3Id).eq(CategoryLevel3::getStatus, 1));
        if (level3 == null) {
            return null;
        }
        Long level2Id = level3.getLevel2Id();
        CategoryLevel2 l2 = categoryLevel2Mapper.selectOne(
                new LambdaQueryWrapper<CategoryLevel2>().eq(CategoryLevel2::getId, level2Id).eq(CategoryLevel2::getStatus, 1));
        if (priceDate == null) {
            priceDate = LocalDate.now().minusDays(1);
        }
        if (l2 == null) {
            return null;
        }
        LambdaQueryWrapper<RecyclePrice> priceQuery = new LambdaQueryWrapper<RecyclePrice>()
                .eq(RecyclePrice::getPriceDate, priceDate)
                .eq(RecyclePrice::getLevel3Id, level3.getId());
        RecyclePrice price = recyclePriceMapper.selectOne(priceQuery);

        RecyclePrice latestPrice = price;
        if (latestPrice == null) {
            LambdaQueryWrapper<RecyclePrice> latestQuery = new LambdaQueryWrapper<RecyclePrice>()
                    .orderByDesc(RecyclePrice::getPriceDate)
                    .last("LIMIT 1")
                    .eq(RecyclePrice::getLevel3Id, level3.getId());
            latestPrice = recyclePriceMapper.selectOne(latestQuery);
        }
        RecycleMarketProductVo vo = new RecycleMarketProductVo();
        vo.setLevel2Id(l2.getId());
        vo.setLevel3Id(level3 != null ? level3.getId() : null);
        vo.setName(level3 != null ? level3.getName() : l2.getName());
        if (latestPrice != null) {
            vo.setId(latestPrice.getId());
            vo.setPrice(latestPrice.getRecyclePrice());
            vo.setPriceDate(latestPrice.getPriceDate());
            vo.setUpdateDate(latestPrice.getPriceDate().format(UPDATE_DATE_FORMAT));
            vo.setRemark(latestPrice.getRemark());
        } else {
            vo.setPrice(java.math.BigDecimal.ZERO);
            vo.setPriceDate(priceDate);
            vo.setUpdateDate(priceDate.format(UPDATE_DATE_FORMAT));
        }
        return vo;
    }

    @Override
    public List<RecycleMarketPriceHistoryVo> getPriceHistory(Long level3Id, int limit) {
        if (limit <= 0) {
            limit = 30;
        }
        if (level3Id == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<RecyclePrice> queryWrapper = new LambdaQueryWrapper<RecyclePrice>()
                .orderByDesc(RecyclePrice::getPriceDate)
                .last("LIMIT " + limit)
                .eq(RecyclePrice::getLevel3Id, level3Id);
        List<RecyclePrice> list = recyclePriceMapper.selectList(queryWrapper);
        return list.stream().map(p -> {
            RecycleMarketPriceHistoryVo vo = new RecycleMarketPriceHistoryVo();
            vo.setPriceDate(p.getPriceDate());
            vo.setPrice(p.getRecyclePrice());
            return vo;
        }).collect(Collectors.toList());
    }
}
