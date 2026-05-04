package com.zesheng.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.sys.entity.RolePermission;
import com.zesheng.sys.mapper.RolePermissionMapper;
import com.zesheng.sys.model.request.RolePermissionPageRequest;
import com.zesheng.sys.model.request.RolePermissionSaveRequest;
import com.zesheng.sys.model.request.RolePermissionUpdateRequest;
import com.zesheng.sys.model.response.RolePermissionListResponse;
import com.zesheng.sys.model.response.RolePermissionPageResponse;
import com.zesheng.sys.model.response.RolePermissionSaveResponse;
import com.zesheng.sys.model.response.RolePermissionUpdateResponse;
import com.zesheng.sys.model.response.RolePermissionVo;
import com.zesheng.sys.service.IRolePermissionService;
import com.zesheng.common.response.R;
import com.zesheng.common.response.PageMeta;
import com.zesheng.common.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.zesheng.common.response.BatchDeleteResponse;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统端-角色与权限关联 服务实现类
 *
 * @author czk
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
@Slf4j // 生产级日志
public class RolePermissionServiceImpl implements IRolePermissionService {
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 新增系统端-角色与权限关联
     * 核心：新增后查询数据库，补全BaseEntity的createdAt/updatedAt
     */
    @Override
    public R<RolePermissionSaveResponse> save(RolePermissionSaveRequest saveRequest) {
// DTO转实体（仅复制业务字段，BaseEntity字段由MyBatis-Plus自动填充）
        RolePermission entity = BeanCopyUtils.copyIgnoreNull(saveRequest, RolePermission.class);

// 执行新增
        int insertCount = rolePermissionMapper.insert(entity);
        if (insertCount <= 0) {
            log.error("【新增{}】失败，请求参数：{}", BUSINESS_NAME, saveRequest);
            return R.error("新增" + BUSINESS_NAME + "失败");
        }

// 关键：查询数据库获取完整实体（含createdAt/updatedAt）
        RolePermission savedEntity = rolePermissionMapper.selectById(entity.getId());
        Assert.notNull(savedEntity, "新增" + BUSINESS_NAME + "成功，但查询不到数据，ID：" + entity.getId());

// 实体转返回体（自动包含createdAt/updatedAt）
        RolePermissionSaveResponse response = BeanCopyUtils.copyIgnoreNull(savedEntity, RolePermissionSaveResponse.class);
        response.setId(savedEntity.getId()); // 显式赋值ID，确保非空

        log.info("【新增{}】成功，ID：{}", BUSINESS_NAME, savedEntity.getId());
        return R.success(response);
    }

    /**
     * 单个删除系统端-角色与权限关联
     * 自动适配：有deleted_at字段用软删除，无则物理删除
     */
    @Override
    public R<Void> delete(Long id) {
        Assert.notNull(id, BUSINESS_NAME + "ID不能为空");


        // 查询数据是否存在
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getId, id);

        RolePermission existEntity = rolePermissionMapper.selectOne(queryWrapper);
        if (existEntity == null) {
            log.error("【删除{}】失败，ID：{}不存在", BUSINESS_NAME, id);
            return R.error(BUSINESS_NAME + "ID不存在");
        }

        int deleteCount;
        // 执行物理删除
        deleteCount = rolePermissionMapper.deleteById(id);

        if (deleteCount <= 0) {
            log.error("【删除{}】失败，ID：{}", BUSINESS_NAME, id);
            return R.error("删除" + BUSINESS_NAME + "失败");
        }

        log.info("【删除{}】成功，ID：{}", BUSINESS_NAME, id);
        return R.success();
    }

    /**
     * 批量删除系统端-角色与权限关联
     * 自动适配：有deleted_at字段用软删除，无则物理删除
     */
    @Override
    public R<BatchDeleteResponse> delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return R.error("删除ID列表不能为空");
        }


        BatchDeleteResponse response = new BatchDeleteResponse();
        response.setTotalRequest(ids.size());

        // 查询实际存在的ID列表
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getId, ids);

        List<Long> existIds = rolePermissionMapper.selectObjs(queryWrapper)
                .stream()
                .map(obj -> Long.parseLong(obj.toString()))
                .collect(Collectors.toList());

        // 处理不存在的ID
        Map<Serializable, String> failedIds = new HashMap<>();
        for (Long id : ids) {
            if (!existIds.contains(id)) {
                failedIds.put(id, "数据不存在");
            }
        }
        response.setFailedIds(failedIds);

        if (!CollectionUtils.isEmpty(existIds)) {
            int deleteCount;
            // 执行物理删除
            deleteCount = rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                    .in(RolePermission::getId, existIds));

            if (deleteCount > 0) {
                response.setSuccessIds(existIds.stream()
                        .map(id -> (Serializable) id)
                        .collect(Collectors.toList()));
            }
        }

        response.calculateCount();
        log.info("【批量删除{}】总请求数：{}，成功删除：{}，失败：{}",
                BUSINESS_NAME, response.getTotalRequest(),
                response.getSuccessCount(), response.getFailedCount());

        String msg = "批量删除完成，成功" + response.getSuccessCount() + "条，失败" + response.getFailedCount() + "条";
        return R.success(msg, response);
    }

    /**
     * 修改系统端-角色与权限关联
     * 核心：更新后查询数据库，补全updatedAt
     */
    @Override
    public R<RolePermissionUpdateResponse> update(Long id, RolePermissionUpdateRequest updateRequest) {
        // 参数校验
        Assert.notNull(id, BUSINESS_NAME + "ID不能为空");
        Assert.notNull(updateRequest, "更新请求参数不能为空");

        // 检查数据是否存在
        RolePermission existEntity = rolePermissionMapper.selectById(id);
        if (existEntity == null) {
            log.error("【修改{}】失败，ID：{}不存在", BUSINESS_NAME, id);
            return R.error("要修改的" + BUSINESS_NAME + "不存在");
        }

        // 复制更新参数（仅非空字段，不覆盖BaseEntity字段）
        BeanCopyUtils.copyIgnoreNullToExist(updateRequest, existEntity);

        // 执行更新
        int updateCount = rolePermissionMapper.updateById(existEntity);
        if (updateCount <= 0) {
            log.error("【修改{}】失败，ID：{}，请求参数：{}", BUSINESS_NAME, id, updateRequest);
            return R.error("修改" + BUSINESS_NAME + "失败");
        }

        // 查询最新数据（含更新后的updatedAt）
        RolePermission updatedEntity = rolePermissionMapper.selectById(id);

        // 转换为返回体
        RolePermissionUpdateResponse response = BeanCopyUtils.copyIgnoreNull(updatedEntity, RolePermissionUpdateResponse.class);
        response.setId(id);

        log.info("【修改{}】成功，ID：{}", BUSINESS_NAME, id);
        return R.success(response);
    }

    /**
     * 分页查询系统端-角色与权限关联
     * 优化：校验分页参数，排序用BaseEntity的createdAt
     */
    @Override
    public R<RolePermissionPageResponse> page(RolePermissionPageRequest pageRequest) {
        // 分页参数校验（避免无效参数）
        int pageNum = pageRequest.getPageNum() <= 0 ? 1 : pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize() <= 0 ? 10 : pageRequest.getPageSize();
        pageSize = pageSize > 100 ? 100 : pageSize; // 限制最大页大小

        // 构建分页查询
        Page<RolePermission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RolePermission> queryWrapper = new LambdaQueryWrapper<RolePermission>()
                .orderByDesc(RolePermission::getCreatedAt); // 匹配BaseEntity的createdAt

        IPage<RolePermission> iPage = rolePermissionMapper.selectPage(page, queryWrapper);

        // 转换为VO分页对象（统一使用BeanCopyUtils）
        IPage<RolePermissionVo> voPage = iPage.convert(entity -> BeanCopyUtils.copyIgnoreNull(entity, RolePermissionVo.class));

        // 构建分页返回体
        RolePermissionPageResponse response = new RolePermissionPageResponse();
        PageMeta pageMeta = PageMeta.of(voPage.getTotal(), (int) voPage.getCurrent(), (int) voPage.getSize());
        response.setPageMeta(pageMeta);
        response.setRecords(voPage.getRecords());

        log.info("【分页查询{}】页码：{}，页大小：{}，总数：{}", BUSINESS_NAME, pageNum, pageSize, voPage.getTotal());
        return R.success(response);
    }

    /**
     * 查询系统端-角色与权限关联列表（不分页）
     */
    @Override
    public R<List<RolePermissionListResponse>> list() {
        List<RolePermission> entityList = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .orderByDesc(RolePermission::getCreatedAt));

// 转换为ListResponse（继承Vo，无需重复赋值）
        List<RolePermissionListResponse> responseList = entityList.stream()
                .map(entity -> BeanCopyUtils.copyIgnoreNull(entity, RolePermissionListResponse.class))
                .collect(Collectors.toList());

        log.info("【查询{}列表】总数：{}", BUSINESS_NAME, responseList.size());
        return R.success(responseList);
    }

    /**
     * 查询系统端-角色与权限关联详情
     */
    @Override
    public R<RolePermissionVo> info(Long id) {
        Assert.notNull(id, BUSINESS_NAME + "ID不能为空");

        RolePermission entity = rolePermissionMapper.selectById(id);
        if (entity == null) {
            log.error("【查询{}详情】ID：{}不存在", BUSINESS_NAME, id);
            return R.error(BUSINESS_NAME + "不存在");
        }

        RolePermissionVo vo = BeanCopyUtils.copyIgnoreNull(entity, RolePermissionVo.class);
        return R.success(vo);
    }
}