package com.zesheng.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.sys.entity.Permission;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository; // 标识Mapper组件，消除IDEA提示

import java.util.List;

/**
* 系统端-权限表 Mapper 接口
*
* @author czk
* @since 2026-02-19
*/
@Repository // 生产级规范：标注Mapper为Spring组件
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_role r ON rp.role_id = r.id " +
            "INNER JOIN admin_user u ON r.id = u.role_id " +
            "WHERE u.id = #{userId} AND r.status = 1")
    List<Permission> selectByUserId(Long userId);
}