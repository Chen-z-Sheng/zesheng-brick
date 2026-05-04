package com.zesheng.client.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zesheng.client.entity.LogisticsCompany;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LogisticsCompanyMapper extends BaseMapper<LogisticsCompany> {

    /**
     * 报单成功且名称与字典完全一致时，将启用状态的物流公司 sort+1（全局热度，不按用户区分）
     */
    @Update("UPDATE admin_logistics_company SET sort = IFNULL(sort, 0) + 1, updated_at = NOW() "
            + "WHERE status = 1 AND name = #{name}")
    int incrementSortByEnabledName(@Param("name") String name);
}
