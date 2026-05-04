package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.CategoryLevel1;
import com.zesheng.admin.entity.CategoryLevel2;
import com.zesheng.admin.entity.CategoryLevel3;
import com.zesheng.admin.entity.RecyclePrice;
import com.zesheng.admin.mapper.CategoryLevel1Mapper;
import com.zesheng.admin.mapper.CategoryLevel2Mapper;
import com.zesheng.admin.mapper.CategoryLevel3Mapper;
import com.zesheng.admin.mapper.RecyclePriceMapper;
import com.zesheng.admin.model.request.*;
import com.zesheng.admin.model.response.RecyclePricePageResponse;
import com.zesheng.admin.model.response.RecyclePriceVo;
import com.zesheng.admin.service.IRecycleMarketService;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行情管理 Service 实现
 */
@Service
public class RecycleMarketServiceImpl implements IRecycleMarketService {

    @Resource
    private CategoryLevel1Mapper categoryLevel1Mapper;

    @Resource
    private CategoryLevel2Mapper categoryLevel2Mapper;

    @Resource
    private CategoryLevel3Mapper categoryLevel3Mapper;

    @Resource
    private RecyclePriceMapper recyclePriceMapper;

    @Override
    public List<CategoryLevel1> listLevel1() {
        return categoryLevel1Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel1>()
                        .orderByAsc(CategoryLevel1::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<CategoryLevel1> saveLevel1(CategoryLevel1SaveRequest request) {
        CategoryLevel1 entity = new CategoryLevel1();
        entity.setName(request.getName().trim());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        categoryLevel1Mapper.insert(entity);
        return R.success(categoryLevel1Mapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<CategoryLevel1> updateLevel1(Long id, CategoryLevel1UpdateRequest request) {
        CategoryLevel1 entity = categoryLevel1Mapper.selectById(id);
        if (entity == null) {
            return R.error("一级分类不存在");
        }
        entity.setName(request.getName().trim());
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        categoryLevel1Mapper.updateById(entity);
        return R.success(categoryLevel1Mapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteLevel1(Long id) {
        int rows = categoryLevel1Mapper.deleteById(id);
        return R.success(rows);
    }

    @Override
    public List<CategoryLevel2> listLevel2(Long level1Id) {
        return categoryLevel2Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel2>()
                        .eq(CategoryLevel2::getLevel1Id, level1Id)
                        .orderByAsc(CategoryLevel2::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<CategoryLevel2> saveLevel2(CategoryLevel2SaveRequest request) {
        CategoryLevel2 entity = new CategoryLevel2();
        entity.setLevel1Id(request.getLevel1Id());
        entity.setName(request.getName().trim());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        categoryLevel2Mapper.insert(entity);
        return R.success(categoryLevel2Mapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<CategoryLevel2> updateLevel2(Long id, CategoryLevel2UpdateRequest request) {
        CategoryLevel2 entity = categoryLevel2Mapper.selectById(id);
        if (entity == null) {
            return R.error("二级分类不存在");
        }
        entity.setName(request.getName().trim());
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        categoryLevel2Mapper.updateById(entity);
        return R.success(categoryLevel2Mapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteLevel2(Long id) {
        int rows = categoryLevel2Mapper.deleteById(id);
        return R.success(rows);
    }

    @Override
    public List<CategoryLevel3> listLevel3(Long level2Id) {
        return categoryLevel3Mapper.selectList(
                new LambdaQueryWrapper<CategoryLevel3>()
                        .eq(CategoryLevel3::getLevel2Id, level2Id)
                        .orderByAsc(CategoryLevel3::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<CategoryLevel3> saveLevel3(CategoryLevel3SaveRequest request) {
        CategoryLevel2 level2 = categoryLevel2Mapper.selectById(request.getLevel2Id());
        if (level2 == null) {
            return R.error("二级分类不存在");
        }
        CategoryLevel3 entity = new CategoryLevel3();
        entity.setLevel2Id(request.getLevel2Id());
        entity.setName(request.getName().trim());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        categoryLevel3Mapper.insert(entity);
        return R.success(categoryLevel3Mapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<CategoryLevel3> updateLevel3(Long id, CategoryLevel3UpdateRequest request) {
        CategoryLevel3 entity = categoryLevel3Mapper.selectById(id);
        if (entity == null) {
            return R.error("三级分类不存在");
        }
        entity.setName(request.getName().trim());
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        categoryLevel3Mapper.updateById(entity);
        return R.success(categoryLevel3Mapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteLevel3(Long id) {
        int rows = categoryLevel3Mapper.deleteById(id);
        return R.success(rows);
    }

    @Override
    public LocalDate getLatestPriceDate() {
        RecyclePrice one = recyclePriceMapper.selectOne(
                new LambdaQueryWrapper<RecyclePrice>()
                        .orderByDesc(RecyclePrice::getPriceDate)
                        .last("LIMIT 1"));
        return one != null ? one.getPriceDate() : null;
    }

    @Override
    public R<RecyclePricePageResponse> pagePrice(RecyclePricePageRequest request) {
        LambdaQueryWrapper<RecyclePrice> wrapper = new LambdaQueryWrapper<>();
        if (request.getLevel1Id() != null) {
            List<CategoryLevel2> level2List = categoryLevel2Mapper.selectList(
                    new LambdaQueryWrapper<CategoryLevel2>().eq(CategoryLevel2::getLevel1Id, request.getLevel1Id()));
            if (CollectionUtils.isEmpty(level2List)) {
                return R.success(buildEmptyPageResponse(request));
            }
            List<Long> level2Ids = level2List.stream().map(CategoryLevel2::getId).collect(Collectors.toList());
            List<CategoryLevel3> level3List = categoryLevel3Mapper.selectList(
                    new LambdaQueryWrapper<CategoryLevel3>().in(CategoryLevel3::getLevel2Id, level2Ids));
            if (CollectionUtils.isEmpty(level3List)) {
                return R.success(buildEmptyPageResponse(request));
            }
            wrapper.in(RecyclePrice::getLevel3Id, level3List.stream().map(CategoryLevel3::getId).collect(Collectors.toList()));
        }
        if (request.getLevel2Id() != null) {
            List<CategoryLevel3> level3List = categoryLevel3Mapper.selectList(
                    new LambdaQueryWrapper<CategoryLevel3>().eq(CategoryLevel3::getLevel2Id, request.getLevel2Id()));
            if (CollectionUtils.isEmpty(level3List)) {
                return R.success(buildEmptyPageResponse(request));
            }
            wrapper.in(RecyclePrice::getLevel3Id, level3List.stream().map(CategoryLevel3::getId).collect(Collectors.toList()));
        }
        if (request.getLevel3Id() != null) {
            wrapper.eq(RecyclePrice::getLevel3Id, request.getLevel3Id());
        }
        if (request.getPriceDate() != null) {
            wrapper.eq(RecyclePrice::getPriceDate, request.getPriceDate());
        }
        String orderBy = request.getOrderBy();
        if (orderBy != null && !orderBy.isBlank()) {
            boolean asc = "ASC".equalsIgnoreCase(request.getOrder());
            if ("priceDate".equals(orderBy)) {
                wrapper.orderBy(true, asc, RecyclePrice::getPriceDate);
            } else if ("recyclePrice".equals(orderBy)) {
                wrapper.orderBy(true, asc, RecyclePrice::getRecyclePrice);
            } else {
                wrapper.orderBy(true, false, RecyclePrice::getUpdatedAt);
            }
        } else {
            wrapper.orderByDesc(RecyclePrice::getPriceDate).orderByDesc(RecyclePrice::getUpdatedAt);
        }

        Page<RecyclePrice> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<RecyclePrice> iPage = recyclePriceMapper.selectPage(page, wrapper);
        List<RecyclePriceVo> voList = buildVoList(iPage.getRecords());

        RecyclePricePageResponse response = new RecyclePricePageResponse();
        response.setPageMeta(com.zesheng.common.response.PageMeta.of(
                iPage.getTotal(), (int) iPage.getCurrent(), (int) iPage.getSize()));
        response.setRecords(voList);
        return R.success(response);
    }

    private RecyclePricePageResponse buildEmptyPageResponse(RecyclePricePageRequest request) {
        RecyclePricePageResponse response = new RecyclePricePageResponse();
        response.setPageMeta(com.zesheng.common.response.PageMeta.of(0L, request.getPageNum(), request.getPageSize()));
        response.setRecords(new ArrayList<>());
        return response;
    }

    private List<RecyclePriceVo> buildVoList(List<RecyclePrice> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        List<Long> level3Ids = records.stream()
                .map(RecyclePrice::getLevel3Id)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<CategoryLevel3> level3List = categoryLevel3Mapper.selectBatchIds(level3Ids);
        Map<Long, CategoryLevel3> level3Map = level3List.stream().collect(Collectors.toMap(CategoryLevel3::getId, e -> e));
        List<Long> level2Ids = level3List.stream()
                .map(CategoryLevel3::getLevel2Id)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<CategoryLevel2> level2List = categoryLevel2Mapper.selectBatchIds(level2Ids);
        Map<Long, CategoryLevel2> level2Map = level2List.stream().collect(Collectors.toMap(CategoryLevel2::getId, e -> e));

        List<Long> level1Ids = level2List.stream().map(CategoryLevel2::getLevel1Id).distinct().collect(Collectors.toList());
        List<CategoryLevel1> level1List = categoryLevel1Mapper.selectBatchIds(level1Ids);
        Map<Long, CategoryLevel1> level1Map = level1List.stream().collect(Collectors.toMap(CategoryLevel1::getId, e -> e));

        List<RecyclePriceVo> voList = new ArrayList<>();
        for (RecyclePrice r : records) {
            RecyclePriceVo vo = new RecyclePriceVo();
            vo.setId(r.getId());
            vo.setLevel3Id(r.getLevel3Id());
            vo.setPriceDate(r.getPriceDate());
            vo.setRecyclePrice(r.getRecyclePrice());
            vo.setRemark(r.getRemark());
            vo.setCreatedAt(r.getCreatedAt());
            vo.setUpdatedAt(r.getUpdatedAt());
            CategoryLevel3 l3 = level3Map.get(r.getLevel3Id());
            if (l3 != null) {
                vo.setLevel3Name(l3.getName());
                vo.setLevel2Id(l3.getLevel2Id());
                CategoryLevel2 l2 = level2Map.get(l3.getLevel2Id());
                if (l2 != null) {
                    vo.setLevel2Name(l2.getName());
                    vo.setLevel1Id(l2.getLevel1Id());
                    CategoryLevel1 l1 = level1Map.get(l2.getLevel1Id());
                    if (l1 != null) {
                        vo.setLevel1Name(l1.getName());
                    }
                }
            }
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public R<RecyclePriceVo> getPriceById(Long id) {
        RecyclePrice entity = recyclePriceMapper.selectById(id);
        if (entity == null) {
            return R.error("行情记录不存在");
        }
        List<RecyclePriceVo> voList = buildVoList(List.of(entity));
        return R.success(voList.isEmpty() ? null : voList.get(0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<RecyclePrice> savePrice(RecyclePriceSaveRequest request) {
        CategoryLevel3 level3 = categoryLevel3Mapper.selectById(request.getLevel3Id());
        if (level3 == null) {
            return R.error("三级分类不存在");
        }
        CategoryLevel2 level2 = categoryLevel2Mapper.selectById(level3.getLevel2Id());
        if (level2 == null) {
            return R.error("二级分类不存在");
        }
        Long cnt = recyclePriceMapper.selectCount(
                new LambdaQueryWrapper<RecyclePrice>()
                        .eq(RecyclePrice::getLevel3Id, request.getLevel3Id())
                        .eq(RecyclePrice::getPriceDate, request.getPriceDate()));
        if (cnt != null && cnt > 0) {
            return R.error("该商品当日已有行情记录，请直接编辑");
        }
        RecyclePrice entity = new RecyclePrice();
        entity.setLevel3Id(request.getLevel3Id());
        entity.setPriceDate(request.getPriceDate());
        entity.setRecyclePrice(request.getRecyclePrice());
        entity.setRemark(request.getRemark() != null ? request.getRemark().trim() : "");
        try {
            recyclePriceMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            return R.error("该日期行情已存在，请刷新列表后编辑");
        }
        return R.success(recyclePriceMapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<RecyclePrice> updatePrice(Long id, RecyclePriceUpdateRequest request) {
        RecyclePrice entity = recyclePriceMapper.selectById(id);
        if (entity == null) {
            return R.error("行情记录不存在");
        }
        entity.setRecyclePrice(request.getRecyclePrice());
        entity.setRemark(request.getRemark() != null ? request.getRemark().trim() : "");
        recyclePriceMapper.updateById(entity);
        return R.success(recyclePriceMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deletePrice(Long id) {
        int rows = recyclePriceMapper.deleteById(id);
        return R.success(rows);
    }

    @Override
    public List<RecyclePriceVo> listPricesByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        List<RecyclePrice> list = recyclePriceMapper.selectList(
                new LambdaQueryWrapper<RecyclePrice>()
                        .eq(RecyclePrice::getPriceDate, date)
                        .orderByAsc(RecyclePrice::getLevel3Id));
        return buildVoList(list);
    }
}
