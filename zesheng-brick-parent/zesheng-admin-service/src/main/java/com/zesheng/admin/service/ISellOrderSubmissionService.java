package com.zesheng.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.admin.entity.SellOrderSubmission;
import com.zesheng.admin.model.request.SellOrderSubmissionPageRequest;
import com.zesheng.admin.model.request.SellOrderSubmissionUpdateRequest;
import com.zesheng.common.response.R;

/**
 * 行情报单提交记录 Service
 */
public interface ISellOrderSubmissionService {

    IPage<SellOrderSubmission> page(SellOrderSubmissionPageRequest request);

    SellOrderSubmission getById(Long id);

    R<SellOrderSubmission> update(Long id, SellOrderSubmissionUpdateRequest request);

    R<SellOrderSubmission> appendSettledProofUrl(Long id, String url);

    R<SellOrderSubmission> removeSettledProofUrl(Long id, String url, Integer index);
}
