package com.zesheng.sys.mapper;

    import com.baomidou.mybatisplus.core.mapper.BaseMapper;
    import com.zesheng.sys.entity.Role;

import org.springframework.stereotype.Repository; // 标识Mapper组件，消除IDEA提示

/**
* 系统端-角色表 Mapper 接口
*
* @author czk
* @since 2026-02-19
*/
@Repository // 生产级规范：标注Mapper为Spring组件
public interface RoleMapper extends BaseMapper<Role> {

}