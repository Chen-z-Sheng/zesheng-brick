package com.zesheng.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.FormSubmission;
import com.zesheng.admin.model.request.FormSubmissionPageRequest;
import com.zesheng.admin.model.request.FormSubmissionUpdateRequest;
import com.zesheng.common.response.R;

/**
 * 固结报单提交记录 Service
 */
public interface IFormSubmissionService {

    /**
     * 分页查询
     */
    IPage<FormSubmission> page(FormSubmissionPageRequest request);

    /**
     * 根据ID查询
     */
    FormSubmission getById(Long id);

    /**
     * 更新（状态、备注等）
     */
    R<FormSubmission> update(Long id, FormSubmissionUpdateRequest request);

    R<FormSubmission> appendSettledProofUrl(Long id, String url);

    R<FormSubmission> removeSettledProofUrl(Long id, String url, Integer index);
}
