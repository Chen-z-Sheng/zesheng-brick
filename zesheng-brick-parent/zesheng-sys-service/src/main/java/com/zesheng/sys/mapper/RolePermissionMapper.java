package com.zesheng.sys.mapper;

    import com.baomidou.mybatisplus.core.mapper.BaseMapper;
    import com.zesheng.sys.entity.RolePermission;

import org.springframework.stereotype.Repository; // 标识Mapper组件，消除IDEA提示

/**
* 系统端-角色与权限关联 Mapper 接口
*
* @author czk
* @since 2026-02-20
*/
@Repository // 生产级规范：标注Mapper为Spring组件
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

}