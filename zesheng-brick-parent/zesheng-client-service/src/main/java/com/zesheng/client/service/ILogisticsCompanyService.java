package com.zesheng.client.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zesheng.client.entity.LogisticsCompany;

import java.util.List;

public interface ILogisticsCompanyService extends IService<LogisticsCompany> {

    /**
     * 启用的物流公司，名称模糊可选，按 sort、id 排序，最多 20 条（与管理端列表逻辑一致）
     */
    List<LogisticsCompany> getLogisticsCompanies(String name);
}
