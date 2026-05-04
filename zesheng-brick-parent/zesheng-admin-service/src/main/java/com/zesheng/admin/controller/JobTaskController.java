package com.zesheng.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zesheng.common.request.PageAndSortQueryRequest;
import com.zesheng.common.response.PageResult;
import com.zesheng.common.response.R;
import com.zesheng.sys.entity.JobTask;
import com.zesheng.sys.model.request.JobTaskSaveRequest;
import com.zesheng.sys.model.request.JobTaskUpdateRequest;
import com.zesheng.sys.service.IJobTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("sys/job-task")
@Tag(name = "管理端-定时任务", description = "管理端-系统定时任务管理")
@RequiredArgsConstructor
public class JobTaskController {

    private final IJobTaskService jobTaskService;

    @GetMapping
    @Operation(summary = "查询任务列表")
    @PreAuthorize("hasAuthority('sys:job-task:list')")
    public R<List<JobTask>> list() {
        return R.success(jobTaskService.list());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询任务")
    @PreAuthorize("hasAuthority('sys:job-task:list')")
    public R<PageResult<JobTask>> page(PageAndSortQueryRequest queryDto) {
        IPage<JobTask> iPage = jobTaskService.page(queryDto);
        return R.success(PageResult.success(iPage).getData());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情")
    @PreAuthorize("hasAuthority('sys:job-task:list')")
    public R<JobTask> getById(@PathVariable Long id) {
        JobTask task = jobTaskService.getById(id);
        if (task == null) {
            return R.error("定时任务不存在");
        }
        return R.success(task);
    }

    @PostMapping
    @Operation(summary = "新增任务")
    @PreAuthorize("hasAuthority('sys:job-task:add')")
    public R<JobTask> save(@Valid @RequestBody JobTaskSaveRequest request) {
        return jobTaskService.save(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "修改任务")
    @PreAuthorize("hasAuthority('sys:job-task:update')")
    public R<JobTask> update(@PathVariable Long id, @Valid @RequestBody JobTaskUpdateRequest request) {
        return jobTaskService.update(id, request);
    }

    @PostMapping("/{id}/trigger")
    @Operation(summary = "手动触发任务")
    @PreAuthorize("hasAuthority('sys:job-task:update')")
    public R<String> trigger(@PathVariable Long id) {
        return jobTaskService.triggerNow(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    @PreAuthorize("hasAuthority('sys:job-task:delete')")
    public R<Integer> deleteById(@PathVariable Long id) {
        return jobTaskService.deleteById(id);
    }
}
