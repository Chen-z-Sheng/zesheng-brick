package com.zesheng.client.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.client.entity.HelpFaq;
import com.zesheng.client.mapper.HelpFaqMapper;
import com.zesheng.client.service.IHelpFaqService;
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
    public List<HelpFaq> listEnabled() {
        return helpFaqMapper.selectList(
                new LambdaQueryWrapper<HelpFaq>()
                        .eq(HelpFaq::getStatus, 1)
                        .orderByAsc(HelpFaq::getSortOrder));
    }
}
