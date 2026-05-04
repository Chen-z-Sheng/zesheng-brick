package com.zesheng.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.common.response.PageMeta;
import com.zesheng.sys.entity.Permission;
import com.zesheng.sys.mapper.PermissionMapper;
import com.zesheng.sys.model.request.PermissionPageRequest;
import com.zesheng.sys.model.request.PermissionSaveRequest;
import com.zesheng.sys.model.request.PermissionUpdateRequest;
import com.zesheng.sys.model.response.PermissionListResponse;
import com.zesheng.sys.model.response.PermissionPageResponse;
import com.zesheng.sys.model.response.PermissionSaveResponse;
import com.zesheng.sys.model.response.PermissionUpdateResponse;
import com.zesheng.sys.model.response.PermissionVo;
import com.zesheng.sys.service.IPermissionService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统端-权限表 服务实现类
 *
 * @author czk
 * @since 2026-02-19
 */
@Service
@RequiredArgsConstructor
@Slf4j // 生产级日志
public class PermissionServiceImpl implements IPermissionService {
    private final PermissionMapper permissionMapper;

    /**
     * 新增系统端-权限表
     * 核心：新增后查询数据库，补全BaseEntity的createdAt/updatedAt
     */
    @Override
    public R<PermissionSaveResponse> save(PermissionSaveRequest saveRequest) {
        // DTO转实体（仅复制业务字段，BaseEntity字段由MyBatis-Plus自动填充）
        Permission entity = BeanCopyUtils.copyIgnoreNull(saveRequest, Permission.class);

        // 执行新增
        int insertCount = permissionMapper.insert(entity);
        if (insertCount <= 0) {
            log.error("【新增{}】失败，请求参数：{}", BUSINESS_NAME, saveRequest);
            return R.error("新增" + BUSINESS_NAME + "失败");
        }

        // 关键：查询数据库获取完整实体（含createdAt/updatedAt）
        Permission savedEntity = permissionMapper.selectById(entity.getId());
        Assert.notNull(savedEntity, "新增" + BUSINESS_NAME + "成功，但查询不到数据，ID：" + entity.getId());

        // 实体转返回体（自动包含createdAt/updatedAt）
        PermissionSaveResponse response = BeanCopyUtils.copyIgnoreNull(savedEntity, PermissionSaveResponse.class);
        response.setId(savedEntity.getId()); // 显式赋值ID，确保非空

        log.info("【新增{}】成功，ID：{}", BUSINESS_NAME, savedEntity.getId());
        return R.success(response);
    }

    /**
     * 批量删除系统端-权限表
     * 删除存在的ID，返回实际删除数
     */
    @Override
    public R<Integer> delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return R.error("删除ID列表不能为空");
        }

        // 查询存在的ID（避免删除不存在的数据）
        LambdaQueryWrapper<Permission> existWrapper = new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, ids);
        List<Long> existIds = permissionMapper.selectList(existWrapper)
                .stream()
                .map(Permission::getId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(existIds)) {
            return R.error("要删除的" + BUSINESS_NAME + "不存在");
        }

        // 执行删除（仅删除存在的ID）
        LambdaQueryWrapper<Permission> deleteWrapper = new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, existIds);
        int deleteCount = permissionMapper.delete(deleteWrapper);

        log.info("【批量删除{}】请求ID数：{}，实际删除数：{}", BUSINESS_NAME, ids.size(), deleteCount);
        return R.success(deleteCount);
    }

    /**
     * 修改系统端-权限表
     * 核心：更新后查询数据库，补全updatedAt
     */
    @Override
    public R<PermissionUpdateResponse> update(Long id, PermissionUpdateRequest updateRequest) {
        // 参数校验
        Assert.notNull(id, BUSINESS_NAME + "ID不能为空");
        Assert.notNull(updateRequest, "更新请求参数不能为空");

        // 检查数据是否存在
        Permission existEntity = permissionMapper.selectById(id);
        if (existEntity == null) {
            log.error("【修改{}】失败，ID：{}不存在", BUSINESS_NAME, id);
            return R.error("要修改的" + BUSINESS_NAME + "不存在");
        }

        // 复制更新参数（仅非空字段，不覆盖BaseEntity字段）
        BeanCopyUtils.copyIgnoreNullToExist(updateRequest, existEntity);

        // 执行更新
        int updateCount = permissionMapper.updateById(existEntity);
        if (updateCount <= 0) {
            log.error("【修改{}】失败，ID：{}，请求参数：{}", BUSINESS_NAME, id, updateRequest);
            return R.error("修改" + BUSINESS_NAME + "失败");
        }

        // 查询最新数据（含更新后的updatedAt）
        Permission updatedEntity = permissionMapper.selectById(id);

        // 转换为返回体
        PermissionUpdateResponse response = BeanCopyUtils.copyIgnoreNull(updatedEntity, PermissionUpdateResponse.class);
        response.setId(id);

        log.info("【修改{}】成功，ID：{}", BUSINESS_NAME, id);
        return R.success(response);
    }

    /**
     * 分页查询系统端-权限表
     * 优化：校验分页参数，排序用BaseEntity的createdAt
     */
    @Override
    public R<PermissionPageResponse> page(PermissionPageRequest pageRequest) {
        // 分页参数校验（避免无效参数）
        int pageNum = pageRequest.getPageNum() <= 0 ? 1 : pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize() <= 0 ? 10 : pageRequest.getPageSize();
        pageSize = pageSize > 100 ? 100 : pageSize; // 限制最大页大小

        // 构建分页查询
        Page<Permission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<Permission>()
                .orderByDesc(Permission::getCreatedAt); // 匹配BaseEntity的createdAt

        IPage<Permission> iPage = permissionMapper.selectPage(page, queryWrapper);

        // 转换为VO分页对象（统一使用BeanCopyUtils）
        IPage<PermissionVo> voPage = iPage.convert(entity -> BeanCopyUtils.copyIgnoreNull(entity, PermissionVo.class));

        // 构建分页返回体
        PermissionPageResponse response = new PermissionPageResponse();
        PageMeta pageMeta = PageMeta.of(voPage.getTotal(), (int) voPage.getCurrent(), (int) voPage.getSize());
        response.setPageMeta(pageMeta);
        response.setRecords(voPage.getRecords());

        log.info("【分页查询{}】页码：{}，页大小：{}，总数：{}", BUSINESS_NAME, pageNum, pageSize, voPage.getTotal());
        return R.success(response);
    }

    /**
     * 查询系统端-权限表列表（不分页）
     */
    @Override
    public R<List<PermissionListResponse>> list() {
        List<Permission> entityList = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .orderByDesc(Permission::getCreatedAt));

        // 转换为ListResponse（继承Vo，无需重复赋值）
        List<PermissionListResponse> responseList = entityList.stream()
                .map(entity -> BeanCopyUtils.copyIgnoreNull(entity, PermissionListResponse.class))
                .collect(Collectors.toList());

        log.info("【查询{}列表】总数：{}", BUSINESS_NAME, responseList.size());
        return R.success(responseList);
    }

    /**
     * 查询系统端-权限表详情
     */
    @Override
    public R<PermissionVo> info(Long id) {
        Assert.notNull(id, BUSINESS_NAME + "ID不能为空");

        Permission entity = permissionMapper.selectById(id);
        if (entity == null) {
            log.error("【查询{}详情】ID：{}不存在", BUSINESS_NAME, id);
            return R.error(BUSINESS_NAME + "不存在");
        }

        PermissionVo vo = BeanCopyUtils.copyIgnoreNull(entity, PermissionVo.class);
        return R.success(vo);
    }

    /**
     * 根据用户ID查询权限列表
     */
    @Override
    public R<List<PermissionListResponse>> getPermissionsByUserId(Long userId) {
        Assert.notNull(userId, "用户ID不能为空");

        List<Permission> permissionList = permissionMapper.selectByUserId(userId);
        
        List<PermissionListResponse> responseList = permissionList.stream()
                .map(entity -> BeanCopyUtils.copyIgnoreNull(entity, PermissionListResponse.class))
                .collect(Collectors.toList());

        log.info("【查询用户权限列表】用户ID：{}，权限总数：{}", userId, responseList.size());
        return R.success(responseList);
    }
}