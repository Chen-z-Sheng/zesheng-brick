package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.LogisticsCompany;
import com.zesheng.admin.mapper.LogisticsCompanyMapper;
import com.zesheng.admin.model.request.LogisticsCompanyPageRequest;
import com.zesheng.admin.model.request.LogisticsCompanySaveRequest;
import com.zesheng.admin.model.request.LogisticsCompanyUpdateRequest;
import com.zesheng.admin.service.ILogisticsCompanyService;
import com.zesheng.common.response.R;
import com.zesheng.common.util.BeanCopyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LogisticsCompanyServiceImpl implements ILogisticsCompanyService {

    private final LogisticsCompanyMapper logisticsCompanyMapper;

    @Override
    @Transactional(readOnly = true)
    public IPage<LogisticsCompany> page(LogisticsCompanyPageRequest request) {
        Page<LogisticsCompany> page = new Page<>(request.getPageNum(), request.getPageSize());
        // sort 与 C 端一致：数值大为全局选用热度，常用排前
        LambdaQueryWrapper<LogisticsCompany> wrapper = new LambdaQueryWrapper<LogisticsCompany>()
                .orderByDesc(LogisticsCompany::getSort)
                .orderByAsc(LogisticsCompany::getId);
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(LogisticsCompany::getName, request.getName().trim());
        }
        return logisticsCompanyMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public R<LogisticsCompany> get(Long id) {
        Assert.notNull(id, "ID不能为空");
        LogisticsCompany entity = logisticsCompanyMapper.selectById(id);
        if (entity == null) {
            return R.error("物流公司不存在");
        }
        return R.success(entity);
    }

    @Override
    public R<LogisticsCompany> save(LogisticsCompanySaveRequest request) {
        LogisticsCompany entity = BeanCopyUtils.copyIgnoreNull(request, LogisticsCompany.class);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(Boolean.TRUE);
        }
        logisticsCompanyMapper.insert(entity);
        return R.success(entity);
    }

    @Override
    public R<LogisticsCompany> update(Long id, LogisticsCompanyUpdateRequest request) {
        Assert.notNull(id, "ID不能为空");
        LogisticsCompany exist = logisticsCompanyMapper.selectById(id);
        if (exist == null) {
            return R.error("物流公司不存在");
        }
        BeanCopyUtils.copyIgnoreNullToExist(request, exist);
        logisticsCompanyMapper.updateById(exist);
        return R.success(exist);
    }

    @Override
    public R<Integer> delete(Long id) {
        Assert.notNull(id, "ID不能为空");
        LogisticsCompany exist = logisticsCompanyMapper.selectById(id);
        if (exist == null) {
            return R.error("物流公司不存在");
        }
        return R.success(logisticsCompanyMapper.deleteById(id));
    }
}
