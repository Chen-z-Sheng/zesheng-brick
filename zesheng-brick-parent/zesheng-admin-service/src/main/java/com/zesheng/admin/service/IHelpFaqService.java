package com.zesheng.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.HelpFaq;
import com.zesheng.admin.model.request.HelpFaqSaveRequest;
import com.zesheng.admin.model.request.HelpFaqUpdateRequest;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;

import java.util.List;

/**
 * 帮助中心FAQ Service接口
 */
public interface IHelpFaqService {

    List<HelpFaq> list();

    IPage<HelpFaq> page(PageAndSortQueryRequest queryDto);

    HelpFaq getById(Long id);

    R<HelpFaq> save(HelpFaqSaveRequest request);

    R<HelpFaq> update(Long id, HelpFaqUpdateRequest request);

    R<Integer> deleteById(Long id);
}
