package com.zesheng.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;
import com.zesheng.sys.entity.ConfigEntity;
import com.zesheng.sys.model.request.ConfigSaveRequest;
import com.zesheng.sys.model.request.ConfigUpdateRequest;

import java.util.List;

public interface IConfigService {

    IPage<ConfigEntity> pageConfig(PageAndSortQueryRequest queryDto);

    /** 全量列表（不分页） */
    List<ConfigEntity> list();

    ConfigEntity getById(Long id);

    ConfigEntity getByKey(String configKey);

    R<ConfigEntity> save(ConfigSaveRequest request);

    R<ConfigEntity> update(Long id, ConfigUpdateRequest request);

    R<Integer> deleteById(Long id);
}
