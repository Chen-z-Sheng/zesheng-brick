package com.zesheng.sys.service;

import com.zesheng.common.response.R;
import com.zesheng.sys.entity.TodoTask;
import com.zesheng.sys.model.request.TodoTaskSaveRequest;
import com.zesheng.sys.model.request.TodoTaskUpdateRequest;

import java.util.List;

public interface ITodoTaskService {

    List<TodoTask> list(Integer status, String keyword);

    TodoTask getById(Long id);

    R<TodoTask> save(TodoTaskSaveRequest request);

    R<TodoTask> update(Long id, TodoTaskUpdateRequest request);

    R<TodoTask> updateStatus(Long id, Integer status);

    R<Integer> deleteById(Long id);
}
