package com.zesheng.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zesheng.admin.entity.HelpFaq;
import com.zesheng.admin.mapper.HelpFaqMapper;
import com.zesheng.admin.model.request.HelpFaqSaveRequest;
import com.zesheng.admin.model.request.HelpFaqUpdateRequest;
import com.zesheng.admin.service.IHelpFaqService;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 帮助中心FAQ Service实现
 */
@Service
public class HelpFaqServiceImpl implements IHelpFaqService {

    @Resource
    private HelpFaqMapper helpFaqMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HelpFaq> list() {
        return helpFaqMapper.selectList(
                new LambdaQueryWrapper<HelpFaq>().orderByAsc(HelpFaq::getSortOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<HelpFaq> page(PageAndSortQueryRequest queryDto) {
        Page<HelpFaq> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        LambdaQueryWrapper<HelpFaq> wrapper = new LambdaQueryWrapper<>();
        String orderBy = queryDto.getOrderBy();
        if (orderBy != null && !orderBy.isBlank()) {
            boolean asc = "ASC".equalsIgnoreCase(queryDto.getOrder());
            wrapper.orderBy(true, asc,
                    "sort_order".equals(orderBy) ? HelpFaq::getSortOrder : HelpFaq::getUpdatedAt);
        } else {
            wrapper.orderByAsc(HelpFaq::getSortOrder);
        }
        return helpFaqMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public HelpFaq getById(Long id) {
        return helpFaqMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<HelpFaq> save(HelpFaqSaveRequest request) {
        HelpFaq entity = new HelpFaq();
        entity.setQuestion(request.getQuestion());
        entity.setAnswer(request.getAnswer());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        helpFaqMapper.insert(entity);
        return R.success(helpFaqMapper.selectById(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<HelpFaq> update(Long id, HelpFaqUpdateRequest request) {
        HelpFaq entity = helpFaqMapper.selectById(id);
        if (entity == null) {
            return R.error("FAQ不存在");
        }
        entity.setQuestion(request.getQuestion());
        entity.setAnswer(request.getAnswer());
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        helpFaqMapper.updateById(entity);
        return R.success(helpFaqMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteById(Long id) {
        int rows = helpFaqMapper.deleteById(id);
        return R.success(rows);
    }
}
