package com.zesheng.admin.service.impl;

import com.zesheng.common.request.BatchDeleteRequest;
import org.apache.commons.lang3.ObjectUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.DeliveryAddress;
import com.zesheng.admin.entity.FormScheme;
import com.zesheng.admin.mapper.DeliveryAddressMapper;
import com.zesheng.admin.mapper.FormSchemeMapper;
import com.zesheng.admin.model.request.FormSchemePageRequest;
import com.zesheng.admin.model.request.FormSchemeSaveRequest;
import com.zesheng.admin.model.request.FormSchemeUpdateRequest;
import com.zesheng.admin.model.response.FormSchemeListResponse;
import com.zesheng.admin.model.response.FormSchemePageResponse;
import com.zesheng.admin.model.response.FormSchemeSaveResponse;
import com.zesheng.admin.model.response.FormSchemeUpdateResponse;
import com.zesheng.admin.model.response.FormSchemeVo;
import com.zesheng.admin.service.IFormSchemeService;
import com.zesheng.common.enums.StatusEnum;
import com.zesheng.common.response.BatchDeleteResponse;
import com.zesheng.common.response.R;
import com.zesheng.common.response.PageMeta;
import com.zesheng.common.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 方案表 服务实现类
 *
 * @author czk
 * @since 2026-02-15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FormSchemeServiceImpl implements IFormSchemeService {

    private final FormSchemeMapper formSchemeMapper;
    private final DeliveryAddressMapper deliveryAddressMapper;

    /**
     * 新增方案
     *
     * @param formSchemeSaveRequest 新增请求
     * @return 新增结果
     */
    @Override
    public R<FormSchemeSaveResponse> save(FormSchemeSaveRequest formSchemeSaveRequest) {
        // 创建 FormScheme 实体对象（仅复制请求中的非空参数）
        FormScheme formScheme = BeanCopyUtils.copyIgnoreNull(formSchemeSaveRequest, FormScheme.class);
        // unit_price 为必填，若前端未传则默认 0
        if (formScheme.getUnitPrice() == null) {
            formScheme.setUnitPrice(java.math.BigDecimal.ZERO);
        }

        // 使用自定义 insertAndFillId，确保 id 回填、created_at/updated_at 由数据库 NOW() 填充
        int result = formSchemeMapper.insertAndFillId(formScheme);

        if (result > 0) {
            // insertAndFillId 已回填 id；若 created_at/updated_at 未填充则再查一次
            FormScheme saved = formScheme;
            if (formScheme.getCreatedAt() == null && formScheme.getId() != null) {
                saved = formSchemeMapper.selectById(formScheme.getId());
            } else if (formScheme.getId() == null) {
                saved = formSchemeMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FormScheme>()
                                .eq(FormScheme::getName, formScheme.getName())
                                .orderByDesc(FormScheme::getId)
                                .last("LIMIT 1")
                );
            }
            Assert.notNull(saved, "新增方案成功，但查询不到新增的数据");

            FormSchemeSaveResponse response = BeanCopyUtils.copyIgnoreNull(saved, FormSchemeSaveResponse.class);
            return R.success(response);
        } else {
            return R.error("新增方案失败");
        }
    }

    @Override
    public R<Integer> delete(Long id) {
        // 校验ID不能为空
        if (ObjectUtils.isEmpty(id)) {
            log.warn("单个删除方案失败：方案ID不能为空");
            return R.error("删除失败，方案ID不能为空");
        }

        // 校验ID对应的方案是否存在
        FormScheme existFormScheme = formSchemeMapper.selectOne(
                new LambdaQueryWrapper<FormScheme>().eq(FormScheme::getId, id)
        );
        if (existFormScheme == null) {
            log.warn("单个删除方案失败：方案ID{}不存在", id);
            return R.error("删除失败，方案ID" + id + "不存在");
        }

        // 执行物理删除
        int deleteCount = formSchemeMapper.deleteById(id);

        // 返回删除结果
        if (deleteCount > 0) {
            log.info("单个删除方案成功，删除的方案ID：{}", id);
            return R.success("删除成功，方案ID：" + id, deleteCount);
        } else {
            log.error("单个删除方案失败：方案ID{}执行删除操作异常", id);
            return R.error("删除失败，方案ID" + id + "删除操作执行异常");
        }
    }

    /**
     * 批量删除方案
     *
     * @param req 批量方案id列表
     * @return 删除数量
     */
    @Override
    public R<BatchDeleteResponse> batchDelete(BatchDeleteRequest req) {
        // 入参校验：ID列表不能为空
        List<Long> ids = req.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return R.error("删除ID列表不能为空");
        }

        // 初始化批量删除响应对象，设置总请求数
        BatchDeleteResponse response = new BatchDeleteResponse();
        response.setTotalRequest(ids.size());

        // 构建查询条件：根据ID列表查询数据
        LambdaQueryWrapper<FormScheme> queryWrapper = new LambdaQueryWrapper<FormScheme>()
                .in(FormScheme::getId, ids);

        // 查询数据库中实际存在的ID列表（修复空指针风险）
        List<Long> existIds = formSchemeMapper.selectObjs(queryWrapper)
                .stream()
                .filter(obj -> obj != null) // 过滤null值，避免空指针
                .map(obj -> Long.parseLong(obj.toString()))
                .collect(Collectors.toList());

        // 处理不存在的ID：记录到失败列表
        Map<Serializable, String> failedIds = new HashMap<>();
        for (Long id : ids) {
            if (!existIds.contains(id)) {
                failedIds.put(id, "数据不存在");
            }
        }
        response.setFailedIds(failedIds);

        // 执行批量物理删除操作
        if (!CollectionUtils.isEmpty(existIds)) {
            int deleteCount = formSchemeMapper.delete(new LambdaQueryWrapper<FormScheme>().in(FormScheme::getId, existIds));

            // 仅当删除成功时填充成功ID列表（避免删除失败却返回成功ID）
            if (deleteCount == existIds.size()) {
                response.setSuccessIds(existIds.stream()
                        .map(id -> (Serializable) id)
                        .collect(Collectors.toList()));
            } else {
                // 部分/全部删除失败时，记录失败原因
                for (Long id : existIds) {
                    failedIds.put(id, "删除操作执行失败");
                }
                response.setFailedIds(failedIds);
            }
        }

        // 自动计算成功/失败数量
        response.calculateCount();

        // 打印操作日志
        log.info("【批量删除{}】总请求数：{}，成功删除：{}，失败：{}",
                "管理端-方案表", response.getTotalRequest(),
                response.getSuccessCount(), response.getFailedCount());

        // 返回结果
        String msg = "批量删除完成，成功" + response.getSuccessCount() + "条，失败" + response.getFailedCount() + "条";
        return R.success(msg, response);
    }

    /**
     * 更新方案
     *
     * @param id                      方案id
     * @param formSchemeUpdateRequest 更新请求
     * @return 更新结果
     */
    @Override
    public R<FormSchemeUpdateResponse> update(Long id, FormSchemeUpdateRequest formSchemeUpdateRequest) {
        Assert.notNull(id, "方案id不能为空");
        Assert.notNull(formSchemeUpdateRequest, "更新请求参数不能为空");

        // 先查询数据是否存在
        FormScheme existFormScheme = formSchemeMapper.selectById(id);
        if (existFormScheme == null) {
            return R.error("要更新的方案不存在");
        }

        // 将请求参数复制到已存在的实体对象（只复制非空字段，不覆盖原有数据）
        BeanCopyUtils.copyIgnoreNullToExist(formSchemeUpdateRequest, existFormScheme);

        // 执行更新
        int result = formSchemeMapper.updateById(existFormScheme);

        if (result > 0) {
            FormSchemeUpdateResponse response = BeanCopyUtils.copyIgnoreNull(existFormScheme, FormSchemeUpdateResponse.class);
            response.setId(id);
            return R.success(response);
        } else {
            return R.error("更新方案失败");
        }
    }

    /**
     * 分页查询方案
     *
     * @param formSchemePageRequest 分页查询请求参数
     * @return 分页查询结果
     */
    @Override
    public R<FormSchemePageResponse> page(FormSchemePageRequest formSchemePageRequest) {
        // 构建分页查询条件并执行关联查询（一次性查询方案+模板名称）
        Page<FormScheme> page = new Page<>(formSchemePageRequest.getPageNum(), formSchemePageRequest.getPageSize());
        LambdaQueryWrapper<FormScheme> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(formSchemePageRequest.getName())) {
            queryWrapper.like(FormScheme::getName, formSchemePageRequest.getName().trim());
        }
        if (formSchemePageRequest.getStatus() != null) {
            queryWrapper.eq(FormScheme::getStatus, StatusEnum.getByCode(formSchemePageRequest.getStatus()));
        }
        queryWrapper.orderByDesc(FormScheme::getCreatedAt);

        IPage<FormSchemeVo> voPage = formSchemeMapper.selectPageForVo(page, queryWrapper);

        // 构建FormSchemePageResponse对象（利用继承关系赋值）
        FormSchemePageResponse response = new FormSchemePageResponse();
        PageMeta pageMeta = PageMeta.of(voPage.getTotal(), (int) voPage.getCurrent(), (int) voPage.getSize());
        response.setPageMeta(pageMeta);
        List<FormSchemeVo> records = voPage.getRecords();
        fillDeliveryAddressText(records);
        response.setRecords(records);

        return R.success(response);
    }

    /**
     * 查询方案列表
     *
     * @return 方案列表
     */
    @Override
    public R<List<FormSchemeListResponse>> list() {
        // 查询所有方案
        List<FormScheme> list = formSchemeMapper.selectList(null);

        List<FormSchemeListResponse> responseList = list.stream().map(formScheme -> {
            FormSchemeListResponse response = new FormSchemeListResponse();
            BeanUtils.copyProperties(formScheme, response);
            if (formScheme.getAddressId() != null) {
                DeliveryAddress addr = deliveryAddressMapper.selectById(formScheme.getAddressId());
                if (addr != null) {
                    response.setDeliveryAddressText(addr.getFullAddress());
                }
            }
            return response;
        }).collect(java.util.stream.Collectors.toList());

        return R.success(responseList);
    }

    /**
     * 查询方案详情
     *
     * @param id 方案id
     * @return 方案详情
     */
    @Override
    public R<FormSchemeVo> info(Long id) {
        FormScheme formScheme = formSchemeMapper.selectById(id);
        if (formScheme == null) {
            return R.error("方案不存在");
        }

        FormSchemeVo vo = new FormSchemeVo();
        BeanUtils.copyProperties(formScheme, vo);
        if (formScheme.getAddressId() != null) {
            DeliveryAddress addr = deliveryAddressMapper.selectById(formScheme.getAddressId());
            if (addr != null) {
                vo.setDeliveryAddressText(addr.getFullAddress());
            }
        }

        return R.success(vo);
    }

    private void fillDeliveryAddressText(List<FormSchemeVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (FormSchemeVo vo : records) {
            if (vo.getAddressId() == null) {
                continue;
            }
            DeliveryAddress addr = deliveryAddressMapper.selectById(vo.getAddressId());
            if (addr != null) {
                vo.setDeliveryAddressText(addr.getFullAddress());
            }
        }
    }
}
