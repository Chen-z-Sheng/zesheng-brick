package com.zesheng.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zesheng.common.response.R;
import com.zesheng.sys.entity.TodoTask;
import com.zesheng.sys.mapper.TodoTaskMapper;
import com.zesheng.sys.model.request.TodoTaskSaveRequest;
import com.zesheng.sys.model.request.TodoTaskUpdateRequest;
import com.zesheng.sys.service.ITodoTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TodoTaskServiceImpl implements ITodoTaskService {

    @Resource
    private TodoTaskMapper todoTaskMapper;

    @Override
    public List<TodoTask> list(Integer status, String keyword) {
        LambdaQueryWrapper<TodoTask> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(TodoTask::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(TodoTask::getTitle, keyword.trim())
                    .or()
                    .like(TodoTask::getContent, keyword.trim()));
        }
        queryWrapper.orderByDesc(TodoTask::getUpdatedAt).orderByDesc(TodoTask::getId);
        return todoTaskMapper.selectList(queryWrapper);
    }

    @Override
    public TodoTask getById(Long id) {
        return todoTaskMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<TodoTask> save(TodoTaskSaveRequest request) {
        TodoTask todoTask = new TodoTask();
        todoTask.setTitle(request.getTitle().trim());
        todoTask.setContent(request.getContent());
        todoTask.setStatus(normalizeStatus(request.getStatus()));
        todoTaskMapper.insert(todoTask);
        return R.success(todoTaskMapper.selectById(todoTask.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<TodoTask> update(Long id, TodoTaskUpdateRequest request) {
        TodoTask todoTask = todoTaskMapper.selectById(id);
        if (todoTask == null) {
            return R.error("待办任务不存在");
        }
        todoTask.setTitle(request.getTitle().trim());
        todoTask.setContent(request.getContent());
        todoTask.setStatus(normalizeStatus(request.getStatus()));
        todoTaskMapper.updateById(todoTask);
        return R.success(todoTaskMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<TodoTask> updateStatus(Long id, Integer status) {
        TodoTask todoTask = todoTaskMapper.selectById(id);
        if (todoTask == null) {
            return R.error("待办任务不存在");
        }
        todoTask.setStatus(normalizeStatus(status));
        todoTaskMapper.updateById(todoTask);
        return R.success(todoTaskMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Integer> deleteById(Long id) {
        TodoTask todoTask = todoTaskMapper.selectById(id);
        if (todoTask == null) {
            return R.error("待办任务不存在");
        }
        return R.success(todoTaskMapper.deleteById(id));
    }

    private Integer normalizeStatus(Integer status) {
        return Integer.valueOf(1).equals(status) ? 1 : 0;
    }
}
