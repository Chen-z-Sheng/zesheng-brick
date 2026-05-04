package com.zesheng.admin.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.FormScheme;
import com.zesheng.admin.model.response.FormSchemeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FormSchemeMapper extends BaseMapper<FormScheme> {

    /**
     * 插入并回填主键 id，使用 @Param 确保 keyProperty 正确绑定到继承自 BaseEntity 的 id 字段
     */
    int insertAndFillId(@Param("entity") FormScheme entity);

    /**
     * 分页查询方案
     */
    @Select("SELECT s.* FROM admin_form_schemes s ${ew.customSqlSegment}")
    IPage<FormSchemeVo> selectPageForVo(Page<FormScheme> page, @Param(Constants.WRAPPER) Wrapper<FormScheme> queryWrapper);
}
