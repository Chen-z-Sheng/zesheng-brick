package com.zesheng.admin.service;

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
import com.zesheng.common.response.R;

import java.util.List;

/**
 * 用户表 服务类
 *
 * @author czk
 * @since 2026-02-16
 */
public interface IUserService {

    /**
     * 新增用户表
     *
     * @param userSaveRequest 新增请求
     * @return 新增结果
     */
    R<UserSaveResponse> save(UserSaveRequest userSaveRequest);

    /**
     * 删除用户表
     *
     * @param ids 用户表id列表
     * @return 删除数量
     */
    R<List<Long>> delete(List<Long> ids);

    /**
     * 修改用户表
     *
     * @param id 用户表id
     * @param userUpdateRequest 更新请求
     * @return 更新结果
     */
    R<UserUpdateResponse> update(Long id, UserUpdateRequest userUpdateRequest);

    /**
     * 分页查询用户表
     *
     * @param userPageRequest 分页查询请求参数
     * @return 分页查询结果
     */
    R<UserPageResponse> page(UserPageRequest userPageRequest);

    /**
     * 查询用户表列表
     *
     * @return 用户表列表
     */
    R<List<UserListResponse>> list();

    /**
     * 查询用户表详情
     *
     * @param id 用户表id
     * @return 用户表详情
     */
    R<UserVo> info(Long id);

    /**
     * 当前登录用户更新个人资料（手机号、头像等）
     */
    R<UserUpdateResponse> updateSelfProfile(SelfProfileUpdateRequest request);

    /**
     * 当前登录用户修改密码
     */
    R<Void> changeOwnPassword(PasswordChangeRequest request);

}
