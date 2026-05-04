package com.zesheng.admin.controller;

import com.zesheng.common.response.R;
import com.zesheng.sys.entity.TodoTask;
import com.zesheng.sys.model.request.TodoTaskSaveRequest;
import com.zesheng.sys.model.request.TodoTaskUpdateRequest;
import com.zesheng.sys.service.ITodoTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("sys/todo-task")
@Tag(name = "管理端-待办任务", description = "管理端-待办任务管理")
@RequiredArgsConstructor
public class TodoTaskController {

    private final ITodoTaskService todoTaskService;

    @GetMapping
    @Operation(summary = "查询待办任务列表")
    @PreAuthorize("hasAuthority('sys:todo-task:list')")
    public R<List<TodoTask>> list(
            @RequestParam(required = false) @Min(value = 0, message = "状态值非法") @Max(value = 1, message = "状态值非法") Integer status,
            @RequestParam(required = false) String keyword) {
        return R.success(todoTaskService.list(status, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询待办任务详情")
    @PreAuthorize("hasAuthority('sys:todo-task:list')")
    public R<TodoTask> getById(@PathVariable Long id) {
        TodoTask todoTask = todoTaskService.getById(id);
        if (todoTask == null) {
            return R.error("待办任务不存在");
        }
        return R.success(todoTask);
    }

    @PostMapping
    @Operation(summary = "新增待办任务")
    @PreAuthorize("hasAuthority('sys:todo-task:add')")
    public R<TodoTask> save(@Valid @RequestBody TodoTaskSaveRequest request) {
        return todoTaskService.save(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "编辑待办任务")
    @PreAuthorize("hasAuthority('sys:todo-task:update')")
    public R<TodoTask> update(@PathVariable Long id, @Valid @RequestBody TodoTaskUpdateRequest request) {
        return todoTaskService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新待办任务状态")
    @PreAuthorize("hasAuthority('sys:todo-task:update')")
    public R<TodoTask> updateStatus(
            @PathVariable Long id,
            @RequestParam @Min(value = 0, message = "状态值非法") @Max(value = 1, message = "状态值非法") Integer status) {
        return todoTaskService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除待办任务")
    @PreAuthorize("hasAuthority('sys:todo-task:delete')")
    public R<Integer> deleteById(@PathVariable Long id) {
        return todoTaskService.deleteById(id);
    }
}
