package com.zesheng.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.sys.entity.Permission;
import com.zesheng.sys.entity.Role;
import com.zesheng.sys.entity.RolePermission;
import com.zesheng.sys.mapper.PermissionMapper;
import com.zesheng.sys.mapper.RoleMapper;
import com.zesheng.sys.mapper.RolePermissionMapper;
import com.zesheng.sys.model.request.RolePageRequest;
import com.zesheng.sys.model.request.RoleSaveRequest;
import com.zesheng.sys.model.request.RoleUpdateRequest;
import com.zesheng.sys.model.response.RoleListResponse;
import com.zesheng.sys.model.response.RolePageResponse;
import com.zesheng.sys.model.response.RoleSaveResponse;
import com.zesheng.sys.model.response.RoleUpdateResponse;
import com.zesheng.sys.model.response.RoleVo;
import com.zesheng.sys.service.IRoleService;
import com.zesheng.common.response.R;
import com.zesheng.common.response.PageMeta;
import com.zesheng.common.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统端-角色表 服务实现类
 *
 * @author czk
 * @since 2026-02-19
 */
@Service
@RequiredArgsConstructor
@Slf4j // 生产级日志
public class RoleServiceImpl implements IRoleService {
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 新增系统端-角色表
     * 核心：新增后查询数据库，补全BaseEntity的createdAt/updatedAt
     */
    @Override
    public R<RoleSaveResponse> save(RoleSaveRequest saveRequest) {
        // DTO转实体（仅复制业务字段，BaseEntity字段由MyBatis-Plus自动填充）
        Role entity = BeanCopyUtils.copyIgnoreNull(saveRequest, Role.class);

        // 执行新增
        int insertCount = roleMapper.insert(entity);
        if (insertCount <= 0) {
            log.error("【新增{}】失败，请求参数：{}", BUSINESS_NAME, saveRequest);
            return R.error("新增" + BUSINESS_NAME + "失败");
        }

        // 关键：查询数据库获取完整实体（含createdAt/updatedAt）
        Role savedEntity = roleMapper.selectById(entity.getId());
        Assert.notNull(savedEntity, "新增" + BUSINESS_NAME + "成功，但查询不到数据，ID：" + entity.getId());

        // 实体转返回体（自动包含createdAt/updatedAt）
        RoleSaveResponse response = BeanCopyUtils.copyIgnoreNull(savedEntity, RoleSaveResponse.class);
        response.setId(savedEntity.getId()); // 显式赋值ID，确保非空

        log.info("【新增{}】成功，ID：{}", BUSINESS_NAME, savedEntity.getId());
        return R.success(response);
    }

    /**
     * 批量删除系统端-角色表
     * 优化：删除存在的ID，返回实际删除数（而非部分不存在就报错）
     */
    @Override
    public R<Integer> delete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return R.error("删除ID列表不能为空");
        }

        // 查询存在的ID（避免删除不存在的数据）
        LambdaQueryWrapper<Role> existWrapper = new LambdaQueryWrapper<Role>()
                .in(Role::getId, ids);
        List<Long> existIds = roleMapper.selectList(existWrapper)
                .stream()
                .map(Role::getId)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(existIds)) {
            return R.error("要删除的" + BUSINESS_NAME + "不存在");
        }

        // 执行删除（仅删除存在的ID）
        LambdaQueryWrapper<Role> deleteWrapper = new LambdaQueryWrapper<Role>()
                .in(Role::getId, existIds);
        int deleteCount = roleMapper.delete(deleteWrapper);

        log.info("【批量删除{}】请求ID数：{}，实际删除数：{}", BUSINESS_NAME, ids.size(), deleteCount);
        return R.success(deleteCount);
    }

    /**
     * 修改系统端-角色表
     * 核心：更新后查询数据库，补全updatedAt
     */
    @Override
    public R<RoleUpdateResponse> update(Long id, RoleUpdateRequest updateRequest) {
        // 参数校验
        Assert.notNull(id, BUSINESS_NAME + "ID不能为空");
        Assert.notNull(updateRequest, "更新请求参数不能为空");

        // 检查数据是否存在
        Role existEntity = roleMapper.selectById(id);
        if (existEntity == null) {
            log.error("【修改{}】失败，ID：{}不存在", BUSINESS_NAME, id);
            return R.error("要修改的" + BUSINESS_NAME + "不存在");
        }

        // 复制更新参数（仅非空字段，不覆盖BaseEntity字段）
        BeanCopyUtils.copyIgnoreNullToExist(updateRequest, existEntity);

        // 执行更新
        int updateCount = roleMapper.updateById(existEntity);
        if (updateCount <= 0) {
            log.error("【修改{}】失败，ID：{}，请求参数：{}", BUSINESS_NAME, id, updateRequest);
            return R.error("修改" + BUSINESS_NAME + "失败");
        }

        // 若传入权限码列表，则先删该角色原有关联再按列表重建
        if (updateRequest.getPermissionCodes() != null) {
            rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                    .eq(RolePermission::getRoleId, id));
            if (!updateRequest.getPermissionCodes().isEmpty()) {
                List<Permission> permissions = permissionMapper.selectList(
                        new LambdaQueryWrapper<Permission>()
                                .in(Permission::getCode, updateRequest.getPermissionCodes()));
                for (Permission p : permissions) {
                    RolePermission rp = new RolePermission();
                    rp.setRoleId(id);
                    rp.setPermissionId(p.getId());
                    rolePermissionMapper.insert(rp);
                }
            }
        }

        // 查询最新数据（含更新后的updatedAt）
        Role updatedEntity = roleMapper.selectById(id);

        // 转换为返回体
        RoleUpdateResponse response = BeanCopyUtils.copyIgnoreNull(updatedEntity, RoleUpdateResponse.class);
        response.setId(id);

        log.info("【修改{}】成功，ID：{}", BUSINESS_NAME, id);
        return R.success(response);
    }

    /**
     * 分页查询系统端-角色表
     * 优化：校验分页参数，排序用BaseEntity的createdAt
     */
    @Override
    public R<RolePageResponse> page(RolePageRequest pageRequest) {
        // 分页参数校验（避免无效参数）
        int pageNum = pageRequest.getPageNum() <= 0 ? 1 : pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize() <= 0 ? 10 : pageRequest.getPageSize();
        pageSize = pageSize > 100 ? 100 : pageSize; // 限制最大页大小

        // 构建分页查询
        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<Role>()
                .orderByDesc(Role::getCreatedAt); // 匹配BaseEntity的createdAt

        IPage<Role> iPage = roleMapper.selectPage(page, queryWrapper);

        // 转换为VO分页对象（统一使用BeanCopyUtils）
        IPage<RoleVo> voPage = iPage.convert(entity -> BeanCopyUtils.copyIgnoreNull(entity, RoleVo.class));

        // 构建分页返回体
        RolePageResponse response = new RolePageResponse();
        PageMeta pageMeta = PageMeta.of(voPage.getTotal(), (int) voPage.getCurrent(), (int) voPage.getSize());
        response.setPageMeta(pageMeta);
        response.setRecords(voPage.getRecords());

        log.info("【分页查询{}】页码：{}，页大小：{}，总数：{}", BUSINESS_NAME, pageNum, pageSize, voPage.getTotal());
        return R.success(response);
    }

    /**
     * 查询系统端-角色表列表（不分页）
     */
    @Override
    public R<List<RoleListResponse>> list() {
        List<Role> entityList = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .orderByDesc(Role::getCreatedAt));

        // 转换为ListResponse并填充每个角色的权限码列表
        List<RoleListResponse> responseList = entityList.stream()
                .map(entity -> {
                    RoleListResponse resp = BeanCopyUtils.copyIgnoreNull(entity, RoleListResponse.class);
                    List<RolePermission> rpList = rolePermissionMapper.selectList(
                            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, entity.getId()));
                    if (CollectionUtils.isEmpty(rpList)) {
                        resp.setPermissionCodes(Collections.emptyList());
                    } else {
                        List<Long> permissionIds = rpList.stream()
                                .map(RolePermission::getPermissionId).collect(Collectors.toList());
                        List<Permission> perms = permissionMapper.selectBatchIds(permissionIds);
                        resp.setPermissionCodes(perms.stream()
                                .map(Permission::getCode).collect(Collectors.toList()));
                    }
                    return resp;
                })
                .collect(Collectors.toList());

        log.info("【查询{}列表】总数：{}", BUSINESS_NAME, responseList.size());
        return R.success(responseList);
    }

    /**
     * 查询系统端-角色表详情
     */
    @Override
    public R<RoleVo> info(Long id) {
        Assert.notNull(id, BUSINESS_NAME + "ID不能为空");

        Role entity = roleMapper.selectById(id);
        if (entity == null) {
            log.error("【查询{}详情】ID：{}不存在", BUSINESS_NAME, id);
            return R.error(BUSINESS_NAME + "不存在");
        }

        RoleVo vo = BeanCopyUtils.copyIgnoreNull(entity, RoleVo.class);
        return R.success(vo);
    }
}