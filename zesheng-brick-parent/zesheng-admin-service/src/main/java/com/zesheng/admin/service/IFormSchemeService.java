package com.zesheng.admin.service;

import com.zesheng.admin.model.request.FormSchemePageRequest;
import com.zesheng.admin.model.request.FormSchemeSaveRequest;
import com.zesheng.admin.model.request.FormSchemeUpdateRequest;
import com.zesheng.admin.model.response.FormSchemeListResponse;
import com.zesheng.admin.model.response.FormSchemePageResponse;
import com.zesheng.admin.model.response.FormSchemeSaveResponse;
import com.zesheng.admin.model.response.FormSchemeUpdateResponse;
import com.zesheng.admin.model.response.FormSchemeVo;
import com.zesheng.common.request.BatchDeleteRequest;
import com.zesheng.common.response.BatchDeleteResponse;
import com.zesheng.common.response.R;

import java.util.List;

public interface IFormSchemeService {
    R<FormSchemeSaveResponse> save(FormSchemeSaveRequest formSchemeSaveRequest);

    R<BatchDeleteResponse> batchDelete(BatchDeleteRequest ids);

    R<FormSchemeUpdateResponse> update(Long id, FormSchemeUpdateRequest formSchemeUpdateRequest);

    R<FormSchemePageResponse> page(FormSchemePageRequest formSchemePageRequest);

    R<List<FormSchemeListResponse>> list();

    R<FormSchemeVo> info(Long id);

    R<Integer> delete(Long id);
}
