package com.zesheng.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.LogisticsCompany;
import com.zesheng.admin.model.request.LogisticsCompanyPageRequest;
import com.zesheng.admin.model.request.LogisticsCompanySaveRequest;
import com.zesheng.admin.model.request.LogisticsCompanyUpdateRequest;
import com.zesheng.common.response.R;

/**
 * 管理端-物流公司
 */
public interface ILogisticsCompanyService {

    /**
     * 分页查询（按排序值、ID 升序）
     */
    IPage<LogisticsCompany> page(LogisticsCompanyPageRequest request);

    R<LogisticsCompany> get(Long id);

    R<LogisticsCompany> save(LogisticsCompanySaveRequest request);

    R<LogisticsCompany> update(Long id, LogisticsCompanyUpdateRequest request);

    R<Integer> delete(Long id);
}
