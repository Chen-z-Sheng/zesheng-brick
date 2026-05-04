package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zesheng.client.entity.LogisticsCompany;
import com.zesheng.client.mapper.LogisticsCompanyMapper;
import com.zesheng.client.service.ILogisticsCompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LogisticsCompanyServiceImpl extends ServiceImpl<LogisticsCompanyMapper, LogisticsCompany>
        implements ILogisticsCompanyService {

    /** 有关键词时联想条数上限 */
    private static final int MAX_ROWS_FILTER = 50;
    /** 无关键词时返回条数上限（小程序字典全量用于筛选） */
    private static final int MAX_ROWS_LIST = 500;

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsCompany> getLogisticsCompanies(String name) {
        LambdaQueryWrapper<LogisticsCompany> w = new LambdaQueryWrapper<>();
        w.eq(LogisticsCompany::getStatus, true);
        if (StringUtils.hasText(name)) {
            w.like(LogisticsCompany::getName, name.trim());
        }
        // sort 越大表示选用次数越多，常用项排前
        w.orderByDesc(LogisticsCompany::getSort).orderByAsc(LogisticsCompany::getId);
        int cap = StringUtils.hasText(name) ? MAX_ROWS_FILTER : MAX_ROWS_LIST;
        w.last("LIMIT " + cap);
        return list(w);
    }
}
