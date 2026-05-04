package com.zesheng.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.R;
import com.zesheng.sys.entity.JobTask;
import com.zesheng.sys.model.request.JobTaskSaveRequest;
import com.zesheng.sys.model.request.JobTaskUpdateRequest;

import java.util.List;

public interface IJobTaskService {

    List<JobTask> list();

    IPage<JobTask> page(PageAndSortQueryRequest queryRequest);

    JobTask getById(Long id);

    R<JobTask> save(JobTaskSaveRequest request);

    R<JobTask> update(Long id, JobTaskUpdateRequest request);

    R<Integer> deleteById(Long id);

    void scanAndExecuteDueTasks();

    R<String> triggerNow(Long id);
}
