package com.zesheng.admin.service.impl;
import com.zesheng.common.response.PageMeta;
import org.springframework.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.User;
import com.zesheng.admin.mapper.UserMapper;
import com.zesheng.sys.service.IRoleService;
import com.zesheng.admin.model.request.PasswordChangeRequest;
import com.zesheng.admin.model.request.SelfProfileUpdateRequest;
import com.zesheng.admin.model.request.UserPageRequest;
import com.zesheng.admin.model.request.UserSaveRequest;
import com.zesheng.admin.model.request.UserUpdateRequest;
import com.zesheng.admin.model.response.UserListResponse;
import com.zesheng.admin.model.response.UserPageResponse;
import com.zesheng.admin.model.response.UserSaveResponse;
import com.zesheng.admin.model.response.UserUpdateResponse;
import com.zesheng.admin.model.response.UserVo;
import com.zesheng.admin.config.AdminPrincipal;
import com.zesheng.admin.service.IUserService;
import com.zesheng.common.enums.StatusEnum;
import com.zesheng.common.response.R;
import com.zesheng.common.util.BeanCopyUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
/**
 * 用户表 服务实现类
 *
 * @author czk
 * @since 2026-02-16
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final IRoleService roleService;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 新增用户表
     *
     * @param userSaveRequest 新增请求
     * @return 新增结果
     */
    @Override
    public R<UserSaveResponse> save(UserSaveRequest userSaveRequest) {
        String username = userSaveRequest.getUsername() != null ? userSaveRequest.getUsername().trim() : "";
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() > 60) {
            throw new IllegalArgumentException("用户名长度不能超过60个字符");
        }
        long usernameTaken = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .isNull(User::getDeletedAt));
        if (usernameTaken > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String phone = userSaveRequest.getPhone() != null ? userSaveRequest.getPhone().trim() : "";
        if (StringUtils.hasText(phone)) {
            long phoneTaken = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, phone)
                    .isNull(User::getDeletedAt));
            if (phoneTaken > 0) {
                throw new IllegalArgumentException("手机号已被占用");
            }
        }

        Long roleId = userSaveRequest.getRoleId();
        if (roleId != null) {
            R<com.zesheng.sys.model.response.RoleVo> roleResult = roleService.info(roleId);
            if (roleResult == null || roleResult.getData() == null) {
                throw new IllegalArgumentException("所选角色不存在");
            }
        }

        // 创建User实体对象（仅复制请求中的非空参数）
        User user = BeanCopyUtils.copyIgnoreNull(userSaveRequest, User.class);
        user.setUsername(username);
        user.setPhone(StringUtils.hasText(phone) ? phone : null);

        // 创建时可不传头像，后续在个人中心等入口补充；空串按未设置入库
        String avatarInput = userSaveRequest.getAvatarUrl() != null ? userSaveRequest.getAvatarUrl().trim() : "";
        user.setAvatarUrl(StringUtils.hasText(avatarInput) ? avatarInput : null);

        // 账号状态：仅允许启用/禁用，默认启用
        Integer statusCode = userSaveRequest.getStatus();
        if (statusCode != null) {
            if (statusCode == 0) {
                user.setStatus(StatusEnum.DISABLE);
            } else if (statusCode == 1) {
                user.setStatus(StatusEnum.ENABLE);
            } else {
                throw new IllegalArgumentException("账号状态只能为0（禁用）或1（启用）");
            }
        } else {
            user.setStatus(StatusEnum.ENABLE);
        }

        // 密码加密处理（保留原有逻辑）
        if (null != userSaveRequest.getPassword()) {
            user.setPasswordHash(passwordEncoder.encode(userSaveRequest.getPassword()));
        }

        Long creatorId = AdminPrincipal.getCurrentUserId();
        if (creatorId != null) {
            user.setCreateBy(creatorId);
        }

        // 执行插入操作（MyBatis insert后，主键id会回显到user中）
        int result = userMapper.insert(user);

        if (result > 0) {
            // 关键：根据新增的ID查询数据库中的完整实体（包含自动生成的创建/修改时间）
            User savedUser = userMapper.selectById(user.getId());
            // 健壮性校验：防止新增成功但查询不到的极端情况
            Assert.notNull(savedUser, "新增用户成功，但查询不到新增的数据，ID：" + user.getId());

            UserSaveResponse response = BeanCopyUtils.copyIgnoreNull(savedUser, UserSaveResponse.class);

            return R.success(response);
        } else {
            return R.error("新增用户表失败");
        }
    }

    /**
     * 删除用户表
     *
     * @param ids 用户表id列表
     * @return 删除数量
     */
    @Override
    public R<List<Long>> delete(List<Long> ids) {
        // 基础非空校验（用Spring工具类更健壮）
        if (CollectionUtils.isEmpty(ids)) {
            return R.error("删除id列表不能为空");
        }

        // 精准查询「实际存在且未软删除」的ID列表（核心：替代count校验）
        List<Long> existIds = userMapper.selectObjs(new LambdaQueryWrapper<User>()
                        .in(User::getId, ids)
                        .isNull(User::getDeletedAt) // 排除已软删除的记录，避免重复删除
                ).stream()
                .map(obj -> Long.parseLong(obj.toString()))
                .collect(Collectors.toList());

        // 无有效ID直接返回错误
        if (CollectionUtils.isEmpty(existIds)) {
            return R.error("要删除的用户不存在");
        }

        // 执行软删除（生产规范，若业务必须物理删除，替换为delete即可）
        int result = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .in(User::getId, existIds)
                .set(User::getDeletedAt, LocalDateTime.now()));

        // 返回实际成功删除的ID，兼容部分ID不存在的场景
        if (result > 0) {
            // 构建提示信息：区分“全部成功”和“部分成功”
            String successMsg = "删除成功";
            if (existIds.size() < ids.size()) {
                List<Long> notExistIds = ids.stream()
                        .filter(id -> !existIds.contains(id))
                        .collect(Collectors.toList());
                successMsg = String.format("成功删除%d条用户数据，%d条用户不存在（不存在ID：%s）",
                        existIds.size(), notExistIds.size(), notExistIds);
            }
            return R.success(successMsg, existIds);
        } else {
            // 删除操作执行失败
            return R.error("删除用户表失败");
        }
    }

    /**
     * 修改用户表
     *
     * @param id                用户表id
     * @param userUpdateRequest 更新请求
     * @return 更新结果
     */
    @Override
    public R<UserUpdateResponse> update(Long id, UserUpdateRequest userUpdateRequest) {
        Assert.notNull(id, "用户表id不能为空");
        Assert.notNull(userUpdateRequest, "更新请求参数不能为空");

        // 先查询数据是否存在
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            return R.error("要更新的用户不存在");
        }

        // 修正：使用新增的工具方法，把请求参数复制到已有User对象中
        BeanCopyUtils.copyIgnoreNullToExist(userUpdateRequest, existUser);

        // 执行更新操作
        int result = userMapper.updateById(existUser);

        if (result > 0) {
            // 查询数据库最新的记录（包含自动更新的updatedAt）
            User updatedUser = userMapper.selectById(id);
            Assert.notNull(updatedUser, "更新用户成功，但查询不到最新数据，ID：" + id);

            // 用现有工具方法创建响应对象（符合原方法设计）
            UserUpdateResponse response = BeanCopyUtils.copyIgnoreNull(updatedUser, UserUpdateResponse.class);
            return R.success(response);
        } else {
            return R.error("更新用户表失败");
        }
    }

    /**
     * 分页查询用户表
     *
     * @param userPageRequest 分页查询请求参数
     * @return 分页查询结果
     */
    @Override
    public R<UserPageResponse> page(UserPageRequest userPageRequest) {
        // 构建分页查询条件并执行查询
        Page<User> page = new Page<>(userPageRequest.getPageNum(), userPageRequest.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(User::getCreatedAt);
        IPage<User> iPage = userMapper.selectPage(page, queryWrapper);

        // 将User分页对象转换为UserVo分页对象
        IPage<UserVo> voPage = iPage.convert(user -> {
            UserVo vo = new UserVo();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });

        // 构建UserPageResponse对象（利用继承关系赋值）
        UserPageResponse response = new UserPageResponse();
        // 给父类PageResult的属性赋值（PageMetaDto + 数据列表）
        PageMeta pageMeta = PageMeta.of(voPage.getTotal(), (int) voPage.getCurrent(), (int) voPage.getSize());
        response.setPageMeta(pageMeta);
        response.setRecords(voPage.getRecords());

        return R.success(response);
    }

    /**
     * 查询用户表列表
     *
     * @return 用户表列表
     */
    @Override
    public R<List<UserListResponse>> list() {
        // 查询所有用户表
        List<User> list = userMapper.selectList(null);

        // 将User列表转换为UserListResponse列表
        List<UserListResponse> responseList = list.stream().map(user -> {
            UserListResponse response = new UserListResponse();
            BeanUtils.copyProperties(user, response);
            return response;
        }).collect(java.util.stream.Collectors.toList());

        return R.success(responseList);
    }

    /**
     * 查询用户表详情
     *
     * @param id 用户表id
     * @return 用户表详情
     */
    @Override
    public R<UserVo> info(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return R.error("用户表不存在");
        }

        UserVo vo = new UserVo();
        BeanUtils.copyProperties(user, vo);

        if (user.getRoleId() != null) {
            R<com.zesheng.sys.model.response.RoleVo> roleResult = roleService.info(user.getRoleId());
            if (roleResult != null && roleResult.getData() != null) {
                vo.setRoleCode(roleResult.getData().getCode());
            }
        }

        return R.success(vo);
    }

    /**
     * 当前登录用户更新资料：仅处理请求中非 null 字段（null 表示不修改）
     */
    @Override
    public R<UserUpdateResponse> updateSelfProfile(SelfProfileUpdateRequest request) {
        Long userId = AdminPrincipal.getCurrentUserId();
        if (userId == null) {
            return R.error("未登录或会话已失效");
        }
        Assert.notNull(request, "请求参数不能为空");

        User existUser = userMapper.selectById(userId);
        if (existUser == null || existUser.getDeletedAt() != null) {
            return R.error("用户不存在");
        }

        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            if (phone.isEmpty()) {
                existUser.setPhone(null);
            } else {
                long phoneTaken = userMapper.selectCount(new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, phone)
                        .ne(User::getId, userId)
                        .isNull(User::getDeletedAt));
                if (phoneTaken > 0) {
                    throw new IllegalArgumentException("手机号已被其他账号占用");
                }
                existUser.setPhone(phone);
            }
        }

        if (request.getAvatarUrl() != null) {
            String url = request.getAvatarUrl().trim();
            existUser.setAvatarUrl(url.isEmpty() ? null : url);
        }

        int result = userMapper.updateById(existUser);
        if (result <= 0) {
            return R.error("更新资料失败");
        }
        User updated = userMapper.selectById(userId);
        Assert.notNull(updated, "更新成功但查询不到用户数据");
        UserUpdateResponse response = BeanCopyUtils.copyIgnoreNull(updated, UserUpdateResponse.class);
        return R.success(response);
    }

    /**
     * 校验旧密码后更新当前用户登录密码
     */
    @Override
    public R<Void> changeOwnPassword(PasswordChangeRequest request) {
        Long userId = AdminPrincipal.getCurrentUserId();
        if (userId == null) {
            return R.error("未登录或会话已失效");
        }
        Assert.notNull(request, "请求参数不能为空");

        User user = userMapper.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            return R.error("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("当前密码不正确");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("新密码不能与当前密码相同");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        int n = userMapper.updateById(user);
        return n > 0 ? R.success() : R.error("修改密码失败");
    }

}
